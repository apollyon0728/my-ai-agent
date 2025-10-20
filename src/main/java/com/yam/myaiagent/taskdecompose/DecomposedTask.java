package com.yam.myaiagent.taskdecompose;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * 拆解后的任务实体类
 * 包含任务描述、查询类型、参数等信息
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DecomposedTask {
    
    /**
     * 任务ID
     */
    private String taskId;
    
    /**
     * 任务描述
     */
    private String description;
    
    /**
     * 查询类型
     * 例如：SQL_QUERY, API_CALL, TEXT_GENERATION等
     */
    private TaskType taskType;
    
    /**
     * 查询模板
     * 对于SQL_QUERY类型，这里存储SQL模板
     * 对于API_CALL类型，这里存储API路径
     */
    private String queryTemplate;
    
    /**
     * 查询参数
     * 键值对形式存储参数名和参数值
     */
    private Map<String, Object> parameters;
    
    /**
     * 优先级
     * 数值越小优先级越高
     */
    private int priority;
    
    /**
     * 依赖的任务ID列表
     * 如果为空，表示没有依赖
     */
    private String[] dependencies;
    
    /**
     * 执行结果
     * 存储任务执行的结果数据
     */
    private Object result;
    
    /**
     * 执行状态
     * 表示任务的执行状态
     */
    private ExecutionStatus status;
    
    /**
     * 错误信息
     * 当任务执行失败时，存储错误信息
     */
    private String errorMessage;
    
    /**
     * 开始执行时间
     */
    private LocalDateTime startTime;
    
    /**
     * 结束执行时间
     */
    private LocalDateTime endTime;
    
    /**
     * 执行耗时（毫秒）
     */
    private long executionTimeMillis;
    
    /**
     * 任务类型枚举
     */
    public enum TaskType {
        /**
         * SQL查询
         */
        SQL_QUERY,
        
        /**
         * API调用
         */
        API_CALL,
        
        /**
         * 文本生成
         */
        TEXT_GENERATION,
        
        /**
         * 数据分析
         */
        DATA_ANALYSIS,
        
        /**
         * MCP工具调用
         */
        MCP_TOOL,
        
        /**
         * Function Call工具调用
         */
        FUNCTION_CALL
    }
    
    /**
     * 执行状态枚举
     */
    public enum ExecutionStatus {
        /**
         * 未执行
         */
        NOT_STARTED,
        
        /**
         * 执行中
         */
        IN_PROGRESS,
        
        /**
         * 已完成
         */
        COMPLETED,
        
        /**
         * 执行失败
         */
        FAILED,
        
        /**
         * 已跳过（依赖任务失败导致）
         */
        SKIPPED
    }
}