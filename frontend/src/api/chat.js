

/**
 * 发送 SSE 流式消息
 * @param {Object} payload - 消息载荷
 * @returns {AsyncIterableIterator<string>} SSE 流
 */
export async function* sendStreamMessage(payload) {
  try {
    const response = await fetch('/api/chat/stream', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    })

    if (!response.ok) {
      throw new Error(`HTTP ${response.status}: ${response.statusText}`)
    }

    const reader = response.body.getReader()
    const decoder = new TextDecoder()

    while (true) {
      const { done, value } = await reader.read()
      if (done) break

      const text = decoder.decode(value, { stream: true })
      const lines = text.split('\n')

      for (const line of lines) {
        const trimmed = line.trim()
        if (trimmed.startsWith('data:')) {
          yield trimmed.slice(5).trim()
        }
      }
    }
  } catch (error) {
    console.error('SSE 流式请求失败:', error)
    throw error
  }
}

/**
 * 发送非流式消息
 * @param {Object} payload - 消息载荷
 * @returns {Promise<Object>} 响应数据
 */
export async function sendMessage(payload) {
  try {
    const response = await fetch('/api/chat', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    })
    return response.json()
  } catch (error) {
    console.error('消息发送失败:', error)
    throw error
  }
}

export default {
  sendStreamMessage,
  sendMessage
}
