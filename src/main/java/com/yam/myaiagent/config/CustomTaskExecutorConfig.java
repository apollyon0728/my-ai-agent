package com.yam.myaiagent.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yam.myaiagent.taskexecutor.CompositeTaskExecutor;
import com.yam.myaiagent.taskexecutor.FunctionCallTaskExecutor;
import com.yam.myaiagent.taskexecutor.McpTaskExecutor;
import com.yam.myaiagent.taskexecutor.SqlQueryTaskExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import jakarta.annotation.PostConstruct;

/**
 * 自定义TaskExecutor配置类
 * 专门用于创建名为"taskExecutor"的CompositeTaskExecutor bean
 */
@Configuration
public class CustomTaskExecutorConfig {
    private static final Logger logger = LoggerFactory.getLogger(CustomTaskExecutorConfig.class);

    @Autowired
    private ObjectMapper objectMapper;

    @PostConstruct
    public void init() {
        logger.info("=== CustomTaskExecutorConfig初始化 ===");
        logger.info("此配置类用于创建名为'taskExecutor'的CompositeTaskExecutor bean");
        logger.info("=== CustomTaskExecutorConfig初始化完成 ===");
    }

    /**
     * 创建McpTaskExecutor
     */
    @Bean
    public McpTaskExecutor mcpTaskExecutor() {
        logger.info("创建McpTaskExecutor");
        return new McpTaskExecutor(objectMapper);
    }

    /**
     * 创建FunctionCallTaskExecutor
     */
    @Bean
    public FunctionCallTaskExecutor functionCallTaskExecutor() {
        logger.info("创建FunctionCallTaskExecutor");
        return new FunctionCallTaskExecutor(objectMapper);
    }

    /**
     * 创建一个名为"taskExecutor"的CompositeTaskExecutor
     * 这将覆盖Spring Boot自动配置的"taskExecutor"
     */
    @Bean(name = "taskExecutor")
    @Primary
    public CompositeTaskExecutor taskExecutor(McpTaskExecutor mcpTaskExecutor, FunctionCallTaskExecutor functionCallTaskExecutor, SqlQueryTaskExecutor sqlQueryTaskExecutor) {
        logger.info("创建自定义的taskExecutor (CompositeTaskExecutor类型)");
        logger.info("注入的依赖: mcpTaskExecutor={}, functionCallTaskExecutor={}", 
                mcpTaskExecutor != null ? mcpTaskExecutor.getClass().getName() : "null",
                functionCallTaskExecutor != null ? functionCallTaskExecutor.getClass().getName() : "null");
        
        CompositeTaskExecutor executor = new CompositeTaskExecutor(mcpTaskExecutor, functionCallTaskExecutor, sqlQueryTaskExecutor);
        logger.info("CompositeTaskExecutor创建成功: {}", executor);
        return executor;
    }
}