<template>
  <div class="ambience-layer" aria-hidden="true">
    <!--
      Beams Background(kokonutd / dorianbaffier,MIT License)
      官方源码:https://kokonutui.com  https://github.com/kokonut-labs/kokonutui
      Vue3 移植:canvas 光束从屏幕底部斜向上流动,hsla 色相按深/浅色模式分布,
      深色模式蓝青色系(hue 190±70,饱和85%/亮度65%),浅色模式更深的蓝紫
      (hue 210±50,饱和75%/亮度45%),双模式均有脉动与柔光罩。
    -->
    <canvas ref="beamCanvas" class="beams-canvas"></canvas>
    <div class="beams-veil"></div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount, watch } from 'vue'
import { useTheme } from '../composables/useTheme'

const { theme } = useTheme()

const beamCanvas = ref(null)
let ctx = null
let rafId = 0
let disposed = false
let beams = []

const MINIMUM_BEAMS = 20
// intensity: subtle 0.7 / medium 0.85 / strong 1.0(原组件同名参数)
const INTENSITY = 1

const isDark = () => theme.value === 'dark'

function createBeam(width, height) {
  const angle = -35 + Math.random() * 10
  const hueBase = isDark() ? 190 : 210
  const hueRange = isDark() ? 70 : 50
  return {
    x: Math.random() * width * 1.5 - width * 0.25,
    y: Math.random() * height * 1.5 - height * 0.25,
    width: 30 + Math.random() * 60,
    length: height * 2.5,
    angle,
    speed: 0.6 + Math.random() * 1.2,
    // 官方 demo 的光束明显可见;此前 0.12+0.16 过淡,主页感知不到动效,提到 demo 同级
    opacity: 0.28 + Math.random() * 0.2,
    hue: hueBase + Math.random() * hueRange,
    pulse: Math.random() * Math.PI * 2,
    pulseSpeed: 0.02 + Math.random() * 0.03
  }
}

function resetBeam(beam, index, totalBeams) {
  if (!beamCanvas.value) return beam
  const canvas = beamCanvas.value
  const column = index % 3
  const spacing = canvas.width / 3
  const hueBase = isDark() ? 190 : 210
  const hueRange = isDark() ? 70 : 50

  beam.y = canvas.height + 100
  beam.x = column * spacing + spacing / 2 + (Math.random() - 0.5) * spacing * 0.5
  beam.width = 100 + Math.random() * 100
  beam.speed = 0.5 + Math.random() * 0.4
  beam.hue = hueBase + (index * hueRange) / totalBeams
  beam.opacity = 0.32 + Math.random() * 0.14
  return beam
}

function drawBeam(beam) {
  ctx.save()
  ctx.translate(beam.x, beam.y)
  ctx.rotate((beam.angle * Math.PI) / 180)

  const pulsingOpacity = beam.opacity * (0.8 + Math.sin(beam.pulse) * 0.2) * INTENSITY
  const gradient = ctx.createLinearGradient(0, 0, 0, beam.length)
  // 浅色模式在纯白底上适度提饱和降亮度,保证光束可见但不刺眼
  const saturation = isDark() ? '85%' : '80%'
  const lightness = isDark() ? '65%' : '42%'

  gradient.addColorStop(0, `hsla(${beam.hue}, ${saturation}, ${lightness}, 0)`)
  gradient.addColorStop(0.1, `hsla(${beam.hue}, ${saturation}, ${lightness}, ${pulsingOpacity * 0.5})`)
  gradient.addColorStop(0.4, `hsla(${beam.hue}, ${saturation}, ${lightness}, ${pulsingOpacity})`)
  gradient.addColorStop(0.6, `hsla(${beam.hue}, ${saturation}, ${lightness}, ${pulsingOpacity})`)
  gradient.addColorStop(0.9, `hsla(${beam.hue}, ${saturation}, ${lightness}, ${pulsingOpacity * 0.5})`)
  gradient.addColorStop(1, `hsla(${beam.hue}, ${saturation}, ${lightness}, 0)`)

  ctx.fillStyle = gradient
  ctx.fillRect(-beam.width / 2, 0, beam.width, beam.length)
  ctx.restore()
}

function updateCanvasSize() {
  const canvas = beamCanvas.value
  if (!canvas) return
  const dpr = window.devicePixelRatio || 1
  const w = window.innerWidth
  const h = window.innerHeight
  canvas.width = w * dpr
  canvas.height = h * dpr
  canvas.style.width = w + 'px'
  canvas.style.height = h + 'px'
  ctx.setTransform(dpr, 0, 0, dpr, 0, 0)
  const totalBeams = MINIMUM_BEAMS * 1.5
  beams = Array.from({ length: totalBeams }, () => createBeam(w, h))
}

function animate() {
  if (disposed || !ctx) return
  const canvas = beamCanvas.value
  ctx.clearRect(0, 0, canvas.width, canvas.height)
  ctx.filter = 'blur(35px)'

  const totalBeams = beams.length
  beams.forEach((beam, index) => {
    beam.y -= beam.speed
    beam.pulse += beam.pulseSpeed
    if (beam.y + beam.length < -100) {
      resetBeam(beam, index, totalBeams)
    }
    drawBeam(beam)
  })
  rafId = requestAnimationFrame(animate)
}

function onResize() {
  updateCanvasSize()
}

// 主题切换时重建光束色相分布(hue 基准不同)
watch(theme, () => {
  if (!beamCanvas.value) return
  updateCanvasSize()
})

onMounted(() => {
  const canvas = beamCanvas.value
  if (!canvas) return
  ctx = canvas.getContext('2d')
  updateCanvasSize()
  rafId = requestAnimationFrame(animate)
  window.addEventListener('resize', onResize)
})

onBeforeUnmount(() => {
  disposed = true
  if (rafId) cancelAnimationFrame(rafId)
  window.removeEventListener('resize', onResize)
})
</script>

<style scoped>
.ambience-layer {
  position: fixed;
  inset: 0;
  z-index: -1;
  overflow: hidden;
  background: var(--background, #0a0a0f);
}

.beams-canvas {
  position: absolute;
  inset: 0;
  filter: blur(12px);
  pointer-events: none;
}

/* 柔光罩:原组件 backdrop blur + 10s 呼吸(原 motion opacity 0.05↔0.15) */
.beams-veil {
  position: absolute;
  inset: 0;
  background: rgb(0 0 0 / 0.05);
  backdrop-filter: blur(50px);
  -webkit-backdrop-filter: blur(50px);
  animation: veil-breathe 10s ease-in-out infinite;
  pointer-events: none;
}
html.light .beams-veil {
  background: rgb(255 255 255 / 0.04);
}

@keyframes veil-breathe {
  0%, 100% { opacity: 0.05; }
  50% { opacity: 0.15; }
}

@media (prefers-reduced-motion: reduce) {
  .beams-veil { animation: none; }
}
</style>
