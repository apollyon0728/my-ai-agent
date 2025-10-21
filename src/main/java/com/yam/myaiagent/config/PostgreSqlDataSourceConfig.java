package com.yam.myaiagent.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import jakarta.annotation.PostConstruct;

/**
 * PostgreSQL数据源配置类
 * 用于配置PostgreSQL数据源
 */
@Configuration
public class PostgreSqlDataSourceConfig {
    private static final Logger logger = LoggerFactory.getLogger(PostgreSqlDataSourceConfig.class);

    @PostConstruct
    public void init() {
        logger.info("=== PostgreSqlDataSourceConfig初始化 ===");
        logger.info("此配置类用于创建PostgreSQL数据源");
        logger.info("=== PostgreSqlDataSourceConfig初始化完成 ===");
    }

    /**
     * 创建PostgreSQL数据源
     * 使用@Primary注解标记为主数据源
     */
    @Primary
    @Bean(name = "postgresqlDataSource")
    @ConfigurationProperties(prefix = "spring.datasource.postgresql")
    public com.zaxxer.hikari.HikariDataSource postgresqlDataSource() {
        logger.info("创建PostgreSQL数据源（自动绑定配置）");
        // Spring Boot会自动将yml属性注入到HikariDataSource
        // 可直接返回，属性已自动生效
        return new com.zaxxer.hikari.HikariDataSource();
    }

    /**
     * 创建PostgreSQL JdbcTemplate
     * 使用@Primary注解标记为主JdbcTemplate
     */
    @Primary
    @Bean(name = "postgresqlJdbcTemplate")
    public JdbcTemplate postgresqlJdbcTemplate(@Qualifier("postgresqlDataSource") DataSource dataSource) {
        logger.info("创建PostgreSQL JdbcTemplate");
        return new JdbcTemplate(dataSource);
    }
}