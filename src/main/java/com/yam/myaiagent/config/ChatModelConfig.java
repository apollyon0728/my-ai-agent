package com.yam.myaiagent.config;

import com.alibaba.cloud.ai.dashscope.chat.DashScopeChatModel;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Primary;

/**
 * 聊天模型配置类
 * 用于配置各种大模型的ChatModel实例
 */
@Configuration
public class ChatModelConfig {
    private static final Logger logger = LoggerFactory.getLogger(ChatModelConfig.class);

    @Autowired
    private ApplicationContext applicationContext;

    // [修复循环依赖异常] 移除 @PostConstruct，避免在 Bean 初始化阶段主动获取 ChatModel Bean
    // public void init() {
    //     logger.info("=== ChatModelConfig初始化 ===");
    //     logger.info("检查所有ChatModel Bean:");
    //
    //     String[] beanNames = applicationContext.getBeanNamesForType(ChatModel.class);
    //     logger.info("找到 {} 个ChatModel Bean", beanNames.length);
    //
    //     try {
    //         for (String beanName : beanNames) {
    //             Object bean = applicationContext.getBean(beanName);
    //             logger.info("Bean名称: {}, 类型: {}", beanName, bean.getClass().getName());
    //         }
    //     } catch (Exception e) {
    //         logger.error("初始化ChatModelBeanTracker时发生错误: ", e);
    //     }
    //
    //     logger.info("=== ChatModelConfig初始化完成 ===");
    // }

    @Bean(name = "alibabaChatModel")
    @Primary
    public ChatModel alibabaChatModel(DashScopeChatModel alibabaModel) {
        logger.info("创建alibabaChatModel Bean，传入的模型类型: {}",
                alibabaModel != null ? alibabaModel.getClass().getName() : "null");
        if (alibabaModel == null) {
            logger.error("DashScopeChatModel为null，无法创建alibabaChatModel Bean");
        }
        return alibabaModel;
    }

//    @Bean(name = "deepSeekChatModel")
//    @Primary
//    public ChatModel deepSeekChatModel(DeepSeekChatModel deepSeekChatModel) {
//        logger.info("deepSeekChatModel Bean，传入的模型类型: {}",
//                deepSeekChatModel != null ? deepSeekChatModel.getClass().getName() : "null");
//        if (deepSeekChatModel == null) {
//            logger.error("DeepSeekChatModel为null，无法创建alibabaChatModel Bean");
//        }
//        return deepSeekChatModel;
//    }

    @Bean(name = "openAIChatModel")
//    @Primary
    public ChatModel openAIChatModel(OpenAiChatModel openAiModel) {
        logger.info("创建openAIChatModel Bean，传入的模型类型: {}", 
                openAiModel != null ? openAiModel.getClass().getName() : "null");
        if (openAiModel == null) {
            logger.error("OpenAiChatModel为null，无法创建openAIChatModel Bean");
        }
        return openAiModel;
    }
}