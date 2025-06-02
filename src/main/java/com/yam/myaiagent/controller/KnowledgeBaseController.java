package com.yam.myaiagent.controller;

import com.yam.myaiagent.model.QARequest;
import com.yam.myaiagent.model.QAResponse;
import com.yam.myaiagent.service.KnowledgeBaseService;
import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeBaseController {

    @Resource
    private KnowledgeBaseService knowledgeBaseService;

    /**
     * 上传Markdown文档到知识库
     */
    @PostMapping("/upload")
    public ResponseEntity<String> uploadDocument(@RequestParam("file") MultipartFile file) {
        knowledgeBaseService.uploadMarkdownDocument(file);
        return ResponseEntity.ok("文档上传成功");
    }

    /**
     * 基于知识库进行问答
     */


    @PostMapping("/qa")
    public ResponseEntity<QAResponse> askQuestion(@RequestBody QARequest request) {
        QAResponse response = knowledgeBaseService.getAnswer(request.getQuestion());
        return ResponseEntity.ok(response);
    }

    /**
     * 获取所有已上传的文档列表
     */
    @GetMapping("/documents")
    public ResponseEntity<List<Document>> getDocumentList() {
        List<Document> documents = knowledgeBaseService.getDocumentList();
        return ResponseEntity.ok(documents);
    }

    /**
     * 删除指定文档
     */
    @DeleteMapping("/documents/{documentId}")
    public ResponseEntity<String> deleteDocument(@PathVariable String documentId) {
        knowledgeBaseService.deleteDocument(documentId);
        return ResponseEntity.ok("文档删除成功");
    }

    /**
     * 重新加载所有文档
     */
    @PostMapping("/reload")
    public ResponseEntity<String> reloadDocuments() {
        knowledgeBaseService.reloadAllDocuments();
        return ResponseEntity.ok("文档重新加载成功");
    }
} 