<template>
  <v-card class="pa-3">
    <v-btn
      color="primary"
      @click="selectFile"
      prepend-icon="mdi-upload"
      :loading="uploading"
    >
      上传文档
    </v-btn>

    <input
      ref="fileInput"
      type="file"
      class="d-none"
      accept=".pdf,.txt,.md"
      @change="onFileSelected"
    />
  </v-card>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import http from '../api/http'

const uploading = ref(false)
const fileInput = ref<HTMLInputElement | null>(null)

const selectFile = () => {
  fileInput.value?.click()
}

const onFileSelected = async (event: Event) => {
  const target = event.target as HTMLInputElement
  const file = target.files?.[0]
  if (!file) return

  uploading.value = true

  try {
    const form = new FormData()
    form.append("file", file)

    const res = await http.post(
      "/api/data/upload",
      form,
      {
        headers: {
          "Content-Type": "multipart/form-data"
        }
      }
    )

    alert(`📄 上传成功！已生成 ${res.data.chunks} 个向量片段`)
  } catch (err) {
    console.error(err)
    alert("❌ 上传失败，请检查后端")
  }

  uploading.value = false
}
</script>

<style scoped>
.d-none {
  display: none;
}
</style>
