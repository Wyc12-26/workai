<template>
  <div class="chat-message" :class="[role, { streaming }]">
    <!-- 头像 -->
    <div class="avatar">
      <el-avatar :size="36">
        <el-icon v-if="role === 'user'"><User /></el-icon>
        <el-icon v-else-if="role === 'assistant'"><Headset /></el-icon>
        <el-icon v-else><WarningFilled /></el-icon>
      </el-avatar>
    </div>

    <!-- 消息内容 -->
    <div class="message-content">
      <!-- 文件引用卡片 -->
      <div v-if="fileId" class="file-reference">
        <el-icon :size="16"><Document /></el-icon>
        <span>{{ fileName || '已上传文件' }}</span>
      </div>

      <!-- 用户文本消息 -->
      <div v-if="role === 'user'" class="message-text">
        {{ content }}
      </div>

      <!-- AI 消息（支持 Markdown） -->
      <div v-if="role === 'assistant'" class="message-markdown">
        <MarkdownRenderer :content="content" />
        <span v-if="streaming" class="cursor"></span>
      </div>

      <!-- 错误消息 -->
      <div v-if="role === 'error'" class="error-content">
        <ToolErrorCard :content="content" />
      </div>

      <!-- 时间戳 -->
      <div class="message-time">{{ formatTime(timestamp) }}</div>
    </div>
  </div>
</template>

<script setup>
import MarkdownRenderer from './MarkdownRenderer.vue'
import ToolErrorCard from './ToolErrorCard.vue'

defineProps({
  role: {
    type: String,
    required: true,
    validator: (val) => ['user', 'assistant', 'error'].includes(val)
  },
  content: {
    type: String,
    default: ''
  },
  timestamp: {
    type: String,
    default: ''
  },
  streaming: {
    type: Boolean,
    default: false
  },
  fileId: {
    type: String,
    default: null
  },
  fileName: {
    type: String,
    default: ''
  }
})

function formatTime(timestamp) {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit' })
}
</script>

<style scoped>
.chat-message {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  max-width: 85%;
}

.chat-message.user {
  margin-left: auto;
  flex-direction: row-reverse;
}

.avatar {
  flex-shrink: 0;
}

.message-content {
  display: flex;
  flex-direction: column;
  gap: 6px;
}

.message-text {
  padding: 10px 14px;
  border-radius: 8px;
  line-height: 1.6;
  word-break: break-word;
}

.chat-message.user .message-text {
  background-color: #1890ff;
  color: #fff;
  border-top-right-radius: 2px;
}

.chat-message.assistant .message-markdown {
  padding: 10px 14px;
  background-color: #f5f5f5;
  border-radius: 8px;
  border-top-left-radius: 2px;
}

.file-reference {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 10px;
  background-color: #ecf5ff;
  border: 1px solid #d9ecff;
  border-radius: 4px;
  font-size: 13px;
  color: #606266;
}

.chat-message.user .file-reference {
  background-color: rgba(255, 255, 255, 0.2);
  border-color: rgba(255, 255, 255, 0.3);
  color: #fff;
}

.message-time {
  font-size: 12px;
  color: #909399;
}

.chat-message.user .message-time {
  text-align: right;
}

.cursor {
  display: inline-block;
  width: 2px;
  height: 16px;
  background-color: #1890ff;
  margin-left: 2px;
  animation: blink 1s infinite;
  vertical-align: middle;
}

@keyframes blink {
  0%, 50% { opacity: 1; }
  51%, 100% { opacity: 0; }
}
</style>
