package com.yam.myaiagent.taskexecutor;

import com.yam.myaiagent.taskdecompose.DecomposedTask;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * 抽象任务执行器
 * 提供基础的任务执行逻辑和工具方法
 */
@Slf4j
public abstract class AbstractTaskExecutor implements TaskExecutor {

    // 线程池，用于异步执行任务
    protected final ExecutorService executorService;
    
    /**
     * 构造函数
     */
    public AbstractTaskExecutor() {
        // 创建固定大小的线程池，线程数为可用处理器数量
        this.executorService = Executors.newFixedThreadPool(
                Runtime.getRuntime().availableProcessors()
        );
    }
    
    /**
     * 执行单个任务
     * 
     * @param task 待执行的任务
     * @return 执行后的任务（包含执行结果和状态）
     */
    @Override
    public DecomposedTask execute(DecomposedTask task) {
        log.info("开始执行任务: {}", task.getTaskId());
        
        // 设置任务状态为执行中
        task.setStatus(DecomposedTask.ExecutionStatus.IN_PROGRESS);
        task.setStartTime(LocalDateTime.now());
        
        try {
            // 执行具体任务逻辑
            DecomposedTask result = executeTask(task);
            
            // 设置任务状态为已完成
            result.setStatus(DecomposedTask.ExecutionStatus.COMPLETED);
            result.setEndTime(LocalDateTime.now());
            result.setExecutionTimeMillis(
                    java.time.Duration.between(result.getStartTime(), result.getEndTime()).toMillis()
            );
            
            log.info("任务执行成功: {}, 耗时: {}ms", result.getTaskId(), result.getExecutionTimeMillis());
            return result;
        } catch (Exception e) {
            // 设置任务状态为执行失败
            task.setStatus(DecomposedTask.ExecutionStatus.FAILED);
            task.setEndTime(LocalDateTime.now());
            task.setExecutionTimeMillis(
                    java.time.Duration.between(task.getStartTime(), task.getEndTime()).toMillis()
            );
            task.setErrorMessage(e.getMessage());
            
            log.error("任务执行失败: {}, 错误: {}", task.getTaskId(), e.getMessage(), e);
            return task;
        }
    }
    
    /**
     * 异步执行单个任务
     * 
     * @param task 待执行的任务
     * @return 包含执行结果的CompletableFuture
     */
    @Override
    public CompletableFuture<DecomposedTask> executeAsync(DecomposedTask task) {
        return CompletableFuture.supplyAsync(() -> execute(task), executorService);
    }
    
    /**
     * 执行多个任务
     * 会根据任务的依赖关系和优先级进行排序和执行
     * 
     * @param tasks 待执行的任务列表
     * @return 执行后的任务列表
     */
    @Override
    public List<DecomposedTask> executeAll(List<DecomposedTask> tasks) {
        log.info("开始执行任务列表，共{}个任务", tasks.size());
        long startTime = System.nanoTime();

        // 按优先级排序
        List<DecomposedTask> sortedTasks = new ArrayList<>(tasks);
        sortedTasks.sort(Comparator.comparingInt(DecomposedTask::getPriority));

        // 已执行和跳过的任务列表（线程安全）
        Queue<DecomposedTask> executedTasks = new java.util.concurrent.ConcurrentLinkedQueue<>();
        Queue<DecomposedTask> skippedTasks = new java.util.concurrent.ConcurrentLinkedQueue<>();
        Queue<DecomposedTask> pendingTasks = new LinkedList<>(sortedTasks);

        // 任务结果future
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        // 使用外部线程池
        java.util.concurrent.ExecutorService threadPool = ThreadPoolManager.getExecutor();

        while (!pendingTasks.isEmpty()) {
            DecomposedTask task = pendingTasks.poll();
            if (task == null) break;

            // 检查任务是否可以执行
            if (canExecute(task, new ArrayList<>(executedTasks))) {
                // 异步执行任务
                CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                    DecomposedTask executedTask = execute(task);
                    executedTasks.add(executedTask);

                    // 如果任务执行失败，将依赖它的任务标记为跳过（需线程安全处理）
                    if (executedTask.getStatus() == DecomposedTask.ExecutionStatus.FAILED) {
                        synchronized (pendingTasks) {
                            markDependentTasksAsSkipped(pendingTasks, executedTask.getTaskId());
                        }
                    }
                }, threadPool);
                futures.add(future);
            } else {
                // 检查是否由于依赖任务失败而无法执行
                if (hasDependencyFailed(task, new ArrayList<>(executedTasks))) {
                    task.setStatus(DecomposedTask.ExecutionStatus.SKIPPED);
                    task.setErrorMessage("依赖任务执行失败");
                    skippedTasks.add(task);
                } else {
                    // 将任务重新放回队列末尾，等待依赖任务完成
                    pendingTasks.add(task);
                }
            }
        }

        // 等待所有异步任务完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        // 将跳过的任务添加到结果列表
        executedTasks.addAll(skippedTasks);

        long endTime = System.nanoTime();
        long durationMillis = (endTime - startTime) / 1_000_000;

        int totalCount = executedTasks.size();
        log.info("多线程任务执行结束，总数量: {}，总耗时: {}ms", totalCount, durationMillis);

        log.info("任务列表执行完成，成功: {}, 失败: {}, 跳过: {}",
                executedTasks.stream().filter(t -> t.getStatus() == DecomposedTask.ExecutionStatus.COMPLETED).count(),
                executedTasks.stream().filter(t -> t.getStatus() == DecomposedTask.ExecutionStatus.FAILED).count(),
                executedTasks.stream().filter(t -> t.getStatus() == DecomposedTask.ExecutionStatus.SKIPPED).count());

        return new ArrayList<>(executedTasks);
    }
    
    /**
     * 异步执行多个任务
     * 
     * @param tasks 待执行的任务列表
     * @return 包含执行结果的CompletableFuture
     */
    @Override
    public CompletableFuture<List<DecomposedTask>> executeAllAsync(List<DecomposedTask> tasks) {
        return CompletableFuture.supplyAsync(() -> executeAll(tasks), executorService);
    }
    
    /**
     * 检查任务是否可以执行
     * 主要检查依赖任务是否已完成
     * 
     * @param task 待检查的任务
     * @param executedTasks 已执行的任务列表
     * @return 是否可以执行
     */
    @Override
    public boolean canExecute(DecomposedTask task, List<DecomposedTask> executedTasks) {
        // 如果没有依赖，可以直接执行
        if (task.getDependencies() == null || task.getDependencies().length == 0) {
            return true;
        }
        
        // 检查所有依赖任务是否都已完成
        for (String dependencyId : task.getDependencies()) {
            boolean dependencyCompleted = executedTasks.stream()
                    .anyMatch(t -> t.getTaskId().equals(dependencyId) && 
                            t.getStatus() == DecomposedTask.ExecutionStatus.COMPLETED);
            
            if (!dependencyCompleted) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 检查任务的依赖是否有失败的
     * 
     * @param task 待检查的任务
     * @param executedTasks 已执行的任务列表
     * @return 是否有依赖任务失败
     */
    protected boolean hasDependencyFailed(DecomposedTask task, List<DecomposedTask> executedTasks) {
        if (task.getDependencies() == null || task.getDependencies().length == 0) {
            return false;
        }
        
        for (String dependencyId : task.getDependencies()) {
            boolean dependencyFailed = executedTasks.stream()
                    .anyMatch(t -> t.getTaskId().equals(dependencyId) && 
                            (t.getStatus() == DecomposedTask.ExecutionStatus.FAILED || 
                             t.getStatus() == DecomposedTask.ExecutionStatus.SKIPPED));
            
            if (dependencyFailed) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 将依赖指定任务的所有任务标记为跳过
     * 
     * @param pendingTasks 待执行的任务队列
     * @param failedTaskId 失败的任务ID
     */
    protected void markDependentTasksAsSkipped(Queue<DecomposedTask> pendingTasks, String failedTaskId) {
        List<DecomposedTask> tasksToSkip = pendingTasks.stream()
                .filter(task -> task.getDependencies() != null && 
                        Arrays.asList(task.getDependencies()).contains(failedTaskId))
                .toList();
        
        for (DecomposedTask task : tasksToSkip) {
            task.setStatus(DecomposedTask.ExecutionStatus.SKIPPED);
            task.setErrorMessage("依赖任务 " + failedTaskId + " 执行失败");
            pendingTasks.remove(task);
            
            // 递归标记依赖此任务的其他任务
            markDependentTasksAsSkipped(pendingTasks, task.getTaskId());
        }
    }
    
    /**
     * 执行具体任务的抽象方法
     * 由子类实现具体的任务执行逻辑
     * 
     * @param task 待执行的任务
     * @return 执行后的任务
     */
    protected abstract DecomposedTask executeTask(DecomposedTask task);
}