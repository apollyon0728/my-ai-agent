package com.yam.myaiagent.service.impl;

import com.yam.myaiagent.advisor.MyLoggerAdvisor;
import com.yam.myaiagent.agent.model.IChatModelStrategy;
import com.yam.myaiagent.model.QAResponse;
import com.yam.myaiagent.rag.LoveAppDocumentLoader;
import com.yam.myaiagent.rag.QueryRewriter;
import com.yam.myaiagent.service.KnowledgeBaseService;
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
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
     * alibabaChatModel
     */
//    public KnowledgeBaseServiceImpl(@Qualifier("alibabaChatModel") ChatModel dashscopeChatModel) {
    public KnowledgeBaseServiceImpl(ChatModel dashscopeChatModel) {
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
            addDocuments(documents);
        } catch (IOException e) {
            throw new RuntimeException("文档上传失败", e);
        }
    }

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
        // 对用户查询进行重写优化
        String rewrittenMessage = queryRewriter.doQueryRewrite(message);

        // 根据模型类型获取对应的策略，如果不存在则使用默认的alibaba策略
        IChatModelStrategy strategy = modelStrategyMap.getOrDefault(modelType, modelStrategyMap.get("alibaba"));
        ChatClient dynamicChatClient = strategy.getChatClient(); // 策略提供ChatClient

        // 执行AI对话请求
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
}