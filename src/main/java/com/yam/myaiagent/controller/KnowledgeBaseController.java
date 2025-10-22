package com.yam.myaiagent.controller;

import com.yam.myaiagent.model.QARequest;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
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
    
    /**
     * 拆解并执行任务，然后汇总结果
     * 该接口会先使用knowledgeBaseService.decomposeTask拆解任务，
     * 然后根据不同的任务类型（MCP_TOOL或FUNCTION_CALL）调用相应的工具来分步骤解决问题，
     * 最后将分步的结果汇总进行问题分析
     *
     * 处理流程：
     * 1. 拆解任务 - 将复杂问题拆解为多个子任务
     * 2. 执行任务 - 根据任务类型选择合适的执行器（MCP或Function Call）
     * 3. 汇总结果 - 将所有任务的执行结果汇总，生成最终回答
     *
     * @param request 包含问题的请求
     * @return 包含回答和执行结果的响应
     */
    @PostMapping("/decomposeAndExecuteTasks")
    public ResponseEntity<QAResponse> decomposeAndExecuteTasks(@RequestBody QARequest request) {
        // 调用服务层方法，完成任务拆解、执行和结果汇总
        QAResponse response = knowledgeBaseService.decomposeAndExecuteTasks(request.getQuestion(), request.getModelType());
        return ResponseEntity.ok(response);
    }
    
    /**
     * 增强版智能问答处理流程
     * 该接口提供完整的智能问答处理流程，包括任务拆解、执行和结果汇总，并支持向量数据库存储
     *
     * 处理流程：
     * 1. 先使用knowledgeBaseService.decomposeTask拆解任务
     * 2. 根据不同的任务类型调用function call工具或MCP来分步骤解决问题
     * 3. 将分步的结果汇总进行问题分析
     * 4. 可选：将任务拆解结果存入向量数据库，用于后续相似问题的快速检索
     *
     * @param request 包含问题的请求
     * @return 包含回答和执行结果的响应
     */
    @PostMapping("/enhancedSmartQA")
    public ResponseEntity<QAResponse> enhancedSmartQA(@RequestBody QARequest request) {
        log.info("KnowledgeBaseController.enhancedSmartQA开始执行, 问题: {}, 模型类型: {}, 是否存储到向量库: {}",
                request.getQuestion(), request.getModelType(), request.isSaveToVectorStore());
        
        // FIXME 1. 拆解任务
        log.info("步骤1: 任务拆解 - 将复杂问题拆解为多个子任务");
        List<DecomposedTask> tasks = knowledgeBaseService.decomposeTask(request.getQuestion());
        log.info("任务拆解完成，共拆解出{}个任务", tasks.size());
        
        // FIXME 2. 执行任务
        log.info("步骤2: 任务执行 - 根据任务类型选择合适的执行器（MCP或Function Call）");
        List<DecomposedTask> executedTasks = knowledgeBaseService.executeTasks(tasks);
        
        // FIXME 统计执行结果
        long completedCount = executedTasks.stream()
                .filter(t -> t.getStatus() == DecomposedTask.ExecutionStatus.COMPLETED).count();
        long failedCount = executedTasks.stream()
                .filter(t -> t.getStatus() == DecomposedTask.ExecutionStatus.FAILED).count();
        long skippedCount = executedTasks.stream()
                .filter(t -> t.getStatus() == DecomposedTask.ExecutionStatus.SKIPPED).count();
        log.info("任务执行结果统计: 成功={}, 失败={}, 跳过={}", completedCount, failedCount, skippedCount);
        
        // 3. 汇总结果
        log.info("步骤3: 结果汇总 - 将所有任务的执行结果汇总，生成最终回答");
        QAResponse response = knowledgeBaseService.decomposeAndExecuteTasks(request.getQuestion(), request.getModelType());
        
        // 4. 如果请求中指定了存储标志，则将任务拆解结果存入向量数据库
        if (request.isSaveToVectorStore() && !tasks.isEmpty()) {
            log.info("步骤4: 向量存储 - 将任务拆解结果存入向量数据库");
            
            // 调用服务层方法将任务拆解结果存入向量数据库
            boolean saveResult = knowledgeBaseService.saveTasksToVectorStore(executedTasks, request.getQuestion());
            
            if (saveResult) {
                log.info("任务拆解结果成功存入向量数据库");
                // 设置响应中的标志位
                response.setSavedToVectorStore(true);
            } else {
                log.warn("任务拆解结果存入向量数据库失败");
                response.setSavedToVectorStore(false);
            }
        }
        
        log.info("KnowledgeBaseController.enhancedSmartQA执行完成");
        return ResponseEntity.ok(response);
    }
    
    /**
     * 智能问答处理流程
     * 该接口提供完整的智能问答处理流程，包括任务拆解、执行和结果汇总
     *
     * @param request 包含问题的请求
     * @return 包含回答和执行结果的响应
     */
    @PostMapping("/smartQA")
    public ResponseEntity<QAResponse> smartQA(@RequestBody QARequest request) {
        // 1. 拆解任务
        List<DecomposedTask> tasks = knowledgeBaseService.decomposeTask(request.getQuestion());
        
        // 2. 执行任务
        List<DecomposedTask> executedTasks = knowledgeBaseService.executeTasks(tasks);
        
        // 3. 汇总结果
        QAResponse response = knowledgeBaseService.decomposeAndExecuteTasks(request.getQuestion(), request.getModelType());
        
        return ResponseEntity.ok(response);
    }
}