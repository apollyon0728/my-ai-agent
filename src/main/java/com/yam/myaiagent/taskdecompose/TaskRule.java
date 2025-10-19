package com.yam.myaiagent.taskdecompose;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * 任务拆解规则
 * 用于存储在向量数据库中，作为问题拆解的依据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@Slf4j
public class TaskRule {
    
    /**
     * 规则ID
     */
    private String ruleId;
    
    /**
     * 规则名称
     */
    private String ruleName;
    
    /**
     * 规则描述
     */
    private String description;
    
    /**
     * 匹配模式
     * 可以是关键词、正则表达式或语义描述
     */
    private String matchPattern;
    
    /**
     * 匹配模式列表
     * 用于存储多个匹配模式
     */
    private List<String> matchPatterns;
    
    /**
     * 匹配类型
     */
    private MatchType matchType;
    
    /**
     * 匹配阈值
     * 对于语义匹配，表示相似度阈值
     */
    private double threshold;
    
    /**
     * 任务模板列表
     * 当规则匹配成功时，生成的任务列表
     */
    private List<TaskTemplate> taskTemplates;
    
    /**
     * 匹配类型枚举
     */
    public enum MatchType {
        /**
         * 关键词匹配
         */
        KEYWORD,
        
        /**
         * 正则表达式匹配
         */
        REGEX,
        
        /**
         * 语义相似度匹配
         */
        SEMANTIC
    }
    
    /**
     * 任务模板
     * 用于生成具体的任务
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TaskTemplate {
        /**
         * 任务描述模板
         */
        private String descriptionTemplate;
        
        /**
         * 任务类型
         */
        private DecomposedTask.TaskType taskType;
        
        /**
         * 查询模板
         */
        private String queryTemplate;
        
        /**
         * 参数映射
         * 键为参数名，值为参数提取规则
         */
        private Map<String, String> parameterMappings;
        
        /**
         * 优先级
         */
        private int priority;
        
        /**
         * 依赖任务索引
         * 数组中存储依赖的任务在taskTemplates中的索引
         */
        private int[] dependencyIndices;
    }
}