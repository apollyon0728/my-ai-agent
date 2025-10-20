package com.yam.myaiagent.config.debug;

import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Configuration;

/**
 * 用于调试TaskExecutor相关问题的配置类
 */
@Configuration
public class TaskExecutorDebugConfig {
    private static final Logger logger = LoggerFactory.getLogger(TaskExecutorDebugConfig.class);

    @Autowired
    private ApplicationContext applicationContext;

    @PostConstruct
    public void init() {
        logger.info("=== TaskExecutorDebugConfig初始化 ===");
        
        // 检查是否存在名为"taskExecutor"的bean
        boolean hasTaskExecutor = applicationContext.containsBean("taskExecutor");
        logger.info("是否存在名为'taskExecutor'的bean: {}", hasTaskExecutor);
        
        if (hasTaskExecutor) {
            Object taskExecutor = applicationContext.getBean("taskExecutor");
            logger.info("'taskExecutor' bean的类型: {}", taskExecutor.getClass().getName());
        }
        
        // 检查是否存在名为"compositeTaskExecutor"的bean
        boolean hasCompositeTaskExecutor = applicationContext.containsBean("compositeTaskExecutor");
        logger.info("是否存在名为'compositeTaskExecutor'的bean: {}", hasCompositeTaskExecutor);
        
        if (hasCompositeTaskExecutor) {
            Object compositeTaskExecutor = applicationContext.getBean("compositeTaskExecutor");
            logger.info("'compositeTaskExecutor' bean的类型: {}", compositeTaskExecutor.getClass().getName());
        }
        
        logger.info("=== TaskExecutorDebugConfig初始化完成 ===");
    }
}