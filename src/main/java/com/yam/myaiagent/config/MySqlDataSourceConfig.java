package com.yam.myaiagent.config;

import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import jakarta.annotation.PostConstruct;

/**
 * MySQL数据源配置类
 * 用于配置MySQL数据源
 */
@Configuration
public class MySqlDataSourceConfig {
    private static final Logger logger = LoggerFactory.getLogger(MySqlDataSourceConfig.class);

    @PostConstruct
    public void init() {
        logger.info("=== MySqlDataSourceConfig初始化 ===");
        logger.info("此配置类用于创建MySQL数据源");
        logger.info("=== MySqlDataSourceConfig初始化完成 ===");
    }

    /**
     * 创建MySQL数据源
     */
    @Bean(name = "mysqlDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.mysql")
    public HikariDataSource mysqlDataSource() {
        logger.info("创建MySQL数据源（自动绑定配置）");
        // Spring Boot会自动将yml属性注入到HikariDataSource
        return new HikariDataSource();
    }

    /**
     * 创建MySQL JdbcTemplate
     */
    @Bean(name = "mysqlJdbcTemplate")
    public JdbcTemplate mysqlJdbcTemplate(@Qualifier("mysqlDataSource") DataSource dataSource) {
        logger.info("创建MySQL JdbcTemplate");
        return new JdbcTemplate(dataSource);
    }
}