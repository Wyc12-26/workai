<template>
  <div class="chat-page">
    <!-- 消息列表区域 -->
    <div class="messages-container" ref="messagesContainer">
      <div v-if="messages.length === 0" class="empty-state">
        <el-icon :size="64" color="#909399"><ChatDotRound /></el-icon>
        <h3>你好，我是你的 AI 办公助手</h3>
        <p>我可以帮你：文档总结、文案生成、联网检索</p>
        <p class="hint">试试上传文件或直接输入问题开始对话吧！</p>
      </div>

      <div v-else>
        <ChatMessage
          v-for="msg in messages"
          :key="msg.id"
          :role="msg.role"
          :content="msg.content"
          :timestamp="msg.timestamp"
          :streaming="msg.isStreaming"
          :file-id="msg.fileId"
          :file-name="msg.fileName"
        />
        
        <LoadingIndicator v-if="isStreaming" />
      </div>
    </div>

    <!-- 输入框容器 -->
      <div class="input-wrapper">
        <!-- 已选文件显示 -->
        <div v-if="attachedFile" class="attached-file">
          <el-icon><Document /></el-icon>
          <span>{{ attachedFile.name }}</span>
          <el-icon class="remove-btn" @click="removeFile"><Close /></el-icon>
        </div>

        <!-- 输入框和工具栏 -->
        <div class="input-box">
          <div class="input-toolbar">
            <el-tooltip content="请上传 txt、md、doc、docx、pdf 格式的文件" placement="top">
              <el-button text circle @click="$refs.fileInputRef?.click()">
                <el-icon><Paperclip /></el-icon>
              </el-button>
            </el-tooltip>
          </div>
          <el-input
            ref="inputRef"
            v-model="inputText"
            type="textarea"
            :rows="3"
            placeholder="输入你的问题...（Shift+Enter 换行，Enter 发送）"
            @keydown.enter.exact.prevent="handleSend"
          />
        </div>

        <!-- 底部：功能按钮 + 发送按钮 -->
        <div class="input-bottom">
          <div class="function-buttons">
            <el-button
              v-for="func in functions"
              :key="func.type"
              :class="{ active: selectedFunction === func.type }"
              @click="toggleFunction(func.type)"
            >
              <el-icon><component :is="func.icon" /></el-icon>
              {{ func.label }}
            </el-button>
          </div>
          <el-button
            class="send-btn"
            type="primary"
            @click="handleSend"
            :disabled="!inputText.trim()"
          >
            <el-icon><Promotion /></el-icon>
            发送
          </el-button>
        </div>
      </div>

    <!-- 隐藏的文件输入 -->
    <input
      ref="fileInputRef"
      type="file"
      class="hidden-file-input"
      @change="handleFileSelect"
      accept=".txt,.md,.doc,.docx,.pdf"
    />
  </div>

</template>

<script setup>
import { ref, computed, nextTick, watch } from 'vue'
import { useChatStore } from '@/stores/chatStore'
import { uploadFile } from '@/api/file'
import { ElMessage } from 'element-plus'
import ChatMessage from '@/components/ChatMessage.vue'
import LoadingIndicator from '@/components/LoadingIndicator.vue'

const chatStore = useChatStore()
const messagesContainer = ref(null)
const fileInputRef = ref(null)
const inputText = ref('')
const attachedFile = ref(null)
const selectedFunction = ref(null)

const functions = [
  { type: 'summarize', label: '文档总结', icon: 'Document' },
  { type: 'template', label: '文案生成', icon: 'EditPen' },
  { type: 'search', label: '联网检索', icon: 'Search' }
]

const messages = computed(() => chatStore.messages)
const isStreaming = computed(() => chatStore.isStreaming)

// 自动滚动到底部
watch(
  messages,
  async () => {
    await nextTick()
    if (messagesContainer.value) {
      messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
    }
  },
  { deep: true }
)

function removeFile() {
  attachedFile.value = null
}

async function handleFileSelect(event) {
  const file = event.target.files[0]
  if (!file) return

  try {
    const fileId = await uploadFile(file)
    attachedFile.value = { id: fileId, name: file.name }
    ElMessage.success('文件上传成功')
  } catch (error) {
    ElMessage.error('文件上传失败，请重试')
  } finally {
    event.target.value = ''
  }
}

async function handleSend() {
  const content = inputText.value.trim()
  if (!content) return

  const fileId = attachedFile.value?.id || null
  const fileName = attachedFile.value?.name || null

  // 清空输入
  inputText.value = ''
  removeFile()

  // 携带功能类型
  const payload = {
    content,
    fileId,
    functionName: selectedFunction.value
  }

  // 发送消息
  await chatStore.sendUserMessage(payload)

  // 发送后取消选中
  selectedFunction.value = null
}

function toggleFunction(type) {
  selectedFunction.value = selectedFunction.value === type ? null : type
}
</script>

<style scoped>
.chat-page {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: #fff;
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 20px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #909399;
}

.empty-state h3 {
  margin-top: 16px;
  color: #303133;
}

.empty-state p {
  margin-top: 8px;
  font-size: 14px;
}

.hint {
  margin-top: 12px;
  font-size: 13px;
  color: #c0c4cc;
}

.input-wrapper {
  padding: 0 20px 16px;
}

.attached-file {
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 12px;
  margin-bottom: 12px;
  background-color: #f5f7fa;
  border-radius: 4px;
  font-size: 13px;
}

.remove-btn {
  cursor: pointer;
  color: #909399;
}

.remove-btn:hover {
  color: #f56c6c;
}

/* 输入框容器 */
.input-box {
  display: flex;
  align-items: stretch;
  gap: 8px;
  border: 1px solid #dcdfe6;
  border-radius: 8px;
  padding: 8px 12px;
  background-color: #fff;
  transition: border-color 0.2s;
}

.input-box:focus-within {
  border-color: #1890ff;
}

.input-toolbar {
  display: flex;
  align-items: flex-start;
  padding-top: 4px;
}

.input-toolbar .el-button {
  color: #909399;
}

.input-toolbar .el-button:hover {
  color: #1890ff;
}

.input-box .el-textarea__inner {
  flex: 1;
  border: none;
  resize: none;
  box-shadow: none;
  font-size: 14px;
  line-height: 1.6;
}

/* 底部：功能按钮 + 发送 */
.input-bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 12px;
}

.function-buttons {
  display: flex;
  gap: 4px;
}

.function-buttons .el-button {
  font-size: 13px;
  color: #606266;
  border: none;
  padding: 6px 12px;
}

.function-buttons .el-button:hover {
  color: #1890ff;
  background-color: #ecf5ff;
}

.function-buttons .el-button.active {
  color: #1890ff;
  background-color: #ecf5ff;
  font-weight: 500;
}

.send-btn {
  min-width: 80px;
  height: 36px;
  border-radius: 6px;
  font-size: 14px;
}

.hidden-file-input {
  display: none;
}
</style>
