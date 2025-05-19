package com.yam.myaiagent.service;

import com.yam.myaiagent.model.QAResponse;
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
} 