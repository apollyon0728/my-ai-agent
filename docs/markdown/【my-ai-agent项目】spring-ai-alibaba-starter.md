# Spring AI Alibaba Starter 能力概览

## 依赖配置

```xml
<dependency>
  <groupId>com.alibaba.cloud.ai</groupId>
  <artifactId>spring-ai-alibaba-starter</artifactId>
  <version>1.0.0-GA</version>
</dependency>
```
建议使用 Spring Boot 3.5.3 及以上，Maven 3.6.3 及以上版本。

---

## 主要能力

### 1. 大模型适配与接入
- 支持阿里云百炼系列、通义千问、Qwen 等主流大模型，涵盖文本生成、对话、文生图、文生语音等多模态AI功能。
- Spring Boot 自动装配，快速调用 ChatClient、ChatModel 等核心能力。
- 支持多模型，统一 API（如 OpenAI 等）。

### 2. 知识库与 RAG 解决方案
- 集成向量数据库（OB Cloud Vector、PGVector），支持知识检索、文档问答、上下文增强等 RAG 应用场景。
- 提供文档导入、向量存储、相似性检索等接口。

### 3. 多智能体与 AI 工作流开发
- 支持多 Agent 协作与 AI 工作流编排，适合复杂业务流程自动化与 AI 辅助决策开发。

### 4. AI 可观测性能力
- 集成 ARMS、Langfuse 等平台，监控 AI 调用次数、响应时延、Token 消耗等关键指标，便于性能调优与故障排查。

### 5. 企业级 MCP 集成
- 支持 Nacos MCP 等微服务配置协议，实现 AI 服务的灵活配置与管理，满足企业级安全稳定需求。

### 6. 统一抽象与开发体验
- Spring AI 体系下，底层 API 抽象统一，支持写一次代码兼容多种模型，降低接入门槛，加速 AI 能力落地。

---

## 官方资源与文档

- [Spring AI Alibaba README（官方文档）](https://raw.githubusercontent.com/alibaba/spring-ai-alibaba/master/README-zh.md)
- [Spring AI 1.0 GA 发布说明](https://java2ai.com/blog/spring-ai-100-ga-released)
- [10分钟快速上手示例](https://zhuanlan.zhihu.com/p/12060152893)

---

适合企业级大模型开发、知识库问答、AI智能体、工作流等场景。