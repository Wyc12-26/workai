<template>
  <div class="tool-error-card">
    <el-alert
      :title="title"
      type="error"
      :description="content"
      show-icon
      closable
      :closable="false"
    />
  </div>
</template>

<script setup>
defineProps({
  title: {
    type: String,
    default: '工具执行出错'
  },
  content: {
    type: String,
    required: true
  }
})
</script>

<style scoped>
.tool-error-card {
  margin: 8px 0;
}
</style>
