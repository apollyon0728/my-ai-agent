package com.yam.myaiagent.controller;

import com.yam.myaiagent.model.QARequest;
import com.yam.myaiagent.model.QAResponse;
import com.yam.myaiagent.service.KnowledgeBaseService;
import com.yam.myaiagent.taskdecompose.DecomposedTask;
import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 知识库控制器类
 * 该类负责处理知识库相关的REST API请求，提供知识库的增删改查等操作接口。
 * 所有接口都映射到"/api/knowledge"路径下。
 */
@RestController
@RequestMapping("/api/knowledge")
public class KnowledgeBaseController {


    @Resource
    private KnowledgeBaseService knowledgeBaseService;

    /**
     * 上传Markdown文档到知识库
     * （加载新文档并添加到向量存储 postgresSQL存储向量化数据）
     */
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> uploadDocument(@RequestParam("file") MultipartFile file) {
        // FIXME 加载新文档并添加到向量存储 postgresSQL存储向量化数据
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
     * 基于知识库进行问答
     */
    @PostMapping("/qaNew")
    public ResponseEntity<QAResponse> askQuestionNew(@RequestBody QARequest request) {
        QAResponse response = knowledgeBaseService.getAnswerNew(request.getQuestion(), request.getModelType());
        return ResponseEntity.ok(response);
    }
    
    /**
     * 基于知识库进行问答并拆解任务
     * 该接口在回答问题的同时，会将用户问题拆解为具体任务列表
     */
    @PostMapping("/qaWithTaskDecomposition")
    public ResponseEntity<QAResponse> askQuestionWithTaskDecomposition(@RequestBody QARequest request) {
        QAResponse response = knowledgeBaseService.getAnswerWithTaskDecomposition(request.getQuestion(), request.getModelType());
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
    /**
     * 任务拆解相关接口
     */
    
    /**
     * 上传任务拆解规则
     * @param ruleJson 规则JSON字符串
     * @return 上传成功的规则ID
     */
    @PostMapping("/taskRule")
    public ResponseEntity<String> uploadTaskRule(@RequestBody String ruleJson) {
        String ruleId = knowledgeBaseService.uploadTaskRule(ruleJson);
        return ResponseEntity.ok(ruleId);
    }
    
    /**
     * 获取所有任务拆解规则
     * @return 所有规则的列表
     */
    @GetMapping("/taskRules")
    public ResponseEntity<List<String>> getAllTaskRules() {
        List<String> rules = knowledgeBaseService.getAllTaskRules();
        return ResponseEntity.ok(rules);
    }
    
    /**
     * 删除指定任务拆解规则
     * @param ruleId 规则ID
     * @return 操作结果
     */
    @DeleteMapping("/taskRule/{ruleId}")
    public ResponseEntity<String> deleteTaskRule(@PathVariable String ruleId) {
        knowledgeBaseService.deleteTaskRule(ruleId);
        return ResponseEntity.ok("规则删除成功");
    }
    
    /**
     * 拆解任务
     * @param request 包含问题的请求
     * @return 拆解后的任务列表
     */
    @PostMapping("/decomposeTask")
    public ResponseEntity<List<DecomposedTask>> decomposeTask(@RequestBody QARequest request) {
        List<DecomposedTask> tasks = knowledgeBaseService.decomposeTask(request.getQuestion());
        return ResponseEntity.ok(tasks);
    }
}