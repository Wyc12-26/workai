<template>
  <aside class="sidebar">
    <!-- 顶部：标题 + 新建对话 -->
    <div class="sidebar-header">
      <span class="logo-text">AI 办公助手</span>
      <el-button type="primary" @click="handleNewChat" circle>
        <el-icon><Plus /></el-icon>
      </el-button>
    </div>

    <!-- 中部：对话历史列表 -->
    <div class="conversation-list">
      <div
        v-for="conv in conversations"
        :key="conv.id"
        class="conversation-item"
        :class="{ active: conv.id === currentConversationId }"
        @click="switchConversation(conv.id)"
      >
        <div class="conv-info">
          <el-icon><ChatDotRound /></el-icon>
          <span class="conv-title">{{ conv.title }}</span>
        </div>
        <el-button
          class="delete-btn"
          text
          @click.stop="handleDelete(conv.id)"
        >
          <el-icon><Delete /></el-icon>
        </el-button>
      </div>
      
      <div v-if="conversations.length === 0" class="empty-state">
        <el-empty description="暂无对话记录" :image-size="60" />
      </div>
    </div>

    <!-- 底部：用户信息 -->
    <div class="sidebar-footer">
      <el-avatar :size="32">U</el-avatar>
      <span class="user-name">用户</span>
    </div>
  </aside>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useChatStore } from '@/stores/chatStore'

const router = useRouter()
const chatStore = useChatStore()

const conversations = computed(() => chatStore.conversations)
const currentConversationId = computed(() => chatStore.currentConversationId)

function handleNewChat() {
  chatStore.createConversation()
  router.push('/')
}

function switchConversation(id) {
  chatStore.switchConversation(id)
  router.push('/')
}

function handleDelete(id) {
  chatStore.deleteConversation(id)
}
</script>

<style scoped>
.sidebar {
  width: 260px;
  background-color: #001529;
  color: #fff;
  display: flex;
  flex-direction: column;
  border-right: 1px solid #d9d9d9;
}

.sidebar-header {
  padding: 16px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.logo-text {
  font-size: 16px;
  font-weight: 600;
}

.conversation-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.conversation-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 10px 12px;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.2s;
  margin-bottom: 4px;
}

.conversation-item:hover {
  background-color: rgba(255, 255, 255, 0.08);
}

.conversation-item.active {
  background-color: #1890ff;
}

.conv-info {
  display: flex;
  align-items: center;
  gap: 8px;
  overflow: hidden;
}

.conv-title {
  flex: 1;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
  font-size: 14px;
}

.delete-btn {
  opacity: 0;
  color: #fff;
}

.conversation-item:hover .delete-btn {
  opacity: 1;
}

.empty-state {
  display: flex;
  justify-content: center;
  padding: 40px 0;
}

.sidebar-footer {
  padding: 16px;
  display: flex;
  align-items: center;
  gap: 10px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

.user-name {
  font-size: 14px;
}
</style>
