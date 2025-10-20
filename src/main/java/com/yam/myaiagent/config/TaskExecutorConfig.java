package com.yam.myaiagent.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.task.TaskExecutionAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import jakarta.annotation.PostConstruct;

/**
 * 自定义TaskExecutor配置类
 * 用于覆盖Spring Boot自动配置的taskExecutor
 */
@Configuration
@EnableAsync
public class TaskExecutorConfig {
    private static final Logger logger = LoggerFactory.getLogger(TaskExecutorConfig.class);

    @PostConstruct
    public void init() {
        logger.info("=== TaskExecutorConfig初始化 ===");
        logger.info("此配置类用于覆盖Spring Boot自动配置的taskExecutor");
        logger.info("=== TaskExecutorConfig初始化完成 ===");
    }

    /**
     * 创建一个名为"applicationTaskExecutor"的TaskExecutor
     * 这将覆盖Spring Boot自动配置的"applicationTaskExecutor"
     */
    @Bean(name = "applicationTaskExecutor")
    @ConditionalOnMissingBean(name = "applicationTaskExecutor")
    @Primary
    public TaskExecutor applicationTaskExecutor() {
        logger.info("创建自定义的applicationTaskExecutor");
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(25);
        executor.setThreadNamePrefix("custom-app-task-");
        executor.initialize();
        return executor;
    }
}