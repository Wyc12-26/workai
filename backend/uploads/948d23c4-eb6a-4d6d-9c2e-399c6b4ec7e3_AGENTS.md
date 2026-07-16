# 轻量企业办公 AI 助手系统 
 ## 项目概述 
本项目是一个基于 Spring AI + Vue3 的轻量级企业办公 Agent。**核心交互模式为单一对话入口 + 后端意图自动路由**，严禁将不同能力拆分为独立前端页面。AI 助手在本项目中仅作为三大能力的执行载体：文档总结、规则化文案生成、联网检索。
 ## 技术栈 
 - 前端：Vue3 + JavaScript + Vite + Element Plus + Pinia + Vue Router + markdown-it 
 - 后端：Spring Boot 3.2 + Spring AI 1.0.0+ 自定义 Function Calling + 任意大模型（通过 API 接入） 
 - AI 模型：通过 Spring AI 配置接入（支持 Qwen / GLM / DeepSeek 等） 
 ## 目录结构 
 ### 前端 (src/)
```text
src/
├── api/
│   ├── chat.js          # SSE流式请求专用封装
│   ├── file.js          # 文件上传独立接口
│   └── index.js         # axios实例与拦截器
├── components/
│   ├── ChatMessage.vue      # 聊天消息气泡组件
│   ├── ToolErrorCard.vue    # 工具异常专用展示组件
│   ├── FileUploader.vue     # 嵌入聊天框的文件上传组件
│   ├── LoadingIndicator.vue # 加载动画组件
│   └── MarkdownRenderer.vue # Markdown渲染组件(已消毒)
├── views/
│   └── ChatPage.vue     # 唯一主交互页面(禁止拆分功能页)
├── stores/
│   └── chatStore.js     # 对话历史与上下文状态管理
├── router/
│   └── index.js         # 仅保留ChatPage及辅助页面路由
├── utils/
│   ├── sseParser.js     # SSE事件解析器(替代promptTemplates)
│   └── markdownUtils.js # Markdown渲染配置
├── styles/
│   └── main.css         # 全局样式
└── App.vue              # 根组件(侧边栏+路由视图)

### 后端 
src/main/
├── resources/
│   └── prompts/                 # 提示词外部化管理(核心)
│       ├── system-prompt.st     # 系统提示词(StringTemplate)
│       └── user-templates/      # 用户提示词模板
│           ├── doc-summarize.st
│           ├── template-gen.st
│           └── web-search.st
└── java/com/example/officeagent/
    ├── controller/
    │   ├── ChatController.java      # 对话接口(SSE/非流式分离)
    │   └── FileController.java      # 文件上传独立接口
    ├── service/
    │   ├── OfficeAssistantService.java
    │   └── impl/
    │       └── OfficeAssistantServiceImpl.java # User Prompt
    渲染+场景路由
    ├── config/
    │   ├── ChatClientConfig.java        # 加载system-prompt.st
    │   └── FunctionCallbackConfig.java  # @Bean @Description
    注册工具
    └── tool/                            # 无状态纯函数工具类
        ├── DocumentSummarizeTool.java
        ├── TemplateGenerateTool.java
        └── WebSearchTool.java

核心架构铁律（AI 必须遵守）
1. 提示词安全与分离原则
System Prompt 绝对后置：系统提示词必须存放在后端 src/main/resources/prompts/system-prompt.st，通过 ChatClient.Builder.defaultSystem() 注入。严禁在前端代码、URL、LocalStorage 或环境变量中出现任何 System Prompt 片段。
User Prompt 后端渲染：前端仅传递结构化参数（如 fileId, templateType, keywords）。用户提示词模板存放在后端 src/main/resources/prompts/user-templates/*.st，由 Service 层负责变量替换与拼接。严禁在前端 utils/ 中编写提示词拼接逻辑。
防注入校验：后端渲染 User Prompt 前，必须对所有用户传入的字符串变量进行转义处理，防止 {} 等特殊字符破坏 StringTemplate 结构。
2. 前端单一入口原则
禁止拆分功能页：文档总结、文案生成、联网检索不得创建独立的 SummarizePage.vue / TemplatePage.vue / SearchPage.vue。
统一交互流：所有能力通过 ChatPage.vue 的统一对话流承载。辅助操作（上传文件、选模板）以组件形式嵌入聊天框或侧边栏，最终转化为对话消息发送。
API 调用规范：所有 HTTP 请求必须通过 src/api/ 封装函数发起，组件内禁止直接使用 axios/fetch。SSE 流式接口与非流式接口必须分文件管理。
3. Spring AI 工具注册规范
版本要求：必须使用 Spring AI 1.0.0+ GA 稳定版，禁止使用 M/SNAPSHOT 版本。
注册方式：工具必须通过 @Bean @Description("...") Function<Input, Output> 方式在 FunctionCallbackConfig.java 中注册。禁止使用过时的 @Tool 注解或手动构建 FunctionCallback。
工具纯函数：所有 Tool Bean 必须是幂等的纯函数，相同输入永远返回相同输出，禁止在工具内部维护状态或执行副作用操作

通信协议约束
SSE 流式接口
/api/chat/stream 必须返回原生 text/event-stream，禁止使用 {code, msg, data} JSON 包装。
正常内容以 data: 事件发送；工具调用失败必须以 event: error 发送结构化错误信息，前端通过 ToolErrorCard.vue 渲染，禁止静默吞没错误或降级为普通消息气泡。
前端必须使用专用 SSE 解析器（src/utils/sseParser.js），禁止用普通 fetch 读取流。
文件上传协议
聊天流中禁止传输文件二进制数据。
必须先调用 POST /api/files/upload 获取 fileId，再在聊天消息中以文本引用形式传递 fileId。
后端接收文件后必须通过 Tika/OCR 提取纯文本，原始文件加密存储或即时删除，提取结果缓存 TTL ≤ 1小时。

约束项	具体要求	实现位置
上下文记忆	Token窗口滑动策略，非固定条数	MessageWindowChatMemory / Token Advisor
输出截断	单次响应 ≤ 2000 字符	后端 .take(2000) + System Prompt 双重约束
敏感信息	禁止复述薪资/密码/密钥	System Prompt 安全红线 + 后端过滤拦截器
工具超时	单工具执行 ≤ 30s，超时返回明确错误	Tool Bean 内部 try-catch + CompletableFuture
API地址	禁止硬编码，通过Vite proxy/环境变量注入	vite.config.js / .env.*

AI 在执行“新增工具/页面/接口”任务时，必须按以下顺序检查：
新增 AI 工具
在 tool/ 创建工具类，确保为无状态纯函数
在 FunctionCallbackConfig.java 用 @Bean @Description 注册
在 prompts/user-templates/ 新增对应 User Prompt 模板
在 OfficeAssistantServiceImpl 中添加场景路由与模板渲染逻辑
更新 system-prompt.st 中的工具契约表
无需修改前端页面，仅需确认 ChatPage 能正确渲染新工具的返回格式
新增前端组件
使用 <script setup> + scoped CSS
优先复用 Element Plus 组件，禁止引入重型第三方库
如涉及 API，先在 src/api/ 添加封装函数
如涉及全局状态，在 src/stores/ 扩展 Store，禁止组件间 props 透传超过 2 层
⚠️ AI 禁止行为清单
❌ 禁止建议将 System Prompt 放到前端常量文件
❌ 禁止为单个 AI 能力创建独立路由页面
❌ 禁止在 SSE 接口返回 JSON 包装格式
❌ 禁止使用 Spring AI M7 或更早版本的 API
❌ 禁止在工具类中注入 HttpServletRequest/Response
❌ 禁止省略 try-catch 直接调用外部 API
❌ 禁止在 Markdown 渲染组件中使用 v-html 未消毒内容


 