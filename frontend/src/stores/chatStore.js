import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { sendStreamMessage } from '@/api/chat'

export const useChatStore = defineStore('chat', () => {
  // 状态
  const conversations = ref(JSON.parse(localStorage.getItem('conversations') || '[]'))
  const currentConversationId = ref(localStorage.getItem('currentConversationId') || null)
  const isStreaming = ref(false)
  const currentStreamingMessage = ref(null)

  // 计算属性
  const currentConversation = computed(() => {
    return conversations.value.find(c => c.id === currentConversationId.value)
  })

  const messages = computed(() => {
    return currentConversation.value?.messages || []
  })

  // 方法
  function saveToLocalStorage() {
    localStorage.setItem('conversations', JSON.stringify(conversations.value))
    localStorage.setItem('currentConversationId', currentConversationId.value)
  }

  function createConversation(title = '新对话') {
    const newConversation = {
      id: Date.now().toString(),
      title,
      messages: [],
      createdAt: new Date().toISOString(),
      updatedAt: new Date().toISOString()
    }
    conversations.value.unshift(newConversation)
    currentConversationId.value = newConversation.id
    saveToLocalStorage()
    return newConversation
  }

  function switchConversation(id) {
    currentConversationId.value = id
    saveToLocalStorage()
  }

  function deleteConversation(id) {
    const index = conversations.value.findIndex(c => c.id === id)
    if (index !== -1) {
      conversations.value.splice(index, 1)
      if (currentConversationId.value === id) {
        currentConversationId.value = conversations.value[0]?.id || null
      }
      saveToLocalStorage()
    }
  }

  function addMessage(conversationId, message) {
    const conversation = conversations.value.find(c => c.id === conversationId)
    if (conversation) {
      conversation.messages.push(message)
      conversation.updatedAt = new Date().toISOString()
      saveToLocalStorage()
    }
  }

  function updateLastMessage(conversationId, content) {
    const conversation = conversations.value.find(c => c.id === conversationId)
    if (conversation && conversation.messages.length > 0) {
      const lastMessage = conversation.messages[conversation.messages.length - 1]
      if (lastMessage.role === 'assistant') {
        lastMessage.content = content
        saveToLocalStorage()
      }
    }
  }

  async function sendUserMessage(payload) {
    const content = typeof payload === 'object' ? payload.content : payload
    const fileId = payload?.fileId || null
    const functionName = payload?.functionName || null

    // 确保有当前对话
    if (!currentConversationId.value) {
      createConversation(content.substring(0, 20) + (content.length > 20 ? '...' : ''))
    }

    const userMessage = {
      id: Date.now().toString(),
      role: 'user',
      content,
      timestamp: new Date().toISOString(),
      fileId,
      functionName
    }

    addMessage(currentConversationId.value, userMessage)

    // 如果是第一条消息，更新对话标题
    const conversation = currentConversation.value
    if (conversation.messages.length === 1) {
      conversation.title = content.substring(0, 20) + (content.length > 20 ? '...' : '')
      saveToLocalStorage()
    }

    // 创建 AI 回复占位
    const assistantMessage = {
      id: (Date.now() + 1).toString(),
      role: 'assistant',
      content: '',
      timestamp: new Date().toISOString(),
      isStreaming: true
    }
    addMessage(currentConversationId.value, assistantMessage)

    // 发送消息
    await sendToBackend(content, fileId, functionName, assistantMessage.id)

    return assistantMessage.id
  }

  async function sendToBackend(content, fileId = null, functionName = null, messageId) {
    isStreaming.value = true
    currentStreamingMessage.value = messageId

    const payload = {
      content,
      conversationId: currentConversationId.value,
      fileId: fileId || undefined,
      functionName: functionName || undefined
    }

    let accumulatedContent = ''

    try {
      // sendStreamMessage 现在是 async generator
      for await (const chunk of sendStreamMessage(payload)) {
        accumulatedContent += chunk
        updateLastMessage(currentConversationId.value, accumulatedContent)
      }
    } catch (error) {
      console.error('SSE 错误:', error)
      addMessage(currentConversationId.value, {
        id: (Date.now() + 2).toString(),
        role: 'error',
        content: '发送消息失败，请稍后重试',
        timestamp: new Date().toISOString()
      })
    } finally {
      // 移除流式标记
      const conversation = conversations.value.find(c => c.id === currentConversationId.value)
      if (conversation) {
        const msg = conversation.messages.find(m => m.id === messageId)
        if (msg) {
          msg.isStreaming = false
        }
      }
      saveToLocalStorage()
      isStreaming.value = false
      currentStreamingMessage.value = null
    }
  }

  function clearCurrentConversation() {
    if (currentConversationId.value) {
      const conversation = conversations.value.find(c => c.id === currentConversationId.value)
      if (conversation) {
        conversation.messages = []
        saveToLocalStorage()
      }
    }
  }

  return {
    conversations,
    currentConversationId,
    currentConversation,
    messages,
    isStreaming,
    currentStreamingMessage,
    createConversation,
    switchConversation,
    deleteConversation,
    sendUserMessage,
    clearCurrentConversation
  }
})
