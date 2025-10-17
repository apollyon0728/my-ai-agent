package com.yam.myaiagent.agent.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;

/**
 * OpenAI大模型策略实现
 */
@Component("openai")
public class OpenAIChatModelStrategy implements IChatModelStrategy {

    @Autowired
    @Qualifier("openAIChatModel")
    private ChatModel openAIChatModel;

    private volatile ChatClient chatClient;

    @Override
    public ChatClient getChatClient() {
        if (chatClient == null) {
            synchronized (this) {
                if (chatClient == null) {
                    org.slf4j.LoggerFactory.getLogger(OpenAIChatModelStrategy.class).info(
                        "初始化OpenAIChatModelStrategy的ChatClient，使用的模型类型: {}", 
                        openAIChatModel != null ? openAIChatModel.getClass().getName() : "null");
                    chatClient = ChatClient.builder(openAIChatModel).build();
                }
            }
        }
        return chatClient;
    }
}