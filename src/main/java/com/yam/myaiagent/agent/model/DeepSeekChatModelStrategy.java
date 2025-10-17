package com.yam.myaiagent.agent.model;

import org.springframework.stereotype.Component;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import jakarta.annotation.Resource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * DeepSeek大模型策略实现
 */
//@Component("deepseek")
//public class DeepSeekChatModelStrategy implements IChatModelStrategy {
//
//    private static final Logger logger = LoggerFactory.getLogger(DeepSeekChatModelStrategy.class);
//
//    @Resource(name = "deepSeekChatModel")
//    private ChatModel deepSeekChatModel;
//
//    private volatile ChatClient chatClient;
//
//    @Override
//    public ChatClient getChatClient() {
//        if (chatClient == null) {
//            synchronized (this) {
//                if (chatClient == null) {
//                    logger.info("DeepSeek策略使用DeepSeek模型初始化ChatClient");
//                    chatClient = ChatClient.builder(deepSeekChatModel).build();
//                }
//            }
//        }
//        return chatClient;
//    }
//}