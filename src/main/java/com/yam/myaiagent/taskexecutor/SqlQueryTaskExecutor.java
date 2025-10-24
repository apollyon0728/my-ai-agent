package com.yam.myaiagent.taskexecutor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yam.myaiagent.taskdecompose.DecomposedTask;
import com.yam.myaiagent.taskdecompose.util.SqlParameterUtils;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
            
            // 添加调试日志
            log.info("【SQL执行调试】SQL模板原始内容: {}", sqlTemplate);
            log.info("【SQL执行调试】SQL参数详情: {}", parameters);
            log.info("【SQL执行调试】SQL参数类型: {}", parameters != null ? parameters.getClass().getName() : "null");
            if (parameters != null) {
                for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                    log.info("【SQL执行调试】参数 {} = {} (类型: {})",
                            entry.getKey(), entry.getValue(),
                            entry.getValue() != null ? entry.getValue().getClass().getName() : "null");
                }
            }

            // 检查SQL模板中是否包含{{}}格式的占位符
            if (sqlTemplate.contains("{{")) {
                log.warn("【SQL执行调试】SQL模板包含未处理的{{}}格式占位符，这可能导致SQL语法错误");
                // 提取所有{{}}格式的占位符
                Pattern pattern = Pattern.compile("\\{\\{([^}]+)\\}\\}");
                Matcher matcher = pattern.matcher(sqlTemplate);
                while (matcher.find()) {
                    String placeholder = matcher.group(1);
                    log.warn("【SQL执行调试】未处理的占位符: {}, 参数映射中是否存在: {}",
                            placeholder, parameters != null && parameters.containsKey(placeholder));
                }
                
                // 尝试替换占位符
                if (parameters != null) {
                    String processedSql = sqlTemplate;
                    for (Map.Entry<String, Object> entry : parameters.entrySet()) {
                        String paramName = entry.getKey();
                        Object paramValue = entry.getValue();
                        String placeholder = "\\{\\{" + paramName + "\\}\\}";
                        
                        // 使用SqlParameterUtils格式化参数值
                        String formattedValue;
                        if (paramValue != null) {
                            SqlParameterUtils.ParameterType paramType = SqlParameterUtils.identifyParameterType(paramValue);
                            formattedValue = SqlParameterUtils.formatParameterValue(paramValue, paramType);
                            log.info("【SQL执行调试】参数 {} 的类型为 {}, 格式化后的值为: {}",
                                    paramName, paramType, formattedValue);
                        } else {
                            formattedValue = "null";
                        }
                        
                        processedSql = processedSql.replaceAll(placeholder, formattedValue);
                    }
                    log.info("【SQL执行调试】尝试处理后的SQL: {}", processedSql);
                    
                    // 如果仍然包含{{}}格式的占位符，记录警告
                    if (processedSql.contains("{{")) {
                        log.warn("【SQL执行调试】处理后的SQL仍然包含未替换的占位符");
                        
                        // 提取未替换的占位符
                        Pattern remainingPattern = Pattern.compile("\\{\\{([^}]+)\\}\\}");
                        Matcher remainingMatcher = remainingPattern.matcher(processedSql);
                        while (remainingMatcher.find()) {
                            String remainingPlaceholder = remainingMatcher.group(1);
                            log.warn("【SQL执行调试】未能替换的占位符: {}", remainingPlaceholder);
                            
                            // 尝试使用默认值替换未处理的占位符
                            if (remainingPlaceholder.equals("start_date")) {
                                processedSql = processedSql.replaceAll("\\{\\{" + remainingPlaceholder + "\\}\\}", "'2000-01-01'");
                                log.info("【SQL执行调试】使用默认值'2000-01-01'替换start_date占位符");
                            } else if (remainingPlaceholder.equals("data_source")) {
                                processedSql = processedSql.replaceAll("\\{\\{" + remainingPlaceholder + "\\}\\}", "'1'");
                                log.info("【SQL执行调试】使用默认值'1'替换data_source占位符");
                            } else {
                                // 对于其他未知占位符，使用通用默认值
                                processedSql = processedSql.replaceAll("\\{\\{" + remainingPlaceholder + "\\}\\}", "NULL");
                                log.info("【SQL执行调试】使用NULL替换未知占位符: {}", remainingPlaceholder);
                            }
                        }
                        
                        log.info("【SQL执行调试】使用默认值替换后的SQL: {}", processedSql);
                    } else {
                        log.info("【SQL执行调试】所有占位符已替换");
                    }
                    
                    // 使用处理后的SQL
                    sqlTemplate = processedSql;
                }
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