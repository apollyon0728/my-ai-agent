package com.yam.myaiagent.taskdecompose;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yam.myaiagent.taskdecompose.util.SqlParameterUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
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

    /**
     * ObjectMapper实例，用于JSON序列化和反序列化操作
     * 该对象映射器被声明为final类型，确保其引用不可变，
     * 并且在类初始化时创建一个新的ObjectMapper实例。
     * ObjectMapper是Jackson库的核心类，提供Java对象与JSON之间的转换功能。</p>
     */
    private final ObjectMapper objectMapper = new ObjectMapper();

    // 规则文档的前缀，用于区分普通文档和规则文档
    private static final String RULE_PREFIX = "TASK_RULE:";

    // 相似度阈值，用于匹配规则 - 降低阈值以提高匹配率
    private static final double SIMILARITY_THRESHOLD = 0.5;

    // 缓存最近使用的规则ID，避免重复添加
    private final Map<String, Long> recentRuleCache = new ConcurrentHashMap<>();

    /**
     * 获取向量存储中的文档总数
     *
     * @return 向量存储中文档的数量，如果获取失败则返回-1
     */
    private long getVectorStoreDocumentCount() {
        try {
            // 构造搜索请求来获取所有文档
            SearchRequest countRequest = SearchRequest.builder()
                    .query("*")
                    .topK(1000)  // 设置一个较大的值
                    .build();
            List<Document> allDocs = vectorStore.similaritySearch(countRequest);
            return Objects.requireNonNull(allDocs).size();
        } catch (Exception e) {
            log.error("获取向量存储文档数量失败: {}", e.getMessage());
            return -1;
        }
    }

    /**
     * 将输入的问题拆解为多个子任务
     *
     * @param question 需要拆解的问题字符串
     * @return 拆解后的任务列表，按优先级排序
     */
    @Override
    public List<DecomposedTask> decompose(String question) {
        log.info("decompose 开始拆解问题: {}, 当前线程ID: {}, 当前向量存储文档数量: {}",
                question, Thread.currentThread().getId(), getVectorStoreDocumentCount());

        // FIXME 1. 查询相似的规则
        List<TaskRule> matchedRules = findMatchingRules(question);
        if (matchedRules.isEmpty()) {
            log.info("未找到匹配的规则，返回空任务列表");
            return Collections.emptyList();
        }

        // 记录找到的规则信息，便于调试
        log.info("找到{}个匹配规则:", matchedRules.size());
        for (int i = 0; i < matchedRules.size(); i++) {
            TaskRule rule = matchedRules.get(i);
            log.info("规则 #{}: ID={}, 名称={}", i + 1, rule.getRuleId(), rule.getRuleName());
        }

        // FIXME 2. 根据规则生成任务
        List<DecomposedTask> tasks = new ArrayList<>();
        for (TaskRule rule : matchedRules) {
            tasks.addAll(generateTasksFromRule(rule, question));
        }

        // FIXME 3. 按优先级排序
        tasks.sort(Comparator.comparingInt(DecomposedTask::getPriority));

        log.info("VectorStoreTaskDecomposer decompose 问题拆解完成>>>>>，共生成{}个任务", tasks.size());
        return tasks;
    }


    /**
     * [重要] 查找与问题匹配的规则
     * 支持分块存储的规则查询和合并
     *
     * @param question 用户问题
     * @return 匹配的规则列表
     */
    private List<TaskRule> findMatchingRules(String question) {
        // 使用向量搜索查找相似的规则
        // 使用Builder模式创建SearchRequest对象
        long initialDocCount = getVectorStoreDocumentCount();
        log.info("构建SearchRequest，查询: {}, 相似度阈值: {}, 当前线程ID: {}, 查询前向量存储文档数量: {}",
                question, SIMILARITY_THRESHOLD, Thread.currentThread().getId(), initialDocCount);

        // 详细记录用户问题的关键部分，帮助调试规则匹配
        log.info("【规则匹配调试】用户问题: {}", question);
        // 提取可能的关键词，帮助分析为什么规则匹配失败
        String[] keywordPatterns = {"source", "business_day", "erp", "in", "=", ">=", "<="};
        for (String pattern : keywordPatterns) {
            if (question.contains(pattern)) {
                log.info("【规则匹配调试】检测到关键词: {}", pattern);
            }
        }

        // 尝试不同格式的过滤表达式
        log.info("尝试使用不同格式的过滤表达式");

        // FIXME 方式1: 使用 metadata.type = 'TASK_RULE' 格式
        String filterExpr1 = "metadata.type == 'TASK_RULE'";
        log.info("尝试过滤表达式格式1: {}", filterExpr1);

        try {
            SearchRequest searchRequest = SearchRequest.builder()
                    .query(question)
                    .topK(10) // 增加topK以获取更多可能的块
                    .similarityThreshold(SIMILARITY_THRESHOLD)
                    .filterExpression(filterExpr1)
                    .build();

            log.info("成功创建SearchRequest对象(格式1)");
            List<Document> similarDocuments = vectorStore.similaritySearch(searchRequest);
            log.info("格式1-查询结果: 找到{}个文档, 查询后向量存储文档数量: {}",
                    Objects.requireNonNull(similarDocuments).size(), getVectorStoreDocumentCount());

            // 记录文档元数据，帮助调试
            if (!similarDocuments.isEmpty()) {
                Document firstDoc = similarDocuments.getFirst();
                log.info("第一个文档ID: {}, 元数据: {}", firstDoc.getId(), firstDoc.getMetadata());
            }

            // 处理分块规则
            List<TaskRule> rules = processDocumentsWithChunks(similarDocuments);
            
            if (!rules.isEmpty()) {
                log.info("使用过滤表达式格式1成功找到{}个规则", rules.size());
                return rules;
            }

            log.info("使用过滤表达式格式1未找到规则，尝试格式2");
        } catch (Exception e) {
            log.error("过滤表达式格式1错误: {}, 尝试格式2", e.getMessage());
        }

        // FIXME 方式2: 使用 type = 'TASK_RULE' 格式
        String filterExpr2 = "metadata['type'] == 'TASK_RULE'";
        log.info("尝试过滤表达式格式2: {}", filterExpr2);

        try {
            SearchRequest searchRequest = SearchRequest.builder()
                    .query(question)
                    .topK(10) // 增加topK以获取更多可能的块
                    .similarityThreshold(SIMILARITY_THRESHOLD)
                    .filterExpression(filterExpr2)
                    .build();

            log.info("成功创建SearchRequest对象(格式2)");
            List<Document> similarDocuments = vectorStore.similaritySearch(searchRequest);
            log.info("格式2-查询结果: 找到{}个文档, 查询后向量存储文档数量: {}",
                    similarDocuments.size(), getVectorStoreDocumentCount());

            // 处理分块规则
            List<TaskRule> rules = processDocumentsWithChunks(similarDocuments);
            
            if (!rules.isEmpty()) {
                log.info("使用过滤表达式格式2成功找到{}个规则", rules.size());
                return rules;
            }

            log.info("使用过滤表达式格式2未找到规则，尝试不使用过滤器");
        } catch (Exception e) {
            log.error("过滤表达式格式2错误: {}, 尝试不使用过滤器", e.getMessage());
        }

        // FIXME 如果两种过滤表达式都失败，尝试不使用过滤器
        log.info("findMatchingRules 尝试不使用过滤器");
        SearchRequest searchRequest = SearchRequest.builder()
                .query(question)
                .topK(10) // 增加topK以获取更多可能的块
                .similarityThreshold(SIMILARITY_THRESHOLD)
                .build();

        log.info("成功创建无过滤器的SearchRequest对象");
        List<Document> similarDocuments = vectorStore.similaritySearch(searchRequest);
        log.info("查询结果: 找到{}个文档, 查询后向量存储文档数量: {}",
                Objects.requireNonNull(similarDocuments).size(), getVectorStoreDocumentCount());

        // 处理分块规则
        List<TaskRule> rules = processDocumentsWithChunks(similarDocuments);
        
        log.info("不使用过滤器找到{}个规则", rules.size());
        return rules;
    }
    
    /**
     * 处理可能包含分块的文档列表
     * 将属于同一规则的块合并，并解析为TaskRule对象
     *
     * @param documents 文档列表
     * @return 解析后的TaskRule对象列表
     */
    private List<TaskRule> processDocumentsWithChunks(List<Document> documents) {
        // 用于存储完整规则的Map
        Map<String, StringBuilder> completeRuleMap = new HashMap<>();
        // 用于存储规则块数量的Map
        Map<String, Integer> ruleChunkCountMap = new HashMap<>();
        // 用于存储已收集的块索引的Map
        Map<String, Set<Integer>> collectedChunksMap = new HashMap<>();
        
        // 首先处理非分块的规则
        List<TaskRule> nonChunkedRules = new ArrayList<>();
        
        for (Document doc : documents) {
            Map<String, Object> metadata = doc.getMetadata();
            Boolean isChunked = (Boolean) metadata.get("is_chunked");
            
            if (isChunked == null || !isChunked) {
                // 非分块规则，直接解析
                TaskRule rule = parseRuleFromDocument(doc);
                if (rule != null) {
                    nonChunkedRules.add(rule);
                }
                continue;
            }
            
            // 处理分块规则
            String parentRuleId = (String) metadata.get("parent_rule_id");
            Integer chunkIndex = (Integer) metadata.get("chunk_index");
            Integer totalChunks = (Integer) metadata.get("total_chunks");
            
            if (parentRuleId == null || chunkIndex == null || totalChunks == null) {
                log.warn("分块规则元数据不完整，跳过: {}", doc.getId());
                continue;
            }
            
            // 初始化规则块收集器
            if (!completeRuleMap.containsKey(parentRuleId)) {
                completeRuleMap.put(parentRuleId, new StringBuilder());
                ruleChunkCountMap.put(parentRuleId, totalChunks);
                collectedChunksMap.put(parentRuleId, new HashSet<>());
            }
            
            // 添加块内容
            Set<Integer> collectedChunks = collectedChunksMap.get(parentRuleId);
            if (!collectedChunks.contains(chunkIndex)) {
                completeRuleMap.get(parentRuleId).append(doc.getText());
                collectedChunks.add(chunkIndex);
                log.info("收集规则 {} 的块 {}/{}", parentRuleId, chunkIndex + 1, totalChunks);
            }
        }
        
        // 处理收集到的分块规则
        List<TaskRule> chunkedRules = new ArrayList<>();
        for (Map.Entry<String, StringBuilder> entry : completeRuleMap.entrySet()) {
            String ruleId = entry.getKey();
            StringBuilder ruleJson = entry.getValue();
            int collectedCount = collectedChunksMap.get(ruleId).size();
            int totalCount = ruleChunkCountMap.get(ruleId);
            
            if (collectedCount == totalCount) {
                // 所有块都已收集，解析完整规则
                log.info("规则 {} 的所有块已收集完成 ({}/{}), 尝试解析", ruleId, collectedCount, totalCount);
                try {
                    TaskRule rule = objectMapper.readValue(ruleJson.toString(), TaskRule.class);
                    if (rule != null) {
                        chunkedRules.add(rule);
                        log.info("成功解析分块规则: {}", ruleId);
                    }
                } catch (JsonProcessingException e) {
                    log.error("解析分块规则失败: {}, 错误: {}", ruleId, e.getMessage());
                }
            } else {
                log.warn("规则 {} 的块收集不完整 ({}/{}), 跳过解析", ruleId, collectedCount, totalCount);
            }
        }
        
        // 合并非分块规则和分块规则
        List<TaskRule> allRules = new ArrayList<>(nonChunkedRules);
        allRules.addAll(chunkedRules);
        
        log.info("共解析 {} 个非分块规则和 {} 个分块规则", nonChunkedRules.size(), chunkedRules.size());
        return allRules;
    }

    /**
     * 从文档中解析任务规则
     *
     * @param document 包含规则信息的文档对象，不能为空
     * @return 解析成功返回TaskRule对象，解析失败返回null
     */
    private TaskRule parseRuleFromDocument(Document document) {
        try {
            // 尝试将文档文本内容反序列化为TaskRule对象
            return objectMapper.readValue(document.getText(), TaskRule.class);
        } catch (JsonProcessingException e) {
            log.error("解析规则文档失败: {}", e.getMessage());
            return null;
        }
    }


    /**
     * 根据规则生成具体任务
     *
     * @param rule     匹配的规则
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

            // FIXME 提取参数
            Map<String, Object> parameters = extractParameters(template.getParameterMappings(), question);

            // FIXME 处理查询模板中的参数占位符
            String processedQueryTemplate = processTemplate(template.getQueryTemplate(), parameters);

            // FIXME 处理描述模板中的参数占位符
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
     * @param question          用户问题
     * @return 提取的参数
     */
    private Map<String, Object> extractParameters(Map<String, String> parameterMappings, String question) {
        Map<String, Object> parameters = new HashMap<>();

        if (parameterMappings == null) {
            return parameters;
        }

        log.info("【参数提取调试】开始从问题中提取参数，问题: {}", question);
        log.info("【参数提取调试】参数映射: {}", parameterMappings);
        
        // 预处理问题，将中文连接词替换为标准SQL连接词，以便更好地匹配
        String processedQuestion = question
                .replaceAll("(?i)\\s+且\\s+", " AND ")
                .replaceAll("(?i)\\s+或\\s+", " OR ")
                .replaceAll("(?i)\\s+非\\s+", " NOT ");
        
        log.info("【参数提取调试】预处理后的问题: {}", processedQuestion);

        // 遍历参数映射
        for (Map.Entry<String, String> entry : parameterMappings.entrySet()) {
            String paramName = entry.getKey();
            String extractRule = entry.getValue();

            log.info("【参数提取调试】尝试提取参数: {}, 使用规则: {}", paramName, extractRule);

            // 使用正则表达式提取参数
            Pattern pattern = Pattern.compile(extractRule);
            Matcher matcher = pattern.matcher(processedQuestion);

            if (matcher.find()) {
                log.info("【参数提取调试】找到匹配: {}", matcher.group());
                String extractedValue;
                
                // 如果有捕获组，使用第一个非null的捕获组的值
                if (matcher.groupCount() > 0) {
                    extractedValue = null;
                    for (int i = 1; i <= matcher.groupCount(); i++) {
                        if (matcher.group(i) != null) {
                            extractedValue = matcher.group(i);
                            log.info("【参数提取调试】使用捕获组 {} 值: {}", i, extractedValue);
                            break;
                        }
                    }
                    
                    // 如果所有捕获组都为null，使用整个匹配
                    if (extractedValue == null) {
                        extractedValue = matcher.group();
                        log.info("【参数提取调试】所有捕获组为null，使用整个匹配值: {}", extractedValue);
                    }
                    
                    // 记录所有捕获组，帮助调试复杂正则表达式
                    for (int i = 1; i <= matcher.groupCount(); i++) {
                        log.info("【参数提取调试】捕获组 {}: {}", i, matcher.group(i));
                    }
                } else {
                    // 否则使用整个匹配的值
                    extractedValue = matcher.group();
                    log.info("【参数提取调试】使用整个匹配值: {}", extractedValue);
                }
                
                // 处理提取的值，移除多余的引号和空格
                extractedValue = extractedValue.trim();
                if (extractedValue.startsWith("'") && extractedValue.endsWith("'")) {
                    extractedValue = extractedValue.substring(1, extractedValue.length() - 1);
                    log.info("【参数提取调试】移除单引号后: {}", extractedValue);
                }
                
                // 检查参数安全性
                if (!SqlParameterUtils.isSqlParameterSafe(extractedValue)) {
                    log.warn("【参数提取调试】检测到不安全的SQL参数值: {}", extractedValue);
                    continue;
                }
                
                // 处理特殊参数类型
                if (SqlParameterUtils.isInClauseParameter(paramName, extractedValue)) {
                    // 对于IN子句参数，进行特殊处理
                    log.info("【参数提取调试】检测到IN子句参数: {}={}", paramName, extractedValue);
                    
                    // 详细记录IN子句参数的内容和格式
                    if (extractedValue.contains(",")) {
                        String[] values = extractedValue.split(",");
                        log.info("【参数提取调试】IN子句包含{}个值: {}", values.length, Arrays.toString(values));
                        
                        // 处理每个值，移除多余的引号和空格
                        for (int i = 0; i < values.length; i++) {
                            values[i] = values[i].trim();
                            if (values[i].startsWith("'") && values[i].endsWith("'")) {
                                values[i] = values[i].substring(1, values[i].length() - 1);
                            }
                        }
                        
                        // 重新组合处理后的值
                        extractedValue = String.join(",", values);
                        log.info("【参数提取调试】处理后的IN子句值: {}", extractedValue);
                    }
                    
                    parameters.put(paramName, extractedValue);
                } else {
                    // 对于普通参数，直接存储
                    parameters.put(paramName, extractedValue);
                }
                
                log.info("【参数提取调试】成功提取参数: {}={}", paramName, extractedValue);
            } else {
                log.warn("【参数提取调试】未能提取参数: {}, 规则: {}", paramName, extractRule);
            }
        }
        
        // 尝试从问题中提取特殊格式的参数
        extractSpecialParameters(processedQuestion, parameters);
        
        return parameters;
    }
    
    /**
     * 从问题中提取特殊格式的参数，如IN子句和多条件
     *
     * @param question 用户问题
     * @param parameters 已提取的参数
     */
    private void extractSpecialParameters(String question, Map<String, Object> parameters) {
        log.info("【特殊参数提取】开始提取特殊格式参数");
        
        // 提取source in (x) 格式的参数
        Pattern sourceInPattern = Pattern.compile("source\\s+in\\s+\\(([^\\)]+)\\)", Pattern.CASE_INSENSITIVE);
        Matcher sourceInMatcher = sourceInPattern.matcher(question);
        if (sourceInMatcher.find()) {
            String sourceValue = sourceInMatcher.group(1).trim();
            log.info("【特殊参数提取】找到source in子句: {}", sourceValue);
            parameters.put("data_source", sourceValue);
        }
        
        // 提取business_day in ('x', 'y', 'z') 格式的参数
        Pattern businessDayInPattern = Pattern.compile("business_day\\s+in\\s+\\(([^\\)]+)\\)", Pattern.CASE_INSENSITIVE);
        Matcher businessDayInMatcher = businessDayInPattern.matcher(question);
        if (businessDayInMatcher.find()) {
            String businessDayValue = businessDayInMatcher.group(1).trim();
            log.info("【特殊参数提取】找到business_day in子句: {}", businessDayValue);
            parameters.put("business_day", businessDayValue);
        }
        
        // 提取erp = x 格式的参数
        Pattern erpPattern = Pattern.compile("erp\\s*=\\s*([^\\s,\\)]+)", Pattern.CASE_INSENSITIVE);
        Matcher erpMatcher = erpPattern.matcher(question);
        if (erpMatcher.find()) {
            String erpValue = erpMatcher.group(1).trim();
            log.info("【特殊参数提取】找到erp参数: {}", erpValue);
            parameters.put("erp", erpValue);
        }
        
        // 提取erp_business_day in ('x') 格式的参数
        Pattern erpBusinessDayPattern = Pattern.compile("erp_business_day\\s+in\\s+\\(([^\\)]+)\\)", Pattern.CASE_INSENSITIVE);
        Matcher erpBusinessDayMatcher = erpBusinessDayPattern.matcher(question);
        if (erpBusinessDayMatcher.find()) {
            String erpBusinessDayValue = erpBusinessDayMatcher.group(1).trim();
            log.info("【特殊参数提取】找到erp_business_day in子句: {}", erpBusinessDayValue);
            parameters.put("erp_business_day", erpBusinessDayValue);
        }
    }

    /**
     * 处理模板中的参数占位符
     *
     * @param template   模板字符串
     * @param parameters 参数映射
     * @return 处理后的字符串
     */
    private String processTemplate(String template, Map<String, Object> parameters) {
        if (template == null) {
            return null;
        }

        log.info("【模板处理调试】开始处理模板: {}", template);
        log.info("【模板处理调试】可用参数: {}", parameters);

        String result = template;

        // 替换参数占位符
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            String paramName = entry.getKey();
            Object paramValue = entry.getValue();
            String placeholder = "\\{\\{" + paramName + "\\}\\}";
            
            log.info("【模板处理调试】处理参数: {}={}, 占位符: {}", paramName, paramValue, placeholder);
            
            // 根据参数类型进行格式化
            String formattedValue;
            if (SqlParameterUtils.isInClauseParameter(paramName, paramValue)) {
                // 处理IN子句参数
                formattedValue = SqlParameterUtils.processInClauseParameter(paramValue.toString());
                log.info("【模板处理调试】格式化IN子句参数: {}={}", paramName, formattedValue);
            } else {
                // 处理普通参数
                formattedValue = SqlParameterUtils.formatParameterValue(paramValue);
                log.info("【模板处理调试】格式化普通参数: {}={}", paramName, formattedValue);
            }
            
            // 替换占位符
            String beforeReplace = result;
            result = result.replaceAll(placeholder, formattedValue);
            
            // 检查替换是否成功
            if (beforeReplace.equals(result)) {
                log.warn("【模板处理调试】占位符替换失败: {}", placeholder);
            } else {
                log.info("【模板处理调试】占位符替换成功: {} -> {}", placeholder, formattedValue);
            }
        }

        log.info("【模板处理调试】处理后的模板: {}", result);
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

            // 记录原始规则ID
            String originalId = rule.getRuleId();
            log.info("原始规则ID: {}, 类型: {}", originalId,
                    originalId != null ? originalId.getClass().getName() : "null");

            // 保存原始业务ID到businessId字段
            if (originalId != null) {
                rule.setBusinessId(originalId);
                log.info("设置业务ID(businessId): {}", rule.getBusinessId());
            }

            // 生成规则ID
            if (originalId == null) {
                rule.setRuleId(UUID.randomUUID().toString());
                log.info("规则ID为空，生成新的UUID: {}", rule.getRuleId());
            } else {
                // 检查规则ID是否为有效的UUID格式
                try {
                    UUID.fromString(originalId);
                    log.info("规则ID已经是有效的UUID格式: {}", originalId);
                } catch (IllegalArgumentException e) {
                    // 如果不是有效的UUID，则生成新的UUID
                    String newUuid = UUID.randomUUID().toString();
                    log.info("规则ID不是有效的UUID格式，从 '{}' 替换为新生成的UUID: {}", originalId, newUuid);
                    rule.setRuleId(newUuid);
                }
            }

            // 构建元数据，保留原始ID
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("type", "TASK_RULE");  // 使用不带前缀的类型名称
            metadata.put("name", rule.getRuleName());

            // 使用businessId字段保存原始业务ID
            if (rule.getBusinessId() != null) {
                metadata.put("businessId", rule.getBusinessId());
                log.info("保存业务ID '{}' 到元数据中", rule.getBusinessId());
            }

            // 将规则转换为文档
            Document document = Document.builder()
                    .id(rule.getRuleId())
                    .text(objectMapper.writeValueAsString(rule))
                    .metadata(metadata)
                    .build();

            log.info("创建文档，文档ID: {}, 元数据: {}", document.getId(), metadata);

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
        log.info("获取所有拆解规则");

        // 尝试不同格式的过滤表达式
        List<Document> ruleDocuments = new ArrayList<>();
        boolean success = false;

        // 方式1: 使用 metadata.type = 'TASK_RULE' 格式
        try {
            log.info("尝试使用过滤表达式格式1: metadata.type = 'TASK_RULE'");
            SearchRequest searchRequest = SearchRequest.builder()
                    .query("*")
                    .topK(100)
                    .filterExpression("metadata.type = 'TASK_RULE'")
                    .build();

            ruleDocuments = vectorStore.similaritySearch(searchRequest);
            log.info("使用过滤表达式格式1成功找到{}个规则文档", ruleDocuments.size());
            success = true;
        } catch (Exception e) {
            log.error("使用过滤表达式格式1失败: {}", e.getMessage());
        }

        // 方式2: 使用 type = 'TASK_RULE' 格式
        if (!success) {
            try {
                log.info("尝试使用过滤表达式格式2: type = 'TASK_RULE'");
                SearchRequest searchRequest = SearchRequest.builder()
                        .query("*")
                        .topK(100)
                        .filterExpression("type = 'TASK_RULE'")
                        .build();

                ruleDocuments = vectorStore.similaritySearch(searchRequest);
                log.info("使用过滤表达式格式2成功找到{}个规则文档", ruleDocuments.size());
                success = true;
            } catch (Exception e) {
                log.error("使用过滤表达式格式2失败: {}", e.getMessage());
            }
        }

        //  FIXME 如果两种过滤表达式都失败，尝试不使用过滤器
        if (!success) {
            log.info("getAllRules 尝试不使用过滤器");
            SearchRequest searchRequest = SearchRequest.builder()
                    .query("*")
                    .topK(100)
                    .build();

            ruleDocuments = vectorStore.similaritySearch(searchRequest);
            log.info("不使用过滤器找到{}个文档", ruleDocuments.size());

            // 如果不使用过滤器，需要手动过滤出规则文档
            ruleDocuments = ruleDocuments.stream()
                    .filter(doc -> {
                        Map<String, Object> metadata = doc.getMetadata();
                        return metadata != null &&
                                ("TASK_RULE".equals(metadata.get("type")) ||
                                        (metadata.get("type") != null && metadata.get("type").toString().contains("TASK_RULE")));
                    })
                    .collect(Collectors.toList());

            log.info("手动过滤后找到{}个规则文档", ruleDocuments.size());
        }

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