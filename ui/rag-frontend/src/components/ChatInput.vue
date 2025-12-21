<template>
  <div class="d-flex align-center mt-4">
    <v-text-field
      v-model="input"
      label="输入你的问题…"
      variant="outlined"
      class="flex-grow-1 mr-2"
      hide-details
      :disabled="loading"
      @keydown.enter="send"
    />

    <v-btn
      color="primary"
      :loading="loading"
      :disabled="loading || !input.trim()"
      @click="send"
    >
      发送
    </v-btn>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const props = defineProps({
  loading: Boolean
})

const emit = defineEmits(['send'])

const input = ref('')

const send = () => {
  if (!input.value.trim()) return

  emit('send', input.value.trim())
  input.value = ''
}
</script>

<style scoped>
/* 保持简洁 */
</style>
