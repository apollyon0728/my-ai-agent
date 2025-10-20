package com.yam.myaiagent.taskexecutor;

import com.yam.myaiagent.taskdecompose.DecomposedTask;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/**
 * 组合任务执行器
 * 根据任务类型选择合适的执行器来执行任务
 */
@Service("compositeTaskExecutor")
@Slf4j
public class CompositeTaskExecutor extends AbstractTaskExecutor {

    @PostConstruct
    public void init() {
        log.info("CompositeTaskExecutor初始化");
        log.info("Bean名称: compositeTaskExecutor");
        log.info("Bean类型: {}", this.getClass().getName());
    }

    // 任务类型到执行器的映射
    private final Map<DecomposedTask.TaskType, TaskExecutor> executorMap = new HashMap<>();
    
    // 默认执行器，用于处理未注册类型的任务
    private TaskExecutor defaultExecutor;
    
    /**
     * 构造函数
     * 注入所有可用的任务执行器
     */
    @Autowired
    public CompositeTaskExecutor(
            @Autowired(required = false) McpTaskExecutor mcpTaskExecutor,
            @Autowired(required = false) FunctionCallTaskExecutor functionCallTaskExecutor) {
        super();
        
        // 注册MCP任务执行器
        if (mcpTaskExecutor != null) {
            registerExecutor(DecomposedTask.TaskType.MCP_TOOL, mcpTaskExecutor);
            log.info("已注册MCP任务执行器");
        }
        
        // 注册Function Call任务执行器
        if (functionCallTaskExecutor != null) {
            registerExecutor(DecomposedTask.TaskType.FUNCTION_CALL, functionCallTaskExecutor);
            log.info("已注册Function Call任务执行器");
        }
        
        // 设置默认执行器为自身，处理未注册类型的任务
        this.defaultExecutor = new DefaultTaskExecutor();
        log.info("已设置默认任务执行器");
    }
    
    /**
     * 注册任务执行器
     * 
     * @param taskType 任务类型
     * @param executor 对应的执行器
     */
    public void registerExecutor(DecomposedTask.TaskType taskType, TaskExecutor executor) {
        executorMap.put(taskType, executor);
        log.info("注册任务执行器: {} -> {}", taskType, executor.getClass().getSimpleName());
    }
    
    /**
     * 执行具体任务
     * 根据任务类型选择合适的执行器
     * 
     * @param task 待执行的任务
     * @return 执行后的任务
     */
    @Override
    protected DecomposedTask executeTask(DecomposedTask task) {
        // 根据任务类型获取对应的执行器
        TaskExecutor executor = executorMap.getOrDefault(task.getTaskType(), defaultExecutor);
        
        log.info("使用执行器 {} 执行任务 {}", executor.getClass().getSimpleName(), task.getTaskId());
        
        // 使用选择的执行器执行任务
        return executor.execute(task);
    }
    
    /**
     * 默认任务执行器
     * 用于处理未注册类型的任务
     */
    private class DefaultTaskExecutor extends AbstractTaskExecutor {
        
        @Override
        protected DecomposedTask executeTask(DecomposedTask task) {
            log.warn("使用默认执行器处理未注册类型的任务: {}, 类型: {}", task.getTaskId(), task.getTaskType());
            
            // 设置错误信息
            task.setErrorMessage("未找到对应类型的任务执行器: " + task.getTaskType());
            task.setStatus(DecomposedTask.ExecutionStatus.FAILED);
            
            return task;
        }
    }
}