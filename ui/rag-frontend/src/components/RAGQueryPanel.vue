<template>
  <v-card class="pa-3 h-100 d-flex flex-column">
    <h3 class="text-h6 mb-3">📚 检索到的知识片段</h3>

    <!-- 无内容时提示 -->
    <div
      v-if="!retrievedChunks || retrievedChunks.length === 0"
      class="text-subtitle-2 text-grey-darken-1"
    >
      暂无检索结果
    </div>

    <!-- 展示检索片段 -->
    <div
      v-else
      class="flex-grow-1 overflow-y-auto pr-1"
    >
      <v-card
        v-for="(chunk, index) in retrievedChunks"
        :key="index"
        class="mb-2 pa-2"
        color="amber-lighten-5"
        elevation="1"
      >
        <div class="text-body-2" style="white-space: pre-wrap;">
          {{ chunk.text }}
        </div>
      </v-card>
    </div>
  </v-card>
</template>

<script setup lang="ts">
defineProps({
  retrievedChunks: {
    type: Array,
    required: true
  }
})
</script>

<style scoped>
.h-100 {
  height: 100%;
}
</style>
