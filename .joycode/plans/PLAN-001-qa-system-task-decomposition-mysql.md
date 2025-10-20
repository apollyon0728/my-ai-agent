# 问答系统任务拆解与MySQL查询集成计划

## 任务摘要
本计划旨在改造现有的问答系统，增加问题拆解功能并与MySQL数据库查询集成。系统将首先分析用户描述的问题（如"绩效奖励成本项数据波动"），将其拆解为具体任务，然后调用相应的MySQL查询工具获取数据。拆解规则可以预先定义并上传到向量数据库，使系统能够根据规则智能拆解问题。这一改造将显著提升系统处理复杂业务问题的能力，特别是在数据分析场景中。

## 实现步骤

### TODO: [设计问题拆解模型]
- [ ] 定义问题拆解接口（TaskDecomposer）
- [ ] 创建基于向量数据库的问题拆解实现类（VectorStoreTaskDecomposer）
- [ ] 设计拆解规则的数据结构和存储格式
- [ ] 实现拆解规则的上传和管理功能

### TODO: [创建MySQL查询工具]
- [ ] 设计数据库查询工具接口（DatabaseQueryTool）
- [ ] 实现MySQL查询执行器（MySQLQueryExecutor）
- [ ] 创建查询模板管理器，支持参数化查询
- [ ] 实现动态日期参数处理（如获取前一天日期）
- [ ] 添加查询结果格式化功能

### TODO: [集成问题拆解与查询执行]
- [ ] 扩展KnowledgeBaseService接口，添加新的问答方法
- [ ] 在KnowledgeBaseServiceImpl中实现新方法
- [ ] 创建查询结果与AI回答的融合逻辑
- [ ] 实现查询执行的错误处理和重试机制

### TODO: [API接口扩展]
- [ ] 在KnowledgeBaseController中添加新的API端点
- [ ] 设计请求和响应数据结构
- [ ] 实现API参数验证和错误处理
- [ ] 添加API文档注释

### TODO: [测试与验证]
- [ ] 编写单元测试验证问题拆解功能
- [ ] 测试MySQL查询工具的执行效果
- [ ] 进行集成测试验证端到端流程
- [ ] 使用示例问题（如"绩效奖励成本项数据波动"）进行功能验证

## 详细任务分解

### TODO: [设计问题拆解模型]
- [ ] 定义TaskDecomposer接口
  ```java
  public interface TaskDecomposer {
      List<DecomposedTask> decompose(String question);
  }
  ```
- [ ] 定义DecomposedTask类，包含任务描述、查询类型、参数等字段
- [ ] 创建VectorStoreTaskDecomposer实现类，利用向量数据库存储和检索拆解规则
- [ ] 设计拆解规则的JSON格式，包含规则描述、匹配模式、查询模板等
- [ ] 实现规则匹配算法，支持语义相似度匹配

### TODO: [创建MySQL查询工具]
- [ ] 设计DatabaseQueryTool接口
  ```java
  public interface DatabaseQueryTool {
      QueryResult executeQuery(String queryTemplate, Map<String, Object> params);
  }
  ```
- [ ] 实现MySQLQueryExecutor类，支持执行SQL查询
- [ ] 创建QueryTemplate类管理SQL模板
- [ ] 实现DateParameterProcessor处理日期参数（如获取前一天日期）
- [ ] 设计QueryResult类存储和格式化查询结果

### TODO: [集成问题拆解与查询执行]
- [ ] 在KnowledgeBaseService接口中添加新方法
  ```java
  QAResponse getAnswerWithTaskDecomposition(String question, String modelType);
  ```
- [ ] 在KnowledgeBaseServiceImpl中实现该方法
- [ ] 创建TaskExecutionOrchestrator协调任务执行
- [ ] 实现查询结果与AI回答的融合逻辑
- [ ] 添加执行日志记录功能

### TODO: [API接口扩展]
- [ ] 在KnowledgeBaseController中添加新的API端点
  ```java
  @PostMapping("/qaDecompose")
  public ResponseEntity<QAResponse> askQuestionWithDecomposition(@RequestBody QARequest request) {
      QAResponse response = knowledgeBaseService.getAnswerWithTaskDecomposition(
          request.getQuestion(), request.getModelType());
      return ResponseEntity.ok(response);
  }
  ```
- [ ] 扩展QARequest和QAResponse类，添加任务拆解相关字段
- [ ] 实现参数验证逻辑
- [ ] 添加详细的API文档注释

### TODO: [测试与验证]
- [ ] 编写TaskDecomposerTest测试问题拆解功能
- [ ] 创建MySQLQueryExecutorTest测试数据库查询功能
- [ ] 实现集成测试验证完整流程
- [ ] 使用示例问题进行端到端测试

## 文档要求
- [ ] 更新API文档，添加新接口的说明
- [ ] 编写拆解规则的定义指南
- [ ] 创建MySQL查询模板的编写规范
- [ ] 添加示例和使用说明