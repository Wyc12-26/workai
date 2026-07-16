<template>
  <div
    class="file-uploader"
    :class="{ 'is-dragging': isDragging }"
    @dragover.prevent="isDragging = true"
    @dragleave="isDragging = false"
    @drop.prevent="handleDrop"
  >
    <input
      ref="fileInput"
      type="file"
      class="hidden-input"
      @change="handleFileSelect"
      multiple
    />
    
    <div class="upload-area" @click="$refs.fileInput.click()">
      <el-icon :size="24"><UploadFilled /></el-icon>
      <span class="upload-text">点击或拖拽文件到此处上传</span>
      <span class="upload-hint">支持 txt、md、docx 等文本格式</span>
    </div>

    <!-- 已选文件列表 -->
    <div v-if="selectedFiles.length > 0" class="file-list">
      <div
        v-for="(file, index) in selectedFiles"
        :key="index"
        class="file-item"
      >
        <el-icon><Document /></el-icon>
        <span class="file-name">{{ file.name }}</span>
        <span class="file-size">{{ formatSize(file.size) }}</span>
        <el-icon class="remove-btn" @click="removeFile(index)">
          <Close />
        </el-icon>
      </div>
    </div>

    <!-- 上传进度 -->
    <div v-if="uploading" class="upload-progress">
      <el-progress :percentage="progress" :stroke-width="4" />
      <span class="progress-text">上传中...</span>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { uploadFile } from '@/api/file'

const emit = defineEmits(['uploaded'])

const isDragging = ref(false)
const selectedFiles = ref([])
const uploading = ref(false)
const progress = ref(0)

function handleDrop(event) {
  isDragging.value = false
  const files = Array.from(event.dataTransfer.files)
  addFiles(files)
}

function handleFileSelect(event) {
  const files = Array.from(event.target.files)
  addFiles(files)
}

function addFiles(files) {
  selectedFiles.value = [...selectedFiles.value, ...files]
}

function removeFile(index) {
  selectedFiles.value.splice(index, 1)
}

async function uploadSelectedFiles() {
  if (selectedFiles.value.length === 0) return

  uploading.value = true
  progress.value = 0

  try {
    const file = selectedFiles.value[0]
    const fileId = await uploadFile(file)
    progress.value = 100
    
    ElMessage.success('文件上传成功')
    emit('uploaded', fileId, file.name)
    
    // 清空已选文件
    selectedFiles.value = []
  } catch (error) {
    ElMessage.error('文件上传失败，请重试')
  } finally {
    uploading.value = false
    progress.value = 0
  }
}

function formatSize(bytes) {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return Math.round(bytes / Math.pow(k, i) * 100) / 100 + ' ' + sizes[i]
}

// 暴露上传方法供父组件调用
defineExpose({
  uploadSelectedFiles
})
</script>

<style scoped>
.file-uploader {
  border: 2px dashed #dcdfe6;
  border-radius: 8px;
  padding: 16px;
  transition: all 0.3s;
}

.file-uploader.is-dragging {
  border-color: #1890ff;
  background-color: #ecf5ff;
}

.hidden-input {
  display: none;
}

.upload-area {
  display: flex;
  flex-direction: column;
  align-items: center;
  cursor: pointer;
  color: #909399;
}

.upload-area:hover {
  color: #1890ff;
}

.upload-text {
  margin-top: 8px;
  font-size: 14px;
}

.upload-hint {
  margin-top: 4px;
  font-size: 12px;
  color: #c0c4cc;
}

.file-list {
  margin-top: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.file-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px 12px;
  background-color: #f5f7fa;
  border-radius: 4px;
  font-size: 13px;
}

.file-name {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.file-size {
  color: #909399;
  font-size: 12px;
}

.remove-btn {
  cursor: pointer;
  color: #909399;
}

.remove-btn:hover {
  color: #f56c6c;
}

.upload-progress {
  margin-top: 12px;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.progress-text {
  font-size: 12px;
  color: #909399;
}
</style>
