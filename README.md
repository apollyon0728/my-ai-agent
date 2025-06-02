# My AI Agent 🤖

## 项目介绍

My AI Agent 是一个基于 Spring Boot 和 Spring AI 框架构建的智能代理服务，集成了现代大模型能力，提供对话记忆、多模态交互等功能。本项目通过整合阿里云灵积大模型服务，实现了智能化的人机交互体验，并支持基于 RAG（检索增强生成）的知识库问答能力。

## 系统架构

项目采用模块化设计，主要包括：

- **核心服务模块**：基于 Spring Boot 的 Web 服务
- **AI 对话模块**：集成 Spring AI 与阿里云大模型能力
- **记忆持久化模块**：实现会话状态的本地文件持久化
- **图像搜索服务**：独立的 MCP 服务模块
- **知识库模块**：基于 PGVector 的向量数据库存储和检索服务
- **智能代理模块**：实现 AI 自主决策和工具调用能力

## 核心功能

- ✅ **智能对话**：接入阿里云灵积模型，支持自然语言交互
- ✅ **对话记忆**：基于文件系统的会话记忆持久化，保持上下文连贯性
- ✅ **图像搜索**：独立模块支持基于图像的搜索功能
- ✅ **智能代理**：
  - 支持 AI 自主决策和任务规划
  - 多轮对话中的工具调用能力
  - 基于 ReAct 模式的推理和执行
  - 灵活的工具注册和使用机制
- ✅ **知识库问答**：
  - 支持 Markdown 文档上传和管理
  - 基于 RAG 技术的智能问答
  - 使用 PGVector 进行高效的向量检索
  - 支持文档的增删改查操作

## 技术栈

- **后端框架**：Spring Boot 3.4.x
- **AI 框架**：Spring AI (1.0.0-M6.x)
- **大模型接入**：
  - 阿里云 DashScope SDK (2.19.1)
  - Spring AI Alibaba (1.0.0-M6.1)
  - Ollama 集成
- **向量数据库**：
  - PostgreSQL with pgvector 扩展
  - Spring AI VectorStore 集成
- **序列化工具**：Kryo
- **构建工具**：Maven 3.9.9
- **其他工具**：
  - Hutool (5.8.37)
  - Lombok
- **运行环境**：Java 21

## 快速开始

### 环境要求

- JDK 21+
- Maven 3.9+
- 阿里云账号和 API Key（用于灵积模型访问）

### 安装与运行

1. 克隆项目到本地

```bash
git clone https://github.com/your-username/my-ai-agent.git
cd my-ai-agent
```

2. 配置环境变量

```bash
# 阿里云 API Key（必需）
export ALIBABA_API_KEY=your_api_key_here
```

3. 构建项目

```bash
./mvnw clean package
```

4. 运行主应用

```bash
java -jar target/my-ai-agent-0.0.1-SNAPSHOT.jar
```

5. 运行图像搜索服务（可选）

```bash
cd my-image-search-mcp-server
../mvnw spring-boot:run
```

## 项目结构

```
my-ai-agent/
├── src/main/java/com/yam/myaiagent/
│   ├── chatmemory/          # 对话记忆实现
│   ├── constant/            # 常量定义
│   ├── controller/          # HTTP 接口控制器
│   ├── service/             # 业务逻辑服务
│   └── MyAiAgentApplication.java  # 应用入口
├── my-image-search-mcp-server/    # 图像搜索服务模块
├── .mvn/                   # Maven 包装器配置
├── pom.xml                 # Maven 项目配置
└── README.md               # 项目说明文档
```

## 配置说明

主要配置项包括：

- **聊天记忆存储路径**：默认为项目根目录下的 `/tmp` 文件夹
- **模型参数**：可在应用配置中调整大模型的参数设置
- **服务端口**：默认为 Spring Boot 标准端口 8080
- **向量数据库配置**：
  - PostgreSQL 连接信息
  - pgvector 相关参数设置

## API 接口

### 健康检查

```
GET /health
响应: "ok"
```

### 知识库管理接口

```
# 上传文档
POST /api/knowledge/upload
Content-Type: multipart/form-data

# 获取文档列表
GET /api/knowledge/documents

# 删除文档
DELETE /api/knowledge/documents/{documentId}

# 知识库问答
POST /api/knowledge/qa
Content-Type: application/json
{
    "question": "您的问题"
}
```

## 高级特性

### 文件持久化对话记忆

项目实现了基于 Kryo 序列化的文件持久化对话记忆机制，支持会话状态的保存与恢复，保证了对话的连贯性和上下文理解。

### 智能代理系统

- 基于 ReAct（Reasoning and Acting）模式的智能代理实现
- 支持多轮对话中的自主决策和工具调用
- 灵活的工具注册机制，支持动态扩展
- 状态管理和执行流程控制
- 支持流式输出和异步处理

### 图像搜索服务

独立的图像搜索模块采用 Spring AI MCP 服务架构，提供多模态交互能力。

### RAG 知识库问答

- 基于 PGVector 的高性能向量存储
- 支持文档的语义检索和相关性排序
- 智能查询重写优化检索效果
- 支持多种文档格式的处理和向量化

## 贡献指南

欢迎参与项目贡献！请遵循以下步骤：

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/amazing-feature`)
3. 提交更改 (`git commit -m 'Add some amazing feature'`)
4. 推送到分支 (`git push origin feature/amazing-feature`)
5. 创建 Pull Request

## 许可证

本项目基于 Apache License 2.0 开源，详细内容请查看 LICENSE 文件。

## 联系方式

若有任何问题或建议，欢迎通过 Issues 或以下方式联系我们：

- 邮箱: xzhuzhu961@gmail.com

---

祝您使用愉快！🚀
