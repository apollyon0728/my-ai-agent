package com.yam.myaiagent.agent.model;

import org.springframework.stereotype.Component;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.annotation.Qualifier;
import jakarta.annotation.Resource;

/**
 * 阿里大模型策略实现
 */
@Component("alibaba")
public class AlibabaChatModelStrategy implements IChatModelStrategy {

    @Resource
    @Qualifier("alibabaChatModel")
    private ChatModel alibabaChatModel;

    private volatile ChatClient chatClient;

    @Override
    public ChatClient getChatClient() {
        if (chatClient == null) {
            synchronized (this) {
                if (chatClient == null) {
                    org.slf4j.LoggerFactory.getLogger(AlibabaChatModelStrategy.class).info(
                        "初始化AlibabaChatModelStrategy的ChatClient，使用的模型类型: {}",
                        alibabaChatModel != null ? alibabaChatModel.getClass().getName() : "null");
                    chatClient = ChatClient.builder(alibabaChatModel).build();
                }
            }
        }
        return chatClient;
    }
}