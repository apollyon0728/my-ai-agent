package com.yam.myaiagent.service.impl;

import com.yam.myaiagent.advisor.MyLoggerAdvisor;
import com.yam.myaiagent.agent.model.IChatModelStrategy;
import com.yam.myaiagent.model.QAResponse;
import com.yam.myaiagent.rag.LoveAppDocumentLoader;
import com.yam.myaiagent.rag.QueryRewriter;
import com.yam.myaiagent.service.KnowledgeBaseService;
import com.yam.myaiagent.taskdecompose.DecomposedTask;
import com.yam.myaiagent.taskdecompose.TaskDecomposer;
import com.yam.myaiagent.taskexecutor.CompositeTaskExecutor;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

/**
 * 功能：知识库服务实现类
 * 日期：2025/5/19 20:56
 */
@Service
@Slf4j
public class KnowledgeBaseServiceImpl implements KnowledgeBaseService {

    @Resource
    @Qualifier("pgVectorVectorStore")  // 或 "loveAppVectorStore"
    private VectorStore vectorStore;

    @Resource
    private LoveAppDocumentLoader documentLoader;

    @Resource
    private QueryRewriter queryRewriter;

    @Resource
    private VectorStore pgVectorVectorStore;

    @Resource
    private TaskDecomposer taskDecomposer;
    
    // 修改注入方式，使用@Autowired和@Qualifier组合
    @Autowired
    @Qualifier("compositeTaskExecutor")
    private CompositeTaskExecutor taskExecutor;
    
    // 添加日志记录注入情况
    @PostConstruct
    public void init() {
        log.info("KnowledgeBaseServiceImpl初始化");
        log.info("taskExecutor注入状态: {}", taskExecutor != null ? "成功" : "失败");
        if (taskExecutor != null) {
            log.info("注入的taskExecutor类型: {}", taskExecutor.getClass().getName());
        }
    }

    private static final String MARKDOWN_DIR = "docs/markdown/";

    private final ChatClient chatClient;

    private static final String SYSTEM_PROMPT_NORMAL = "你是一个AI助手，可以帮助用户解答各种问题。";

    /**
     * 新增多模型动态选择问答接口
     * 通过 modelType 参数选择对应模型（alibaba, deepseek等），支持swagger调用
     */
    @Resource
    private Map<String, IChatModelStrategy> modelStrategyMap;


    /**
     * 初始化 ChatClient
     * 知识库服务实现类构造函数
     *
     * @param dashscopeChatModel 通义千问聊天模型实例，用于构建聊天客户端
     *                           alibabaChatModel
     */
    public KnowledgeBaseServiceImpl(@Qualifier("alibabaChatModel") ChatModel dashscopeChatModel) {
        // 初始化基于内存的对话记忆
        ChatMemory chatMemory = new InMemoryChatMemory();

        // 构建聊天客户端，配置系统提示词和默认顾问
        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT_NORMAL)
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(chatMemory),
                        // 自定义日志 Advisor，可按需开启
                        new MyLoggerAdvisor()
//                        // 自定义推理增强 Advisor，可按需开启
//                       ,new ReReadingAdvisor()
                )
                .build();
    }


    /**
     * 上传Markdown文档到知识库
     *
     * @param file
     */
    @Override
    public void uploadMarkdownDocument(MultipartFile file) {
        try {
            // 确保目录存在
            Path dirPath = Paths.get(MARKDOWN_DIR);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }

            // 保存文件
            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
            Path filePath = dirPath.resolve(fileName);
            Files.copy(file.getInputStream(), filePath);

            // 加载新文档并添加到向量存储
            List<Document> documents = documentLoader.loadMarkdownByPath(String.valueOf(filePath.toFile()));

            // FIXME 批量添加文档到向量存储   vectorStore.add(documents)
            addDocuments(documents);
        } catch (IOException e) {
            throw new RuntimeException("文档上传失败", e);
        }
    }

    /**
     * 批量添加文档到向量存储
     */
    @Override
    public void addDocuments(List<Document> documents) {
        vectorStore.add(documents);
    }

    /**
     * 获取问题的回答
     *
     * @param message 用户输入的问题消息
     * @return QAResponse 包含回答内容的响应对象
     */
    @Override
    public QAResponse getAnswer(String message) {
        String chatId = UUID.randomUUID().toString();

        // FIXME 查询重写（主要作用是通过预定义的转换器优化或改写用户的查询语句）
        String rewrittenMessage = queryRewriter.doQueryRewrite(message);

        // 构建聊天请求并获取响应
        ChatResponse chatResponse = chatClient
                .prompt()
                // 使用改写后的查询
                .user(rewrittenMessage)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                // 开启日志，便于观察效果
                .advisors(new MyLoggerAdvisor())
                // FIXME 应用 RAG 检索增强服务（基于 PgVector 向量存储，知识库查询？）
                .advisors(new QuestionAnswerAdvisor(pgVectorVectorStore))
                .call()
                .chatResponse();

        String content = chatResponse.getResult().getOutput().getText();
        log.info("content: {}", content);

        QAResponse qaResponse = new QAResponse();
        qaResponse.setAnswer(content);
        return qaResponse;
    }


    /**
     * 获取问题的回答结果
     *
     * @param message   用户输入的问题消息
     * @param modelType 模型类型，用于选择不同的AI模型策略
     * @return QAResponse 包含AI回答内容的响应对象
     */
    @Override
    public QAResponse getAnswerNew(String message, String modelType) {
        // 生成唯一的对话ID
        String chatId = UUID.randomUUID().toString();

        // FIXME 对用户查询进行重写优化
        String rewrittenMessage = queryRewriter.doQueryRewrite(message);
        log.info("KnowledgeBaseServiceImpl对用户查询进行重写优化 getAnswerNew, 重写前: {}, doQueryRewrite重写后: {}", message, rewrittenMessage);

        // 根据模型类型获取对应的策略，如果不存在则使用默认的alibaba策略
        IChatModelStrategy strategy = modelStrategyMap.getOrDefault(modelType, modelStrategyMap.get("alibaba"));
        ChatClient dynamicChatClient = strategy.getChatClient(); // 策略提供ChatClient

        // 执行AI对话请求
        // 构建并执行聊天请求，获取聊天响应
        // 该代码块主要完成以下功能：
        // 1. 设置用户消息内容
        // 2. 配置对话内存参数，包括对话ID和历史记录检索数量
        // 3. 添加日志记录顾问
        // 4. 添加问答处理顾问
        // 5. 执行调用并获取聊天响应结果
        ChatResponse chatResponse = dynamicChatClient
                .prompt()
                .user(rewrittenMessage)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .advisors(new MyLoggerAdvisor())
                .advisors(new QuestionAnswerAdvisor(pgVectorVectorStore))
                .call()
                .chatResponse();


        // 提取AI回答的内容
        String content = chatResponse != null ? chatResponse.getResult().getOutput().getText() : null;
        log.info("getAnswerNew content: {}", content);

        // 构造并返回响应对象
        QAResponse qaResponse = new QAResponse();
        qaResponse.setAnswer(content);
        return qaResponse;
    }


    @Override
    public List<Document> getDocumentList() {
        // 这里需要实现文档列表的获取逻辑
        // 可以从文件系统或数据库中获取
        return documentLoader.loadMarkdowns();
    }

    @Override
    public void deleteDocument(String documentId) {

    }

    @Override
    public void reloadAllDocuments() {

    }

    /**
     * 使用向量存储进行相似性搜索并回答问题，同时拆解任务
     *
     * @param message   用户输入的问题
     * @param modelType 模型类型
     * @return 包含回答和拆解任务的响应
     */
    @Override
    public QAResponse getAnswerWithTaskDecomposition(String message, String modelType) {
        // 1. 获取问题的回答
        QAResponse qaResponse = getAnswerNew(message, modelType);

        // 2. 拆解任务
        List<DecomposedTask> tasks = decomposeTask(message);

        // 3. 设置任务列表
        qaResponse.setTasks(tasks);

        return qaResponse;
    }

    /**
     * 上传任务拆解规则
     *
     * @param ruleJson 规则JSON字符串
     * @return 上传成功的规则ID
     */
    @Override
    public String uploadTaskRule(String ruleJson) {
        return taskDecomposer.uploadRule(ruleJson);
    }

    /**
     * 获取所有任务拆解规则
     *
     * @return 所有规则的列表
     */
    @Override
    public List<String> getAllTaskRules() {
        return taskDecomposer.getAllRules();
    }

    /**
     * 删除指定任务拆解规则
     *
     * @param ruleId 规则ID
     */
    @Override
    public void deleteTaskRule(String ruleId) {
        taskDecomposer.deleteRule(ruleId);
    }

    /**
     * 拆解任务
     *
     * @param question 用户问题
     * @return 拆解后的任务列表
     */
    @Override
    public List<DecomposedTask> decomposeTask(String question) {
        log.info("KnowledgeBaseServiceImpl.decomposeTask开始执行, 参数: {}", question);
        List<DecomposedTask> tasks = taskDecomposer.decompose(question);
        log.info("KnowledgeBaseServiceImpl.decomposeTask执行完成, 生成任务数量: {}", tasks.size());
        return tasks;
    }
    
    /**
     * 执行拆解后的任务
     *
     * @param tasks 待执行的任务列表
     * @return 执行后的任务列表
     */
    @Override
    public List<DecomposedTask> executeTasks(List<DecomposedTask> tasks) {
        log.info("KnowledgeBaseServiceImpl.executeTasks开始执行, 任务数量: {}", tasks.size());
        
        // 使用组合任务执行器执行所有任务 （目前注入的TaskExecutor是compositeTaskExecutor）
        List<DecomposedTask> executedTasks = taskExecutor.executeAll(tasks);
        
        log.info("KnowledgeBaseServiceImpl.executeTasks执行完成, 成功: {}, 失败: {}, 跳过: {}",
                executedTasks.stream().filter(t -> t.getStatus() == DecomposedTask.ExecutionStatus.COMPLETED).count(),
                executedTasks.stream().filter(t -> t.getStatus() == DecomposedTask.ExecutionStatus.FAILED).count(),
                executedTasks.stream().filter(t -> t.getStatus() == DecomposedTask.ExecutionStatus.SKIPPED).count());
        
        return executedTasks;
    }
    
    /**
     * 拆解并执行任务，然后汇总结果
     * 该方法实现了完整的智能问答处理流程：
     * 1. 任务拆解 - 将复杂问题拆解为多个子任务
     * 2. 任务执行 - 根据任务类型选择合适的执行器（MCP或Function Call）
     * 3. 结果汇总 - 将所有任务的执行结果汇总，生成最终回答
     *
     * @param question 用户问题
     * @param modelType 模型类型
     * @return 包含回答和执行结果的响应
     */
    @Override
    public QAResponse decomposeAndExecuteTasks(String question, String modelType) {
        log.info("KnowledgeBaseServiceImpl.decomposeAndExecuteTasks开始执行, 问题: {}, 模型类型: {}", question, modelType);
        
        // 1. 拆解任务 - 使用knowledgeBaseService.decomposeTask拆解任务
        log.info("步骤1: 任务拆解 - 将复杂问题拆解为多个子任务");
        List<DecomposedTask> tasks = decomposeTask(question);
        if (tasks.isEmpty()) {
            log.warn("任务拆解结果为空，直接使用普通问答");
            return getAnswerNew(question, modelType);
        }
        
        // 记录任务类型分布
        Map<DecomposedTask.TaskType, Long> taskTypeCount = tasks.stream()
                .collect(Collectors.groupingBy(task -> task.getTaskType(), Collectors.counting()));
        log.info("任务类型分布: {}", taskTypeCount);
        
        // 2. 执行任务 - 根据任务类型选择合适的执行器（MCP或Function Call）
        log.info("步骤2: 任务执行 - 根据任务类型选择合适的执行器（MCP或Function Call）");
        List<DecomposedTask> executedTasks = executeTasks(tasks);
        
        // 统计执行结果
        long completedCount = executedTasks.stream()
                .filter(t -> t.getStatus() == DecomposedTask.ExecutionStatus.COMPLETED).count();
        long failedCount = executedTasks.stream()
                .filter(t -> t.getStatus() == DecomposedTask.ExecutionStatus.FAILED).count();
        long skippedCount = executedTasks.stream()
                .filter(t -> t.getStatus() == DecomposedTask.ExecutionStatus.SKIPPED).count();
        log.info("任务执行结果统计: 成功={}, 失败={}, 跳过={}", completedCount, failedCount, skippedCount);
        
        // 3. 汇总结果 - 将所有任务的执行结果汇总，生成最终回答
        log.info("步骤3: 结果汇总 - 将所有任务的执行结果汇总，生成最终回答");
        
        // 构建汇总提示词
        StringBuilder summaryPrompt = new StringBuilder();
        summaryPrompt.append("用户问题：").append(question).append("\n\n");
        summaryPrompt.append("任务执行结果：\n");
        
        // 按任务类型分组展示结果
        Map<DecomposedTask.TaskType, List<DecomposedTask>> tasksByType = executedTasks.stream()
                .collect(Collectors.groupingBy(task -> task.getTaskType()));
        
        // 先展示MCP工具调用结果
        if (tasksByType.containsKey(DecomposedTask.TaskType.MCP_TOOL)) {
            summaryPrompt.append("\n## MCP工具调用结果\n");
            appendTaskResults(summaryPrompt, tasksByType.get(DecomposedTask.TaskType.MCP_TOOL));
        }
        
        // 再展示Function Call工具调用结果
        if (tasksByType.containsKey(DecomposedTask.TaskType.FUNCTION_CALL)) {
            summaryPrompt.append("\n## Function Call工具调用结果\n");
            appendTaskResults(summaryPrompt, tasksByType.get(DecomposedTask.TaskType.FUNCTION_CALL));
        }
        
        // 展示其他类型任务结果
        for (Map.Entry<DecomposedTask.TaskType, List<DecomposedTask>> entry : tasksByType.entrySet()) {
            if (entry.getKey() != DecomposedTask.TaskType.MCP_TOOL &&
                entry.getKey() != DecomposedTask.TaskType.FUNCTION_CALL) {
                summaryPrompt.append("\n## ").append(entry.getKey()).append("结果\n");
                appendTaskResults(summaryPrompt, entry.getValue());
            }
        }
        
        // 从用户问题中提取处理方式，并生成相应的提示词
        String customPrompt = generateCustomPromptFromQuestion(question);
        summaryPrompt.append(customPrompt);
        
        // 4. 使用模型生成最终回答
        String chatId = UUID.randomUUID().toString();
        IChatModelStrategy strategy = modelStrategyMap.getOrDefault(modelType, modelStrategyMap.get("alibaba"));
        ChatClient dynamicChatClient = strategy.getChatClient();
        
        log.info("使用{}模型生成最终回答", modelType);
        ChatResponse chatResponse = dynamicChatClient
                .prompt()
                .user(summaryPrompt.toString())
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .advisors(new MyLoggerAdvisor())
                .call()
                .chatResponse();
        
        String content = chatResponse != null ? chatResponse.getResult().getOutput().getText() : null;
        log.info("汇总分析结果生成完成，长度: {}", content != null ? content.length() : 0);
        
        // 5. 构建响应
        QAResponse qaResponse = new QAResponse();
        qaResponse.setAnswer(content);
        qaResponse.setTasks(executedTasks);
        
        return qaResponse;
    }
    
    /**
     * 从用户问题中提取处理方式，并生成相应的提示词
     * 根据问题中的关键词和语义，识别用户期望的处理方式，并生成相应的提示词
     *
     * @param question 用户问题
     * @return 根据处理方式生成的提示词
     */
    private String generateCustomPromptFromQuestion(String question) {
        log.info("从用户问题中提取处理方式: {}", question);
        
        // 默认提示词
        String defaultPrompt = "\n请根据以上任务执行结果，综合分析并回答用户的问题。提供详细、准确的回答，并引用相关任务的结果作为支持。";
        
        // 如果问题为空，返回默认提示词
        if (question == null || question.trim().isEmpty()) {
            return defaultPrompt;
        }
        
        // 转换为小写，便于匹配关键词
        String lowerQuestion = question.toLowerCase();
        
        // 提取处理方式的规则集
        // 1. 摘要/总结类
        if (lowerQuestion.contains("总结") || lowerQuestion.contains("摘要") ||
            lowerQuestion.contains("概括") || lowerQuestion.contains("简述") ||
            lowerQuestion.contains("归纳")) {
            String prompt = "\n请根据以上任务执行结果，提供一个简洁的总结。重点突出关键信息，使用清晰的结构和要点形式呈现。";
            log.info("匹配到处理方式: 摘要/总结类，生成提示词: {}", prompt);
            return prompt;
        }
        
        // 2. 分析/比较类
        if (lowerQuestion.contains("分析") || lowerQuestion.contains("比较") ||
            lowerQuestion.contains("对比") || lowerQuestion.contains("评估") ||
            lowerQuestion.contains("优缺点")) {
            String prompt = "\n请根据以上任务执行结果，进行深入分析和比较。识别关键差异、优缺点，并提供基于证据的评估。";
            log.info("匹配到处理方式: 分析/比较类，生成提示词: {}", prompt);
            return prompt;
        }
        
        // 3. 建议/推荐类
        if (lowerQuestion.contains("建议") || lowerQuestion.contains("推荐") ||
            lowerQuestion.contains("如何改进") || lowerQuestion.contains("怎么解决") ||
            lowerQuestion.contains("最佳方案")) {
            String prompt = "\n请根据以上任务执行结果，提供具体、可行的建议或推荐。针对用户问题，给出明确的解决方案和实施步骤。";
            log.info("匹配到处理方式: 建议/推荐类，生成提示词: {}", prompt);
            return prompt;
        }
        
        // 4. 预测/趋势类
        if (lowerQuestion.contains("预测") || lowerQuestion.contains("趋势") ||
            lowerQuestion.contains("未来") || lowerQuestion.contains("展望") ||
            lowerQuestion.contains("可能会")) {
            String prompt = "\n请根据以上任务执行结果，分析可能的未来趋势和发展。基于当前数据和模式，提供合理的预测和前瞻性见解。";
            log.info("匹配到处理方式: 预测/趋势类，生成提示词: {}", prompt);
            return prompt;
        }
        
        // 5. 技术说明/教程类
        if (lowerQuestion.contains("如何使用") || lowerQuestion.contains("怎么操作") ||
            lowerQuestion.contains("步骤") || lowerQuestion.contains("教程") ||
            lowerQuestion.contains("指南")) {
            String prompt = "\n请根据以上任务执行结果，提供清晰的技术说明或教程。包含详细步骤、注意事项和最佳实践，确保用户能够轻松理解和操作。";
            log.info("匹配到处理方式: 技术说明/教程类，生成提示词: {}", prompt);
            return prompt;
        }
        
        // 6. 数据统计/图表类
        if (lowerQuestion.contains("统计") || lowerQuestion.contains("数据") ||
            lowerQuestion.contains("图表") || lowerQuestion.contains("百分比") ||
            lowerQuestion.contains("数量")) {
            String prompt = "\n请根据以上任务执行结果，提供数据统计分析。整理关键数字、比例和趋势，并以清晰的方式呈现这些信息。";
            log.info("匹配到处理方式: 数据统计/图表类，生成提示词: {}", prompt);
            return prompt;
        }
        
        // 7. 故障排除/问题解决类
        if (lowerQuestion.contains("故障") || lowerQuestion.contains("错误") ||
            lowerQuestion.contains("问题") || lowerQuestion.contains("修复") ||
            lowerQuestion.contains("解决方案")) {
            String prompt = "\n请根据以上任务执行结果，提供故障排除和问题解决方案。分析可能的原因，并给出具体的修复步骤和预防措施。";
            log.info("匹配到处理方式: 故障排除/问题解决类，生成提示词: {}", prompt);
            return prompt;
        }
        
        // 8. 创意/头脑风暴类
        if (lowerQuestion.contains("创意") || lowerQuestion.contains("想法") ||
            lowerQuestion.contains("创新") || lowerQuestion.contains("头脑风暴") ||
            lowerQuestion.contains("构思")) {
            String prompt = "\n请根据以上任务执行结果，进行创意思考和头脑风暴。提供多样化、创新的想法和可能性，鼓励不同角度的思考。";
            log.info("匹配到处理方式: 创意/头脑风暴类，生成提示词: {}", prompt);
            return prompt;
        }
        
        // 9. 决策支持类
        if (lowerQuestion.contains("决策") || lowerQuestion.contains("选择") ||
            lowerQuestion.contains("应该") || lowerQuestion.contains("是否应该") ||
            lowerQuestion.contains("利弊")) {
            String prompt = "\n请根据以上任务执行结果，提供决策支持分析。评估不同选项的利弊，并基于证据给出明确的建议，帮助用户做出明智决策。";
            log.info("匹配到处理方式: 决策支持类，生成提示词: {}", prompt);
            return prompt;
        }
        
        // 10. 简化/通俗解释类
        if (lowerQuestion.contains("简单解释") || lowerQuestion.contains("通俗") ||
            lowerQuestion.contains("易懂") || lowerQuestion.contains("简化") ||
            lowerQuestion.contains("非专业人士")) {
            String prompt = "\n请根据以上任务执行结果，提供简化和通俗的解释。避免专业术语，使用类比和示例，确保非专业人士也能理解复杂概念。";
            log.info("匹配到处理方式: 简化/通俗解释类，生成提示词: {}", prompt);
            return prompt;
        }
        
        // 如果没有匹配到特定处理方式，返回默认提示词
        log.info("未匹配到特定处理方式，使用默认提示词: {}", defaultPrompt);
        return defaultPrompt;
    }
    
    /**
     * 将任务结果添加到汇总提示词中
     *
     * @param summaryPrompt 汇总提示词构建器
     * @param tasks 任务列表
     */
    private void appendTaskResults(StringBuilder summaryPrompt, List<DecomposedTask> tasks) {
        for (DecomposedTask task : tasks) {
            summaryPrompt.append("- 任务：").append(task.getDescription()).append("\n");
            summaryPrompt.append("  ID：").append(task.getTaskId()).append("\n");
            summaryPrompt.append("  状态：").append(task.getStatus()).append("\n");
            
            if (task.getStatus() == DecomposedTask.ExecutionStatus.COMPLETED) {
                summaryPrompt.append("  结果：").append(task.getResult()).append("\n");
                if (task.getExecutionTimeMillis() > 0) {
                    summaryPrompt.append("  执行时间：").append(task.getExecutionTimeMillis()).append("ms\n");
                }
            } else if (task.getStatus() == DecomposedTask.ExecutionStatus.FAILED) {
                summaryPrompt.append("  错误：").append(task.getErrorMessage()).append("\n");
            } else if (task.getStatus() == DecomposedTask.ExecutionStatus.SKIPPED) {
                summaryPrompt.append("  原因：").append(task.getErrorMessage()).append("\n");
            }
            summaryPrompt.append("\n");
        }
    }
    
    /**
     * 将任务转换为Document对象，用于向量存储
     *
     * @param tasks 任务列表
     * @param question 原始问题
     * @return Document列表
     */
    private List<Document> convertTasksToDocuments(List<DecomposedTask> tasks, String question) {
        List<Document> documents = new ArrayList<>();
        
        // 为每个任务创建一个Document
        for (DecomposedTask task : tasks) {
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("taskId", task.getTaskId());
            metadata.put("taskType", task.getTaskType().name());
            metadata.put("originalQuestion", question);
            metadata.put("timestamp", System.currentTimeMillis());
            metadata.put("status", task.getStatus().name());
            
            // 构建文档内容
            StringBuilder content = new StringBuilder();
            content.append("任务描述: ").append(task.getDescription()).append("\n");
            content.append("任务类型: ").append(task.getTaskType()).append("\n");
            content.append("执行状态: ").append(task.getStatus()).append("\n");
            
            if (task.getStatus() == DecomposedTask.ExecutionStatus.COMPLETED) {
                content.append("执行结果: ").append(task.getResult()).append("\n");
                content.append("执行时间: ").append(task.getExecutionTimeMillis()).append("ms\n");
            } else if (task.getStatus() == DecomposedTask.ExecutionStatus.FAILED) {
                content.append("错误信息: ").append(task.getErrorMessage()).append("\n");
            }
            
            // 创建Document对象
            Document document = new Document(content.toString(), metadata);
            documents.add(document);
        }
        
        // 创建一个汇总Document
        Map<String, Object> summaryMetadata = new HashMap<>();
        summaryMetadata.put("type", "task_summary");
        summaryMetadata.put("originalQuestion", question);
        summaryMetadata.put("timestamp", System.currentTimeMillis());
        summaryMetadata.put("taskCount", tasks.size());
        
        StringBuilder summaryContent = new StringBuilder();
        summaryContent.append("原始问题: ").append(question).append("\n\n");
        summaryContent.append("任务拆解汇总:\n");
        
        for (DecomposedTask task : tasks) {
            summaryContent.append("- ").append(task.getDescription()).append(" (").append(task.getStatus()).append(")\n");
        }
        
        Document summaryDocument = new Document(summaryContent.toString(), summaryMetadata);
        documents.add(summaryDocument);
        
        return documents;
    }
    
    /**
     * 将任务拆解结果存入向量数据库
     *
     * @param tasks 任务列表
     * @param question 原始问题
     * @return 是否成功存储
     */
    @Override
    public boolean saveTasksToVectorStore(List<DecomposedTask> tasks, String question) {
        if (tasks == null || tasks.isEmpty()) {
            log.warn("任务列表为空，无法存入向量数据库");
            return false;
        }
        
        try {
            log.info("开始将任务拆解结果存入向量数据库，任务数量: {}", tasks.size());
            
            // 将任务转换为Document对象
            List<Document> documents = convertTasksToDocuments(tasks, question);
            log.info("任务转换为Document对象完成，文档数量: {}", documents.size());
            
            // 将Document对象添加到向量存储
            vectorStore.add(documents);
            log.info("任务拆解结果成功存入向量数据库");
            
            return true;
        } catch (Exception e) {
            log.error("将任务拆解结果存入向量数据库失败", e);
            return false;
        }
    }
}