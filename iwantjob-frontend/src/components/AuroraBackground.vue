<template>
  <div class="ambience-layer" aria-hidden="true">
    <!-- ===== 深色:Beams 光束背景(kokonutd/BeamsBackground 风格) ===== -->
    <canvas v-show="isDark" ref="beamCanvas" class="beams-canvas"></canvas>
    <div v-show="isDark" class="beams-veil"></div>

    <!-- ===== 浅色:Aurora 极光背景(manuarora700/AuroraBackground 风格) ===== -->
    <div v-show="!isDark" class="aurora-css"></div>
    <div v-show="!isDark" class="aurora-vignette"></div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue'
import { useTheme } from '../composables/useTheme'

const { theme } = useTheme()
const isDark = computed(() => theme.value === 'dark')

const beamCanvas = ref(null)
let rafId = null
let disposed = false

/* ---------- Beams:低分辨率 canvas + CSS blur,GPU 友好 ---------- */
// 主色取自 kokonutd 原组件 canvas 实测采样:蓝/天蓝/靛/青
const BEAM_COLORS = ['#3b82f6', '#60a5fa', '#6366f1', '#818cf8', '#22d3ee', '#a78bfa']
let beams = []

function initBeams(w, h) {
  beams = []
  const count = 9
  for (let i = 0; i < count; i++) {
    beams.push({
      x: (i / count) * w + (Math.random() - 0.5) * w * 0.12,
      angle: (-32 + (Math.random() - 0.5) * 14) * (Math.PI / 180),
      width: w * (0.05 + Math.random() * 0.09),
      color: BEAM_COLORS[i % BEAM_COLORS.length],
      speed: 0.18 + Math.random() * 0.5,
      phase: Math.random() * Math.PI * 2,
      sway: 30 + Math.random() * 90
    })
  }
}

function drawBeams(t) {
  const canvas = beamCanvas.value
  if (!canvas) return
  const ctx = canvas.getContext('2d')
  const w = canvas.width, h = canvas.height
  ctx.clearRect(0, 0, w, h)
  ctx.fillStyle = '#0a0a0f'
  ctx.fillRect(0, 0, w, h)
  ctx.globalCompositeOperation = 'lighter'
  for (const b of beams) {
    const off = Math.sin(t * 0.00022 * b.speed + b.phase) * b.sway
    ctx.save()
    ctx.translate(b.x + off, -h * 0.15)
    ctx.rotate(b.angle)
    const grad = ctx.createLinearGradient(-b.width / 2, 0, b.width / 2, 0)
    grad.addColorStop(0, 'rgba(0,0,0,0)')
    grad.addColorStop(0.5, b.color)
    grad.addColorStop(1, 'rgba(0,0,0,0)')
    ctx.fillStyle = grad
    ctx.fillRect(-b.width / 2, 0, b.width, h * 1.8)
    ctx.restore()
  }
  ctx.globalCompositeOperation = 'source-over'
}

function startBeams() {
  const canvas = beamCanvas.value
  if (!canvas) return
  // 低分辨率:模糊后无感知,绘制成本除以 4
  const scale = 0.28
  canvas.width = Math.floor(window.innerWidth * scale)
  canvas.height = Math.floor(window.innerHeight * scale)
  initBeams(canvas.width, canvas.height)
  const loop = (t) => {
    if (disposed || !isDark.value) return
    drawBeams(t)
    rafId = requestAnimationFrame(loop)
  }
  rafId = requestAnimationFrame(loop)
}

function stopBeams() {
  if (rafId) { cancelAnimationFrame(rafId); rafId = null }
}

function onResize() {
  if (isDark.value) { stopBeams(); startBeams() }
}

onMounted(() => {
  if (isDark.value) startBeams()
  window.addEventListener('resize', onResize)
})

onBeforeUnmount(() => {
  disposed = true
  stopBeams()
  window.removeEventListener('resize', onResize)
})
</script>

<style scoped>
.ambience-layer {
  position: fixed;
  inset: 0;
  z-index: -1;
  overflow: hidden;
  background: var(--background);
}

/* ===== 深色 Beams ===== */
.beams-canvas {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  filter: blur(46px);
  opacity: 0.9;
}
.beams-veil {
  /* 原组件的柔光罩:极淡白 + 大半径 backdrop blur */
  position: absolute;
  inset: 0;
  background: rgba(10, 10, 15, 0.18);
  backdrop-filter: blur(46px);
  -webkit-backdrop-filter: blur(46px);
  pointer-events: none;
}

/* ===== 浅色 Aurora(Aceternity 参数:100deg 重复渐变 + 60s 横向流动) ===== */
.aurora-css {
  position: absolute;
  inset: 0;
  background-image: repeating-linear-gradient(
    100deg,
    #ffffff 0%, #ffffff 7%,
    #a78bfa 8%, #a78bfa 12%,
    #ffffff 13%, #ffffff 20%,
    #d8b4fe 21%, #d8b4fe 25%,
    #ffffff 26%, #ffffff 33%,
    #93c5fd 34%, #93c5fd 38%,
    #ffffff 39%, #ffffff 46%,
    #c084fc 47%, #c084fc 51%,
    #ffffff 52%, #ffffff 59%,
    #7dd3fc 60%, #7dd3fc 64%,
    #ffffff 65%, #ffffff 72%,
    #d8b4fe 73%, #d8b4fe 77%,
    #ffffff 78%, #ffffff 85%,
    #a78bfa 86%, #a78bfa 90%,
    #ffffff 91%, #ffffff 100%
  );
  background-size: 200% 100%;
  background-position: 50% 50%;
  animation: aurora-flow 60s linear infinite;
  opacity: 0.75;
}
@keyframes aurora-flow {
  0% { background-position: 50% 50%; }
  100% { background-position: 350% 50%; }
}

/* 径向白色渐隐(原组件 showRadialGradient) */
.aurora-vignette {
  position: absolute;
  inset: 0;
  background: radial-gradient(ellipse 80% 70% at 50% 42%, rgba(255, 255, 255, 0.92) 20%, rgba(255, 255, 255, 0.55) 55%, rgba(255, 255, 255, 0.15) 100%);
}

@media (prefers-reduced-motion: reduce) {
  .aurora-css { animation: none; }
}
</style>
