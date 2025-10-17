package com.yam.myaiagent.agent;

import com.yam.myaiagent.advisor.MyLoggerAdvisor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.stereotype.Component;

/**
 *  AI 超级智能体
 *  这段代码定义了一个名为 [MyManus]的 AI 智能体类，继承自 [ToolCallAgent]。
 *  它在构造函数中设置了系统提示语、下一步操作提示语、最大执行步数，
 *  并初始化了一个带有日志顾问的聊天客户端，用于处理复杂的用户任务并调用工具逐步解决问题。
 */
@Component
public class MyManus extends ToolCallAgent {

    /**
     * 构造函数，初始化 AI 智能体
     * @param allTools 所有工具
     * @param dashscopeChatModel 聊天模型
     */
    public MyManus(ToolCallback[] allTools, ChatModel dashscopeChatModel) {
        super(allTools);
        this.setName("MyManus");

        // 设置系统提示语和下一步操作提示语(你是YuManus，一个全能的人工智能助手，旨在解决用户提出的任何任务。您可以使用各种工具来高效地完成复杂的请求。 “””；)
        String SYSTEM_PROMPT = """
                You are YuManus, an all-capable AI assistant, aimed at solving any task presented by the user.
                You have various tools at your disposal that you can call upon to efficiently complete complex requests.
                """;
        this.setSystemPrompt(SYSTEM_PROMPT);

        // 设置下一步操作提示语(根据用户需求，主动选择最合适的工具或工具组合。对于复杂任务，你可以将问题分解成 smaller problems，并逐步使用不同的工具来解决。在每次使用工具后，请清晰地解释执行结果并建议下一步操作。如果您在任何时候想停止对话，请使用“terminate”工具/功能调用。)
        String NEXT_STEP_PROMPT = """
                Based on user needs, proactively select the most appropriate tool or combination of tools.
                For complex tasks, you can break down the problem and use different tools step by step to solve it.
                After using each tool, clearly explain the execution results and suggest the next steps.
                If you want to stop the interaction at any point, use the `terminate` tool/function call.
                """;
        this.setNextStepPrompt(NEXT_STEP_PROMPT);
        this.setMaxSteps(10);

        // 初始化 AI 对话客户端
        ChatClient chatClient = ChatClient.builder(dashscopeChatModel)
                .defaultAdvisors(new MyLoggerAdvisor())
                .build();
        this.setChatClient(chatClient);
    }
}

