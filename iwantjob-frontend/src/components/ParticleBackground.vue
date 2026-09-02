<template>
  <div ref="containerRef" class="particles-layer" aria-hidden="true"></div>
</template>

<script setup>
// 全局粒子氛围层:直接走 @tsparticles/engine load API,深浅两套参数,随主题热切换
import { ref, onMounted, onBeforeUnmount, watch } from 'vue'
import { tsParticles } from '@tsparticles/engine'
import { loadSlim } from '@tsparticles/slim'
import { useTheme } from '../composables/useTheme'

const containerRef = ref(null)
const { theme } = useTheme()
let tsInstance = null
let disposed = false
let engineReady = null

const base = () => ({
  fullScreen: { enable: false },
  background: { color: 'transparent' },
  fpsLimit: 60,
  detectRetina: true
})

function darkOptions() {
  return {
    ...base(),
    particles: {
      number: { value: window.innerWidth < 768 ? 22 : 54, density: { enable: true, area: 900 } },
      color: { value: ['#f5f5f7', '#2997ff', '#a259ff'] },
      opacity: { value: { min: 0.12, max: 0.5 } },
      size: { value: { min: 0.6, max: 2.2 } },
      move: { enable: true, speed: 0.32, direction: 'top', outModes: 'out', straight: false, random: true },
      links: { enable: true, distance: 130, color: '#ffffff', opacity: 0.06, width: 1 }
    },
    interactivity: {
      events: { onHover: { enable: true, mode: 'grab' }, resize: { enable: true } },
      modes: { grab: { distance: 150, links: { opacity: 0.18 } } }
    }
  }
}

function lightOptions() {
  return {
    ...base(),
    particles: {
      number: { value: window.innerWidth < 768 ? 18 : 44, density: { enable: true, area: 900 } },
      color: { value: ['#8e8e93', '#0071e3', '#7d4fff'] },
      opacity: { value: { min: 0.08, max: 0.26 } },
      size: { value: { min: 0.6, max: 2 } },
      move: { enable: true, speed: 0.28, direction: 'top', outModes: 'out', straight: false, random: true },
      links: { enable: true, distance: 130, color: '#1d1d1f', opacity: 0.05, width: 1 }
    },
    interactivity: {
      events: { onHover: { enable: true, mode: 'grab' }, resize: { enable: true } },
      modes: { grab: { distance: 150, links: { opacity: 0.14 } } }
    }
  }
}

async function render() {
  if (disposed || !engineReady) return
  try {
    if (tsInstance) { await tsInstance.destroy(); tsInstance = null }
    tsInstance = await tsParticles.load({
      id: 'ambient-particles',
      element: containerRef.value,
      options: theme.value === 'light' ? lightOptions() : darkOptions()
    })
  } catch (e) {
    // 粒子层是氛围增强,失败不影响功能
    console.warn('particles render skipped:', e?.message)
  }
}

onMounted(async () => {
  engineReady = loadSlim(tsParticles)
  await engineReady
  await render()
  watch(theme, () => render())
})

onBeforeUnmount(() => {
  disposed = true
  if (tsInstance) { try { tsInstance.destroy() } catch (e) { /* ignore */ } }
})
</script>

<style scoped>
.particles-layer {
  position: fixed;
  inset: 0;
  z-index: 0;
  pointer-events: none;
}
</style>
