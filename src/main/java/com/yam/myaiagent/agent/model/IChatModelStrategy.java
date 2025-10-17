package com.yam.myaiagent.agent.model;

import org.springframework.ai.chat.client.ChatClient;

/**
 * 大模型 ChatClient 策略接口
 * 每个实现代表一种大模型（如阿里、DeepSeek等）
 */
public interface IChatModelStrategy {
    /**
     * 获取当前模型的 ChatClient
     */
    ChatClient getChatClient();
}