<template>
  <div class="loading-indicator">
    <div class="typing-dots">
      <span></span>
      <span></span>
      <span></span>
    </div>
    <span class="loading-text">AI 正在思考...</span>
  </div>
</template>

<style scoped>
.loading-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
}

.typing-dots {
  display: flex;
  gap: 4px;
}

.typing-dots span {
  width: 6px;
  height: 6px;
  background-color: #909399;
  border-radius: 50%;
  animation: typing 1.4s infinite ease-in-out both;
}

.typing-dots span:nth-child(1) {
  animation-delay: 0s;
}

.typing-dots span:nth-child(2) {
  animation-delay: 0.2s;
}

.typing-dots span:nth-child(3) {
  animation-delay: 0.4s;
}

@keyframes typing {
  0%, 80%, 100% {
    transform: scale(0.6);
    opacity: 0.5;
  }
  40% {
    transform: scale(1);
    opacity: 1;
  }
}

.loading-text {
  color: #909399;
  font-size: 14px;
}
</style>
