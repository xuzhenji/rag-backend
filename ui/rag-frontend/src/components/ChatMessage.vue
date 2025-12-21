<template>
  <div :class="['message-container', role]">
    <v-card
      class="pa-3"
      :color="role === 'assistant' ? 'blue-lighten-5' : 'grey-lighten-3'"
      elevation="1"
      max-width="90%"
    >
      <div class="text-body-1" style="white-space: pre-wrap;">
        {{ text }}
      </div>
    </v-card>
  </div>
</template>

<script setup lang="ts">
defineProps({
  role: {
    type: String,
    required: true // 'user' | 'assistant'
  },
  text: {
    type: String,
    required: true
  }
})
</script>

<style scoped>
.message-container {
  display: flex;
  margin-bottom: 12px;
}

/* 用户消息右对齐 */
.message-container.user {
  justify-content: flex-end;
}

/* 助手消息左对齐 */
.message-container.assistant {
  justify-content: flex-start;
}
</style>
