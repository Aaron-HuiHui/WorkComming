<template>
  <AuroraBackground />
  <ParticleBackground />
  <router-view />
</template>

<script setup>
import AuroraBackground from './components/AuroraBackground.vue'
import ParticleBackground from './components/ParticleBackground.vue'
import { useTheme } from './composables/useTheme'

// 提前初始化主题(首帧前应用,避免闪白)
useTheme()
</script>
