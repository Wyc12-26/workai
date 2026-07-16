import axios from 'axios'

/**
 * 上传文件
 * @param {File} file - 要上传的文件
 * @returns {Promise<string>} fileId
 */
export async function uploadFile(file) {
  try {
    const formData = new FormData()
    formData.append('file', file)
    
    // 文件上传不使用全局 axios 实例，避免 Content-Type 冲突
    const response = await axios.post('/api/files/upload', formData, {
      timeout: 60000
    })
    return response.data.fileId
  } catch (error) {
    console.error('文件上传失败:', error)
    throw error
  }
}

export default {
  uploadFile
}
