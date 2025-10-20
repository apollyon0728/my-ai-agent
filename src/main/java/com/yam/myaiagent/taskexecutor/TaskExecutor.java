package com.yam.myaiagent.taskexecutor;

import com.yam.myaiagent.taskdecompose.DecomposedTask;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 任务执行器接口
 * 负责执行拆解后的任务，并返回执行结果
 */
public interface TaskExecutor {
    
    /**
     * 执行单个任务
     * 
     * @param task 待执行的任务
     * @return 执行后的任务（包含执行结果和状态）
     */
    DecomposedTask execute(DecomposedTask task);
    
    /**
     * 异步执行单个任务
     * 
     * @param task 待执行的任务
     * @return 包含执行结果的CompletableFuture
     */
    CompletableFuture<DecomposedTask> executeAsync(DecomposedTask task);
    
    /**
     * 执行多个任务
     * 会根据任务的依赖关系和优先级进行排序和执行
     * 
     * @param tasks 待执行的任务列表
     * @return 执行后的任务列表
     */
    List<DecomposedTask> executeAll(List<DecomposedTask> tasks);
    
    /**
     * 异步执行多个任务
     * 会根据任务的依赖关系和优先级进行排序和执行
     * 
     * @param tasks 待执行的任务列表
     * @return 包含执行结果的CompletableFuture
     */
    CompletableFuture<List<DecomposedTask>> executeAllAsync(List<DecomposedTask> tasks);
    
    /**
     * 检查任务是否可以执行
     * 主要检查依赖任务是否已完成
     * 
     * @param task 待检查的任务
     * @param executedTasks 已执行的任务列表
     * @return 是否可以执行
     */
    boolean canExecute(DecomposedTask task, List<DecomposedTask> executedTasks);
}