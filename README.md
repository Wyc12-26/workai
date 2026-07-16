# WorkAI - 企业办公 AI 助手

基于 Spring AI + Vue3 的轻量级企业办公 AI 助手，提供文档总结、文案生成、联网检索三大核心能力。

## 特性

- **单一对话入口**：统一的聊天界面，支持多种 AI 能力
- **流式响应**：SSE (Server-Sent Events) 实时显示 AI 回复
- **文件上传**：支持拖拽和按钮上传，自动提取文档内容
- **对话记忆**：自动保存对话历史，支持上下文理解
- **Markdown 渲染**：结构化的 AI 回复展示
- **前后端分离**：独立的开发环境，便于部署和维护

## 技术栈

### 后端
- Spring Boot 3.2.0
- Java 21
- Spring AI 1.0.0 (ZhipuAI)
- MyBatis Plus 3.5.5
- MySQL 8.0
- Maven

### 前端
- Vue 3.4
- Element Plus 2.9
- Pinia 2.1
- Vue Router 4.3
- Vite 5.4
- Markdown-it 14.1
- Axios 1.7

## 快速开始

### 环境要求

- JDK 21+
- Node.js 16+
- MySQL 8.0+
- Maven 3.6+

### 1. 数据库初始化

创建数据库并执行初始化脚本：

```bash
mysql -u root -p < backend/src/main/resources/db/schema.sql
```

### 2. 后端配置

修改 `backend/src/main/resources/application.properties`：

```properties
# 数据库配置
spring.datasource.url=jdbc:mysql://localhost:3306/aistudy?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&characterEncoding=utf8
spring.datasource.username=root
spring.datasource.password=your_password

# 智谱 AI API Key
spring.ai.zhipuai.api-key=your_api_key
```

### 3. 启动后端

```bash
cd backend
mvn spring-boot:run
```

后端将在 `http://localhost:8080` 启动。

### 4. 启动前端

```bash
cd frontend
npm install
npm run dev
```

前端将在 `http://localhost:3000` 启动。

## 项目结构

```
workai/
├── backend/                    # 后端服务
│   ├── src/main/java/com/workai/
│   │   ├── config/            # 配置类
│   │   │   ├── CorsConfig.java           # 跨域配置
│   │   │   ├── MyBatisPlusConfig.java    # MyBatis Plus 配置
│   │   │   └── MyMetaObjectHandler.java  # 自动填充处理器
│   │   ├── controller/        # 控制器
│   │   │   ├── ChatController.java       # 聊天接口 (SSE流式响应)
│   │   │   └── FileController.java       # 文件上传接口
│   │   ├── dto/               # 数据传输对象
│   │   │   └── ChatRequest.java
│   │   ├── entity/            # 实体类
│   │   │   ├── Conversation.java          # 对话实体
│   │   │   └── Message.java               # 消息实体
│   │   ├── mapper/            # MyBatis Plus Mapper
│   │   ├── service/           # 业务逻辑
│   │   │   ├── ChatService.java           # 聊天服务 (Spring AI)
│   │   │   ├── ConversationService.java   # 对话管理服务
│   │   │   └── FileService.java           # 文件处理服务
│   │   └── exception/         # 异常处理
│   │       └── GlobalExceptionHandler.java
│   ├── src/main/resources/
│   │   ├── db/schema.sql      # 数据库初始化脚本
│   │   └── application.properties  # 应用配置
│   └── pom.xml                # Maven 配置
│
├── frontend/                   # 前端应用
│   ├── src/
│   │   ├── api/               # API 封装
│   │   │   ├── chat.js        # 聊天接口
│   │   │   ├── file.js        # 文件上传接口
│   │   │   └── index.js       # Axios 实例配置
│   │   ├── components/        # Vue 组件
│   │   │   ├── ChatMessage.vue        # 聊天消息气泡
│   │   │   ├── FileUploader.vue       # 文件上传组件
│   │   │   ├── LoadingIndicator.vue   # 加载动画
│   │   │   ├── MarkdownRenderer.vue   # Markdown 渲染器
│   │   │   ├── Sidebar.vue            # 侧边栏
│   │   │   └── ToolErrorCard.vue      # 工具错误卡片
│   │   ├── router/            # 路由配置
│   │   ├── stores/            # Pinia 状态管理
│   │   │   └── chatStore.js
│   │   ├── utils/             # 工具函数
│   │   │   └── markdownUtils.js
│   │   ├── views/             # 页面组件
│   │   │   └── ChatPage.vue     # 聊天主页面
│   │   ├── App.vue            # 根组件
│   │   └── main.js            # 入口文件
│   ├── index.html
│   ├── package.json
│   └── vite.config.js         # Vite 配置
```

## 核心功能

### 1. 文档总结

上传文档后，AI 自动提取核心要点，生成结构化摘要。

**支持格式**：txt, md

### 2. 文案生成

根据需求生成各类办公场景的专业文案。

**使用方式**：选择"文案生成"功能，输入需求描述

### 3. 联网检索

获取互联网最新信息，辅助决策。

**使用方式**：选择"联网检索"功能，输入搜索关键词

### 4. 对话记忆

系统自动保存最近 10 条对话记录，AI 可以理解上下文和指代性问题。

### 5. 文件上传

- 支持拖拽上传
- 支持按钮选择文件
- 文件大小限制：50MB
- 自动提取文件内容供 AI 分析

## API 文档

### 聊天接口

#### 流式响应

```http
POST /api/chat/stream
Content-Type: application/json

{
  "content": "你好",
  "conversationId": "",
  "functionName": "summarize"
}
```

**响应**：SSE 流式数据

#### 获取对话列表

```http
GET /api/chat/conversations
```

#### 获取对话消息

```http
GET /api/chat/conversations/{id}/messages
```

#### 删除对话

```http
DELETE /api/chat/conversations/{id}
```

### 文件接口

#### 上传文件

```http
POST /api/files/upload
Content-Type: multipart/form-data

file: [Binary]
```

**响应**：
```json
{
  "success": true,
  "data": {
    "fileId": "uuid",
    "fileName": "test.md",
    "size": 1024
  }
}
```

## 部署

### 后端打包

```bash
cd backend
mvn clean package -DskipTests
java -jar target/workai-backend-1.0.0.jar
```

### 前端打包

```bash
cd frontend
npm install
npm run build
```

将 `dist` 目录部署到 Nginx 或其他静态服务器。

### Nginx 配置示例

```nginx
server {
    listen 80;
    server_name your-domain.com;

    location / {
        root /path/to/frontend/dist;
        try_files $uri $uri/ /index.html;
    }

    location /api/ {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}
```

## 注意事项

- 系统提示词严格存储在 `ChatService.java` 中，前端不接触任何敏感信息
- SSE 流式响应使用原生格式，不使用 JSON 包装
- 文件上传后自动提取文本内容，原始文件加密存储
- 对话记录自动保存到 MySQL 数据库


