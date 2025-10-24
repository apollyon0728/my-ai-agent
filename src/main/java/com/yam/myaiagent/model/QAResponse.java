package com.yam.myaiagent.model;

import com.yam.myaiagent.taskdecompose.DecomposedTask;
import lombok.Data;
import java.util.List;

@Data
public class QAResponse {
    private String answer;
    private List<DecomposedTask> tasks;
    
    /**
     * 标识任务拆解结果是否已存入向量数据库
     */
    private boolean savedToVectorStore = false;
    
//    private List<String> references;
//    private Double confidence;

    private String errorMessage;

}