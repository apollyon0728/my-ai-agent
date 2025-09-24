package com.yam.myaiagent.app;

import com.yam.myaiagent.advisor.MyLoggerAdvisor;
import com.yam.myaiagent.rag.QueryRewriter;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.client.advisor.api.Advisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;

import java.util.List;

import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_CONVERSATION_ID_KEY;
import static org.springframework.ai.chat.client.advisor.AbstractChatMemoryAdvisor.CHAT_MEMORY_RETRIEVE_SIZE_KEY;

/**
 * Java面试大师应用
 * 专注于Java技术面试指导和问题解答
 * 
 * @author cocoyu
 * @date 2024-09-24
 */
@Component
@Slf4j
public class JavaInterviewApp {

    private final ChatClient chatClient;
    
    // 系统全局提示词 - Java面试专家
    private static final String SYSTEM_PROMPT = """
            你是一位资深的Java技术专家和面试官，拥有15年以上的Java开发和面试经验。
            你的专业领域包括但不限于：
            
            【核心技术领域】
            • Java基础：面向对象、集合框架、多线程、IO/NIO、反射、注解
            • JVM原理：内存模型、垃圾回收、类加载机制、性能调优
            • Spring生态：Spring Framework、Spring Boot、Spring Cloud、Spring Security
            • 数据库技术：MySQL优化、Redis缓存、MongoDB、事务管理
            • 微服务架构：分布式系统、消息队列、服务治理、容器化部署
            • 设计模式：23种设计模式的应用场景和实现
            • 算法与数据结构：常见算法、复杂度分析、leetcode经典题目
            
            【面试指导原则】
            1. 根据候选人水平（初级/中级/高级）调整问题难度
            2. 既要考察理论知识，也要关注实际项目经验
            3. 提供标准答案的同时，给出面试技巧和注意事项
            4. 针对常见面试陷阱给出应对策略
            5. 结合最新技术趋势，保持面试内容的时效性
            
            【交互方式】
            • 可以模拟真实面试场景，进行问答练习
            • 提供详细的技术解析和最佳实践建议
            • 根据简历背景定制个性化面试准备方案
            • 分析面试失败原因，给出改进建议
            
            请始终保持专业、耐心、鼓励的态度，帮助面试者提升技术能力和面试表现。
            """;

    /**
     * 初始化 ChatClient
     *
     * @param dashscopeChatModel 阿里云大模型
     */
    public JavaInterviewApp(ChatModel dashscopeChatModel) {
        // 初始化基于内存的对话记忆
        ChatMemory chatMemory = new InMemoryChatMemory();
        chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultSystem(SYSTEM_PROMPT)
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(chatMemory),
                        // 自定义日志 Advisor
                        new MyLoggerAdvisor()
                )
                .build();
    }

    /**
     * Java面试基础对话（支持多轮对话记忆）
     *
     * @param message 用户消息
     * @param chatId 会话ID
     * @return 回复内容
     */
    public String doChat(String message, String chatId) {
        ChatResponse chatResponse = chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("Java面试大师回复: {}", content);
        return content;
    }

    /**
     * Java面试报告结构化输出
     */
    public record JavaInterviewReport(
            String candidateName,           // 候选人姓名
            String interviewLevel,          // 面试级别（初级/中级/高级）
            List<String> strengths,         // 技术优势
            List<String> weaknesses,        // 需要改进的地方
            List<String> recommendations,   // 学习建议
            String overallAssessment       // 整体评价
    ) {}

    /**
     * Java面试SSE流式对话
     *
     * @param message 用户消息
     * @param chatId 会话ID
     * @return 流式响应
     */
    public Flux<String> doChatByStream(String message, String chatId) {
        return chatClient
                .prompt()
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .stream()
                .content();
    }

    /**
     * 生成Java面试评估报告（结构化输出）
     *
     * @param message 面试内容
     * @param chatId 会话ID
     * @return 面试报告
     */
    public JavaInterviewReport generateInterviewReport(String message, String chatId) {
        JavaInterviewReport report = chatClient
                .prompt()
                .system(SYSTEM_PROMPT + """
                        
                        请根据面试对话内容，生成一份详细的Java技术面试评估报告。
                        报告应包含：候选人技术水平评估、优势分析、不足之处、改进建议等。
                        请确保评估客观公正，建议具有可操作性。
                        """)
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .call()
                .entity(JavaInterviewReport.class);
        log.info("生成Java面试报告: {}", report);
        return report;
    }

    // Java面试知识库问答功能
    @Resource
    private VectorStore pgVectorVectorStore;

    @Resource
    private QueryRewriter queryRewriter;

    /**
     * 基于Java知识库的问答
     *
     * @param message 技术问题
     * @param chatId 会话ID
     * @return 详细解答
     */
    public String doChatWithJavaKnowledge(String message, String chatId) {
        // 查询重写 - 优化技术问题的检索效果
        String rewrittenMessage = queryRewriter.doQueryRewrite(message);
        ChatResponse chatResponse = chatClient
                .prompt()
                .system(SYSTEM_PROMPT + """
                        
                        现在你需要结合Java技术知识库来回答问题。
                        请确保答案准确、详细，并提供实际的代码示例。
                        如果是面试相关问题，请同时给出面试技巧。
                        """)
                .user(rewrittenMessage)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .advisors(new MyLoggerAdvisor())
                // 应用RAG检索增强
                .advisors(new QuestionAnswerAdvisor(pgVectorVectorStore))
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("Java知识库问答结果: {}", content);
        return content;
    }

    // Java面试工具调用能力
    @Resource
    private ToolCallback[] allTools;

    /**
     * Java面试功能（支持调用工具）
     * 可以调用各种工具来辅助面试，如生成代码、查询资料等
     *
     * @param message 用户消息
     * @param chatId 会话ID
     * @return 回复内容
     */
    public String doChatWithTools(String message, String chatId) {
        ChatResponse chatResponse = chatClient
                .prompt()
                .system(SYSTEM_PROMPT + """
                        
                        你现在可以使用各种工具来辅助Java面试过程：
                        • 可以生成代码示例和技术文档
                        • 可以搜索最新的Java技术资料
                        • 可以创建面试题目和答案文档
                        • 可以进行在线编程练习
                        
                        请根据需要合理使用工具，提供更好的面试辅导体验。
                        """)
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .advisors(new MyLoggerAdvisor())
                .tools(allTools)
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("Java面试工具调用结果: {}", content);
        return content;
    }

    // MCP服务调用
    @Resource
    private ToolCallbackProvider toolCallbackProvider;

    /**
     * Java面试功能（调用MCP服务）
     *
     * @param message 用户消息
     * @param chatId 会话ID
     * @return 回复内容
     */
    public String doChatWithMcp(String message, String chatId) {
        ChatResponse chatResponse = chatClient
                .prompt()
                .system(SYSTEM_PROMPT + """
                        
                        现在可以通过MCP服务获取更多Java技术资源：
                        • 获取最新的Java框架信息
                        • 查询开源项目和代码示例
                        • 获取技术社区的热门讨论
                        
                        请充分利用这些资源，为面试者提供最新、最准确的技术指导。
                        """)
                .user(message)
                .advisors(spec -> spec.param(CHAT_MEMORY_CONVERSATION_ID_KEY, chatId)
                        .param(CHAT_MEMORY_RETRIEVE_SIZE_KEY, 10))
                .advisors(new MyLoggerAdvisor())
                .tools(toolCallbackProvider)
                .call()
                .chatResponse();
        String content = chatResponse.getResult().getOutput().getText();
        log.info("Java面试MCP服务调用结果: {}", content);
        return content;
    }
}
