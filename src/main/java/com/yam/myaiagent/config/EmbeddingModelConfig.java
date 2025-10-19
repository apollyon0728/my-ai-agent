package com.yam.myaiagent.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * 嵌入模型配置类
 * 用于配置各种大模型的EmbeddingModel实例
 */
@Configuration
public class EmbeddingModelConfig {
    private static final Logger logger = LoggerFactory.getLogger(EmbeddingModelConfig.class);

    /**
     * 配置DashScope嵌入模型为主要模型
     * 使用@Primary注解标记为首选的EmbeddingModel实现
     */
    @Bean
    @Primary
    public EmbeddingModel primaryEmbeddingModel(EmbeddingModel dashscopeEmbeddingModel) {
        logger.info("配置DashScope嵌入模型为主要模型: {}", 
                dashscopeEmbeddingModel != null ? dashscopeEmbeddingModel.getClass().getName() : "null");
        return dashscopeEmbeddingModel;
    }
}