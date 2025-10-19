package com.yam.myaiagent.taskdecompose;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 基于向量存储的任务拆解器实现
 * 使用向量数据库存储和检索任务拆解规则
 */
@Service
@Slf4j
public class VectorStoreTaskDecomposer implements TaskDecomposer {

    @Resource
    @Qualifier("pgVectorVectorStore")
    private VectorStore vectorStore;

    @Resource
    @Qualifier("dashscopeEmbeddingModel")
    private EmbeddingModel embeddingModel;

    private final ObjectMapper objectMapper = new ObjectMapper();

    // 规则文档的前缀，用于区分普通文档和规则文档
    private static final String RULE_PREFIX = "TASK_RULE:";
    
    // 相似度阈值，用于匹配规则
    private static final double SIMILARITY_THRESHOLD = 0.7;

    /**
     * 将问题拆解为具体任务列表
     *
     * @param question 用户输入的问题
     * @return 拆解后的任务列表
     */
    @Override
    public List<DecomposedTask> decompose(String question) {
        log.info("开始拆解问题: {}", question);
        
        // 1. 查询相似的规则
        List<TaskRule> matchedRules = findMatchingRules(question);
        if (matchedRules.isEmpty()) {
            log.info("未找到匹配的规则，返回空任务列表");
            return Collections.emptyList();
        }
        
        // 2. 根据规则生成任务
        List<DecomposedTask> tasks = new ArrayList<>();
        for (TaskRule rule : matchedRules) {
            tasks.addAll(generateTasksFromRule(rule, question));
        }
        
        // 3. 按优先级排序
        tasks.sort(Comparator.comparingInt(DecomposedTask::getPriority));
        
        log.info("问题拆解完成，共生成{}个任务", tasks.size());
        return tasks;
    }

    /**
     * 查找与问题匹配的规则
     *
     * @param question 用户问题
     * @return 匹配的规则列表
     */
    private List<TaskRule> findMatchingRules(String question) {
        // 使用向量搜索查找相似的规则
        // 使用Builder模式创建SearchRequest对象
        log.info("构建SearchRequest，查询: {}, 相似度阈值: {}", question, SIMILARITY_THRESHOLD);
        
        // 尝试更简单的过滤表达式格式
        String filterExpr = "type:TASK_RULE";
        log.info("使用过滤表达式: {}", filterExpr);
        
        try {
            SearchRequest searchRequest = SearchRequest.builder()
                .query(question)
                .topK(5)
                .similarityThreshold(SIMILARITY_THRESHOLD)
                .filterExpression(filterExpr)
                .build();
                
            log.info("成功创建SearchRequest对象");
            List<Document> similarDocuments = vectorStore.similaritySearch(searchRequest);
            
            // 解析文档内容为TaskRule对象
            return similarDocuments.stream()
                    .map(this::parseRuleFromDocument)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } 
        catch (Exception e) {
            // 如果过滤表达式仍然有问题，记录错误并尝试不使用过滤器
            log.error("过滤表达式错误: {}, 尝试不使用过滤器", e.getMessage());
            
            SearchRequest searchRequest = SearchRequest.builder()
                .query(question)
                .topK(5)
                .similarityThreshold(SIMILARITY_THRESHOLD)
                .build();
                
            log.info("使用无过滤器的SearchRequest");
            List<Document> similarDocuments = vectorStore.similaritySearch(searchRequest);
            
            // 解析文档内容为TaskRule对象
            return similarDocuments.stream()
                    .map(this::parseRuleFromDocument)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        }
    }
    
    /**
     * 从文档解析规则
     *
     * @param document 文档对象
     * @return 解析后的规则对象
     */
    private TaskRule parseRuleFromDocument(Document document) {
        try {
            return objectMapper.readValue(document.getText(), TaskRule.class);
        } catch (JsonProcessingException e) {
            log.error("解析规则文档失败: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 根据规则生成具体任务
     *
     * @param rule 匹配的规则
     * @param question 用户问题
     * @return 生成的任务列表
     */
    private List<DecomposedTask> generateTasksFromRule(TaskRule rule, String question) {
        List<DecomposedTask> tasks = new ArrayList<>();
        Map<Integer, String> taskIdMap = new HashMap<>();
        
        // 遍历规则中的任务模板
        for (int i = 0; i < rule.getTaskTemplates().size(); i++) {
            TaskRule.TaskTemplate template = rule.getTaskTemplates().get(i);
            
            // 生成任务ID
            String taskId = UUID.randomUUID().toString();
            taskIdMap.put(i, taskId);
            
            // 提取参数
            Map<String, Object> parameters = extractParameters(template.getParameterMappings(), question);
            
            // 处理查询模板中的参数占位符
            String processedQueryTemplate = processTemplate(template.getQueryTemplate(), parameters);
            
            // 处理描述模板中的参数占位符
            String processedDescription = processTemplate(template.getDescriptionTemplate(), parameters);
            
            // 处理依赖关系
            String[] dependencies = null;
            if (template.getDependencyIndices() != null && template.getDependencyIndices().length > 0) {
                dependencies = Arrays.stream(template.getDependencyIndices())
                        .mapToObj(taskIdMap::get)
                        .toArray(String[]::new);
            }
            
            // 创建任务对象
            DecomposedTask task = DecomposedTask.builder()
                    .taskId(taskId)
                    .description(processedDescription)
                    .taskType(template.getTaskType())
                    .queryTemplate(processedQueryTemplate)
                    .parameters(parameters)
                    .priority(template.getPriority())
                    .dependencies(dependencies)
                    .build();
            
            tasks.add(task);
        }
        
        return tasks;
    }
    
    /**
     * 从问题中提取参数
     *
     * @param parameterMappings 参数映射
     * @param question 用户问题
     * @return 提取的参数
     */
    private Map<String, Object> extractParameters(Map<String, String> parameterMappings, String question) {
        Map<String, Object> parameters = new HashMap<>();
        
        if (parameterMappings == null) {
            return parameters;
        }
        
        // 遍历参数映射
        for (Map.Entry<String, String> entry : parameterMappings.entrySet()) {
            String paramName = entry.getKey();
            String extractRule = entry.getValue();
            
            // 使用正则表达式提取参数
            Pattern pattern = Pattern.compile(extractRule);
            Matcher matcher = pattern.matcher(question);
            
            if (matcher.find()) {
                // 如果有捕获组，使用第一个捕获组的值
                if (matcher.groupCount() > 0) {
                    parameters.put(paramName, matcher.group(1));
                } else {
                    // 否则使用整个匹配的值
                    parameters.put(paramName, matcher.group());
                }
            }
        }
        
        return parameters;
    }
    
    /**
     * 处理模板中的参数占位符
     *
     * @param template 模板字符串
     * @param parameters 参数映射
     * @return 处理后的字符串
     */
    private String processTemplate(String template, Map<String, Object> parameters) {
        if (template == null) {
            return null;
        }
        
        String result = template;
        
        // 替换参数占位符
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            String placeholder = "\\{\\{" + entry.getKey() + "\\}\\}";
            result = result.replaceAll(placeholder, entry.getValue().toString());
        }
        
        return result;
    }

    /**
     * 上传拆解规则到向量数据库
     *
     * @param ruleJson 规则JSON字符串
     * @return 上传成功的规则ID
     */
    @Override
    public String uploadRule(String ruleJson) {
        try {
            log.info("开始上传规则，规则JSON: {}", ruleJson);
            
            // 解析规则JSON
            TaskRule rule = objectMapper.readValue(ruleJson, TaskRule.class);
            
            // 处理matchPatterns字段
            if (rule.getMatchPatterns() != null && !rule.getMatchPatterns().isEmpty()) {
                log.info("检测到matchPatterns字段，值为: {}", rule.getMatchPatterns());
                // 如果matchPattern为空，使用matchPatterns的第一个元素
                if (rule.getMatchPattern() == null && !rule.getMatchPatterns().isEmpty()) {
                    rule.setMatchPattern(rule.getMatchPatterns().get(0));
                    log.info("将matchPatterns的第一个元素设置为matchPattern: {}", rule.getMatchPattern());
                }
            }
            
            // 生成规则ID
            if (rule.getRuleId() == null) {
                rule.setRuleId(UUID.randomUUID().toString());
            }
            
            // 将规则转换为文档
            Document document = Document.builder()
                    .id(rule.getRuleId())
                    .text(objectMapper.writeValueAsString(rule))
                    .metadata(Map.of("type", RULE_PREFIX, "name", rule.getRuleName()))
                    .build();
            
            // 添加到向量存储
            vectorStore.add(Collections.singletonList(document));
            
            log.info("规则上传成功: {}", rule.getRuleId());
            return rule.getRuleId();
        } catch (JsonProcessingException e) {
            log.error("规则上传失败: {}", e.getMessage());
            throw new RuntimeException("规则上传失败", e);
        }
    }

    /**
     * 获取所有拆解规则
     *
     * @return 所有规则的列表
     */
    @Override
    public List<String> getAllRules() {
        // 查询所有规则文档
        // 使用Builder模式创建SearchRequest对象
        SearchRequest searchRequest = SearchRequest.builder()
                .query("*")
                .topK(100)
                .filterExpression("metadata.type:'" + RULE_PREFIX + "'")
                .build();
        
        List<Document> ruleDocuments = vectorStore.similaritySearch(searchRequest);
        
        // 返回规则JSON字符串列表
        return ruleDocuments.stream()
                .map(Document::getText)
                .collect(Collectors.toList());
    }

    /**
     * 删除指定规则
     *
     * @param ruleId 规则ID
     */
    @Override
    public void deleteRule(String ruleId) {
        // 删除指定ID的规则文档
        vectorStore.delete(Collections.singletonList(ruleId));
        log.info("规则删除成功: {}", ruleId);
    }
}