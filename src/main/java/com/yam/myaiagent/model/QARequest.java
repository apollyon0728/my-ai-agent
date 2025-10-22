package com.yam.myaiagent.model;

import lombok.Data;

@Data
public class QARequest {
    /**
     * 问题内容
     * 用于存储待处理的问题文本
     */
    private String question;

    /**
     * 模型类型
     * 用于标识使用的AI模型类型
     */
    private String modelType;

    /**
     * 是否将任务拆解结果存入向量数据库
     * 默认为false，不存储
     */
    private boolean saveToVectorStore = false;
    
    /**
     * 用户指定的分析处理指令
     * 用于指导系统如何分析和处理任务执行结果
     * 如果为空，系统将从问题中自动提取处理方式
     */
    private String analysisInstruction;
}