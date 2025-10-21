package com.yam.myaiagent.taskexecutor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yam.myaiagent.taskdecompose.DecomposedTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * SQL查询任务执行器
 * 用于执行SQL_QUERY类型的任务
 */
@Slf4j
@Component
public class SqlQueryTaskExecutor extends AbstractTaskExecutor {

    private final ObjectMapper objectMapper;
    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    /**
     * 构造函数
     * 
     * @param objectMapper JSON序列化/反序列化工具
     * @param dataSource MySQL数据源
     */
    @Autowired
    public SqlQueryTaskExecutor(ObjectMapper objectMapper, @Qualifier("mysqlDataSource") DataSource dataSource) {
        super();
        this.objectMapper = objectMapper;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        
        log.info("SqlQueryTaskExecutor初始化完成，使用数据源: {}", dataSource.getClass().getName());
    }

    /**
     * 执行SQL查询任务
     * 
     * @param task 待执行的任务
     * @return 执行后的任务
     */
    @Override
    protected DecomposedTask executeTask(DecomposedTask task) {
        log.info("开始执行SQL查询任务: {}", task.getTaskId());
        log.info("SQL模板: {}", task.getQueryTemplate());
        log.info("SQL参数: {}", task.getParameters());
        
        try {
            // 获取SQL模板和参数
            String sqlTemplate = task.getQueryTemplate();
            Map<String, Object> parameters = task.getParameters();
            
            if (parameters == null) {
                parameters = new HashMap<>();
            }
            
            // 执行SQL查询
            List<Map<String, Object>> result;
            if (sqlTemplate.contains(":")) {
                // 使用命名参数
                result = namedParameterJdbcTemplate.queryForList(sqlTemplate, parameters);
            } else {
                // 使用?占位符
                result = jdbcTemplate.queryForList(sqlTemplate);
            }
            
            // 设置执行结果
            task.setResult(result);
            log.info("SQL查询执行成功，返回{}条记录", result.size());
            
            return task;
        } catch (Exception e) {
            log.error("SQL查询执行失败: {}", e.getMessage(), e);
            task.setErrorMessage("SQL查询执行失败: " + e.getMessage());
            return task;
        }
    }
}