<template>
  <v-row class="h-100">

    <!-- 左侧：聊天区域 -->
    <v-col cols="12" md="8">
      <v-card class="pa-4 h-100 d-flex flex-column">

        <!-- 顶部工具栏（新增上传按钮） -->
        <div class="d-flex justify-end mb-3">
          <FileUpload />
        </div>

        <!-- 消息展示区 -->
        <div class="flex-grow-1 overflow-y-auto pr-2">
          <ChatMessage
            v-for="(msg, index) in messages"
            :key="index"
            :role="msg.role"
            :text="msg.text"
          />
        </div>

        <!-- 输入框 -->
        <ChatInput
          :loading="loading"
          @send="handleSend"
        />
      </v-card>
    </v-col>

    <!-- 右侧：RAG 查询内容 -->
    <v-col cols="12" md="4">
      <RAGQueryPanel :retrievedChunks="retrievedChunks" />
    </v-col>

  </v-row>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import ChatMessage from '../components/ChatMessage.vue'
import ChatInput from '../components/ChatInput.vue'
import RAGQueryPanel from '../components/RAGQueryPanel.vue'
import FileUpload from '../components/FileUpload.vue'   // 新增
import http from '../api/http'
import { streamChat } from '../api/streamChat'



// 聊天消息
const messages = ref([
  { role: 'assistant', text: '你好，这是 RAG Demo，你可以提问，也可以上传知识库文档。' }
])

const retrievedChunks = ref([])
const loading = ref(false)
const controller = ref<AbortController | null>(null)

// 暂停按钮
const stop = () => {
  controller.value?.abort()
  loading.value = false
}

// 发送消息逻辑保持不变
const handleSend = async (text: string) => {
  if (!text || loading.value) return

  messages.value.push({ role: 'user', text })

  // const assistantMsg = reactive({ role: 'assistant', text: '' })
  // messages.value.push(assistantMsg)
  const assistantIndex = messages.value.length
  messages.value.push({ role: 'assistant', text: '' })


  loading.value = true
  controller.value = new AbortController()

  try {
    streamChat({
      url: '/api/chat/ragDb',
      payload: { message: text },
      signal: controller.value.signal,
      onMessage(chunk) {
        // assistantMsg.text += chunk
        // console.log('流式返回值chunk:',assistantMsg.text)
         messages.value[assistantIndex].text += chunk
        // console.log('流式返回值chunk:',messages.value[assistantIndex].text)
      },

      onError(err) {
        messages.value[assistantIndex].text += '\n❌ 请求失败'
        console.error(err)
      },

      onComplete() {
        loading.value = false
      }

    })

    // const res = await http.post('/api/chat/ragDb', {message: text})
    // console.log(res.data)

    // retrievedChunks.value = data.chunks ?? []

  } catch (err) {
    console.error(err)
    messages.value.push({
      role: 'assistant',
      text: '请求失败，请检查后端。'
    })
  }

  loading.value = false
}
</script>

<style scoped>
.h-100 {
  height: 100%;
}
</style>
