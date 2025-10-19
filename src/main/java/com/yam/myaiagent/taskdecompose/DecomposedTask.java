package com.yam.myaiagent.taskdecompose;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
        DATA_ANALYSIS
    }
}