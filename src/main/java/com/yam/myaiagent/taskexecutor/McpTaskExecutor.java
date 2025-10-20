package com.yam.myaiagent.taskexecutor;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yam.myaiagent.taskdecompose.DecomposedTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

/**
 * MCP任务执行器
 * 用于执行MCP工具调用任务
 */
@Service
@Slf4j
public class McpTaskExecutor extends AbstractTaskExecutor {

    private final ObjectMapper objectMapper;
    
    /**
     * 构造函数
     */
    @Autowired
    public McpTaskExecutor(ObjectMapper objectMapper) {
        super();
        this.objectMapper = objectMapper;
    }
    
    /**
     * 执行MCP工具调用任务
     * 
     * @param task 待执行的任务
     * @return 执行后的任务
     */
    @Override
    protected DecomposedTask executeTask(DecomposedTask task) {
        log.info("执行MCP任务: {}", task.getTaskId());
        
        try {
            // 检查任务类型
            if (task.getTaskType() != DecomposedTask.TaskType.MCP_TOOL) {
                throw new IllegalArgumentException("任务类型不是MCP_TOOL: " + task.getTaskType());
            }
            
            // 解析参数
            Map<String, Object> parameters = task.getParameters();
            if (parameters == null || parameters.isEmpty()) {
                throw new IllegalArgumentException("MCP任务参数为空");
            }
            
            // 获取必要参数
            String serverName = getRequiredParameter(parameters, "serverName", String.class);
            String toolName = getRequiredParameter(parameters, "toolName", String.class);
            Object arguments = parameters.get("arguments");
            
            log.info("MCP任务参数: serverName={}, toolName={}, arguments={}", 
                    serverName, toolName, arguments);
            
            // 调用MCP工具
            Object result = callMcpTool(serverName, toolName, arguments);
            
            // 设置执行结果
            task.setResult(result);
            
            log.info("MCP任务执行成功: {}", task.getTaskId());
            return task;
        } catch (Exception e) {
            log.error("MCP任务执行失败: {}, 错误: {}", task.getTaskId(), e.getMessage(), e);
            task.setErrorMessage("MCP任务执行失败: " + e.getMessage());
            return task;
        }
    }
    
    /**
     * 获取必要参数
     * 
     * @param parameters 参数映射
     * @param name 参数名
     * @param clazz 参数类型
     * @return 参数值
     */
    private <T> T getRequiredParameter(Map<String, Object> parameters, String name, Class<T> clazz) {
        Object value = parameters.get(name);
        if (value == null) {
            throw new IllegalArgumentException("缺少必要参数: " + name);
        }
        
        if (clazz.isInstance(value)) {
            return clazz.cast(value);
        } else {
            throw new IllegalArgumentException("参数类型错误: " + name + ", 期望: " + clazz.getSimpleName() + 
                    ", 实际: " + value.getClass().getSimpleName());
        }
    }
    
    /**
     * 调用MCP工具
     * 
     * @param serverName MCP服务器名称
     * @param toolName 工具名称
     * @param arguments 参数
     * @return 执行结果
     */
    private Object callMcpTool(String serverName, String toolName, Object arguments) {
        log.info("调用MCP工具: serverName={}, toolName={}", serverName, toolName);
        
        try {
            // 将参数转换为JSON字符串
            String argumentsJson;
            if (arguments instanceof String) {
                argumentsJson = (String) arguments;
            } else {
                argumentsJson = objectMapper.writeValueAsString(arguments);
            }
            
            // TODO: 实现实际的MCP工具调用逻辑
            // 这里需要集成Spring AI的MCP工具调用功能
            // 暂时返回模拟结果
            String result = String.format(
                    "MCP工具调用结果: serverName=%s, toolName=%s, arguments=%s",
                    serverName, toolName, argumentsJson);
            
            log.info("MCP工具调用成功: {}", result);
            return result;
        } catch (JsonProcessingException e) {
            log.error("参数序列化失败: {}", e.getMessage(), e);
            throw new RuntimeException("参数序列化失败: " + e.getMessage(), e);
        } catch (Exception e) {
            log.error("MCP工具调用失败: {}", e.getMessage(), e);
            throw new RuntimeException("MCP工具调用失败: " + e.getMessage(), e);
        }
    }
}