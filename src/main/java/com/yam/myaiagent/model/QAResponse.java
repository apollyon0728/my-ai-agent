package com.yam.myaiagent.model;

import com.yam.myaiagent.taskdecompose.DecomposedTask;
import lombok.Data;
import java.util.List;

@Data
public class QAResponse {
    private String answer;
    private List<DecomposedTask> tasks;
//    private List<String> references;
//    private Double confidence;
}