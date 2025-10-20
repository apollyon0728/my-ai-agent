package com.yam.myaiagent.service;

import com.yam.myaiagent.model.QAResponse;
import com.yam.myaiagent.taskdecompose.DecomposedTask;
import com.yam.myaiagent.taskdecompose.TaskRule;
import org.springframework.ai.document.Document;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;

public interface KnowledgeBaseService {
    /**
     * 上传Markdown文档到知识库
     */
    void uploadMarkdownDocument(MultipartFile file);

    /**
     * 批量添加文档到向量存储
     */
    void addDocuments(List<Document> documents);

    /**
     * 使用向量存储进行相似性搜索并回答问题
     */
    QAResponse getAnswer(String question);

    /**
     * 使用向量存储进行相似性搜索并回答问题
     * @param message 用户输入的问题
     *                modelType 模型类型
     */
    QAResponse getAnswerNew(String message, String modelType);
    
    /**
     * 使用向量存储进行相似性搜索并回答问题，同时拆解任务
     * @param message 用户输入的问题
     * @param modelType 模型类型
     * @return 包含回答和拆解任务的响应
     */
    QAResponse getAnswerWithTaskDecomposition(String message, String modelType);

    /**
     * 获取所有文档列表
     */
    List<Document> getDocumentList();

    /**
     * 删除指定文档
     */
    void deleteDocument(String documentId);

    /**
     * 重新加载所有Markdown文档
     */
    void reloadAllDocuments();
    
    /**
     * 上传任务拆解规则
     * @param ruleJson 规则JSON字符串
     * @return 上传成功的规则ID
     */
    String uploadTaskRule(String ruleJson);
    
    /**
     * 获取所有任务拆解规则
     * @return 所有规则的列表
     */
    List<String> getAllTaskRules();
    
    /**
     * 删除指定任务拆解规则
     * @param ruleId 规则ID
     */
    void deleteTaskRule(String ruleId);
    
    /**
     * 拆解任务
     * @param question 用户问题
     * @return 拆解后的任务列表
     */
    List<DecomposedTask> decomposeTask(String question);
    
    /**
     * 执行拆解后的任务
     * @param tasks 待执行的任务列表
     * @return 执行后的任务列表
     */
    List<DecomposedTask> executeTasks(List<DecomposedTask> tasks);
    
    /**
     * 拆解并执行任务，然后汇总结果
     * @param question 用户问题
     * @param modelType 模型类型
     * @return 包含回答和执行结果的响应
     */
    QAResponse decomposeAndExecuteTasks(String question, String modelType);
}