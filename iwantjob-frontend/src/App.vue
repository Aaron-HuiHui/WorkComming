<template>
  <AnimatedGradient />
  <router-view />
</template>

<script setup>
import AnimatedGradient from './components/AnimatedGradient.vue'
import { useTheme } from './composables/useTheme'

// 提前初始化主题(首帧前应用,避免闪白)
useTheme()
</script>
