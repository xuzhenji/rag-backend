<template>
  <v-app>
    <!-- 顶部导航栏 -->
    <v-app-bar color="primary" dark>
      <v-app-bar-title>RAG Demo System</v-app-bar-title>
    </v-app-bar>

    <!-- 主内容 -->
    <v-main>
      <v-container class="py-4" fluid>
        <ChatPage />
      </v-container>
    </v-main>
  </v-app>
</template>

<script setup lang="ts">
import ChatPage from './pages/ChatPage.vue'
</script>

<style scoped>
/* 全局结构基本保持简单 */
</style>
