package com.yam.myaiagent.model;

import lombok.Data;

@Data
public class QARequest {
    private String question;

    private String modelType;
    
    /**
     * 是否将任务拆解结果存入向量数据库
     * 默认为false，不存储
     */
    private boolean saveToVectorStore = false;
}