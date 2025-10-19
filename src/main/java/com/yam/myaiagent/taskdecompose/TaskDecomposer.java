package com.yam.myaiagent.taskdecompose;

import java.util.List;

/**
 * 任务拆解器接口
 * 负责将用户输入的问题拆解为具体的任务列表
 */
public interface TaskDecomposer {
    
    /**
     * 将问题拆解为具体任务列表
     * 
     * @param question 用户输入的问题
     * @return 拆解后的任务列表
     */
    List<DecomposedTask> decompose(String question);
    
    /**
     * 上传拆解规则到向量数据库
     * 
     * @param ruleJson 规则JSON字符串
     * @return 上传成功的规则ID
     */
    String uploadRule(String ruleJson);
    
    /**
     * 获取所有拆解规则
     * 
     * @return 所有规则的列表
     */
    List<String> getAllRules();
    
    /**
     * 删除指定规则
     * 
     * @param ruleId 规则ID
     */
    void deleteRule(String ruleId);
}