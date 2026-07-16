<template>
  <div class="markdown-content" v-html="renderedMarkdown"></div>
</template>

<script setup>
import { computed } from 'vue'
import { markdownRenderer } from '@/utils/markdownUtils'

const props = defineProps({
  content: {
    type: String,
    default: ''
  }
})

const renderedMarkdown = computed(() => {
  if (!props.content) return ''
  let content = props.content
  content = content.replace(/\n{3,}/g, '\n\n')
  return markdownRenderer.render(content)
})
</script>

<style scoped>
.markdown-content {
  line-height: 1.8;
  word-wrap: break-word;
  font-size: 14px;
  color: #303133;
}

.markdown-content :deep(p) {
  margin-bottom: 12px;
  text-indent: 0;
}

.markdown-content :deep(h1) {
  font-size: 20px;
  font-weight: 600;
  margin-top: 20px;
  margin-bottom: 12px;
  padding-bottom: 6px;
  border-bottom: 1px solid #e8e8e8;
  color: #1f1f1f;
}

.markdown-content :deep(h2) {
  font-size: 18px;
  font-weight: 600;
  margin-top: 18px;
  margin-bottom: 10px;
  color: #1f1f1f;
}

.markdown-content :deep(h3) {
  font-size: 16px;
  font-weight: 600;
  margin-top: 16px;
  margin-bottom: 8px;
  color: #1f1f1f;
}

.markdown-content :deep(h4),
.markdown-content :deep(h5),
.markdown-content :deep(h6) {
  font-size: 14px;
  font-weight: 600;
  margin-top: 14px;
  margin-bottom: 8px;
  color: #1f1f1f;
}

.markdown-content :deep(strong),
.markdown-content :deep(b) {
  font-weight: 600;
  color: #1f1f1f;
}

.markdown-content :deep(code) {
  background-color: #f5f7fa;
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 13px;
  font-family: 'Consolas', 'Monaco', monospace;
}

.markdown-content :deep(pre) {
  background-color: #1f2937;
  padding: 16px;
  border-radius: 8px;
  overflow-x: auto;
  margin: 12px 0;
}

.markdown-content :deep(pre code) {
  background: none;
  padding: 0;
  color: #e5e7eb;
}

.markdown-content :deep(ul),
.markdown-content :deep(ol) {
  padding-left: 24px;
  margin-bottom: 12px;
}

.markdown-content :deep(li) {
  margin-bottom: 6px;
}

.markdown-content :deep(li ul),
.markdown-content :deep(li ol) {
  margin-top: 6px;
  padding-left: 20px;
}

.markdown-content :deep(blockquote) {
  border-left: 4px solid #1890ff;
  padding: 10px 16px;
  margin: 12px 0;
  background-color: #f5f7fa;
  border-radius: 0 4px 4px 0;
  color: #606266;
}

.markdown-content :deep(a) {
  color: #1890ff;
  text-decoration: none;
}

.markdown-content :deep(a:hover) {
  text-decoration: underline;
}

.markdown-content :deep(img) {
  max-width: 100%;
  height: auto;
  border-radius: 4px;
  margin: 8px 0;
}

.markdown-content :deep(hr) {
  border: none;
  border-top: 1px solid #e8e8e8;
  margin: 20px 0;
}

.markdown-content :deep(table) {
  width: 100%;
  border-collapse: collapse;
  margin: 12px 0;
  font-size: 13px;
}

.markdown-content :deep(th),
.markdown-content :deep(td) {
  border: 1px solid #dcdfe6;
  padding: 8px 12px;
  text-align: left;
}

.markdown-content :deep(th) {
  background-color: #f5f7fa;
  font-weight: 600;
}
</style>
