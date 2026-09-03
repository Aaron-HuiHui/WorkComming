<template>
  <div class="ambience-layer" aria-hidden="true">
    <!--
      Animated Gradient 背景(tom_ui/animated-gradient 风格)
      官方预览:https://21st.dev/@tom_ui/components/animated-gradient
      效果:纯黑底 + 若干大面积彩色光晕缓慢漂移(蓝为主,青/紫点缀),
      亮蓝实测采样 RGB(146,197,255)。canvas 低分辨率绘制 + CSS 放大,GPU 友好。
    -->
    <canvas ref="canvas" class="ag-canvas"></canvas>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'
import { useTheme } from '../composables/useTheme'

const { theme } = useTheme()
const canvas = ref(null)
let ctx = null
let rafId = 0
let disposed = false

// 光晕定义:大半径彩色圆,缓慢绕自身基准点漂移
// 深色模式黑底上亮色;浅色模式改用饱和度更高的中亮度色(白底可辨)
const BLOBS_DARK = [
  { color: '146, 197, 255', r: 0.52, bx: 0.72, by: 0.30, speed: 0.00007, phase: 0.0, amp: 0.16 },
  { color: '96, 165, 250',  r: 0.46, bx: 0.16, by: 0.72, speed: 0.00009, phase: 2.1, amp: 0.13 },
  { color: '129, 140, 248', r: 0.40, bx: 0.45, by: 0.85, speed: 0.00006, phase: 4.2, amp: 0.15 },
  { color: '103, 232, 249', r: 0.30, bx: 0.85, by: 0.80, speed: 0.00011, phase: 1.3, amp: 0.10 },
  { color: '167, 139, 250', r: 0.28, bx: 0.35, by: 0.20, speed: 0.00008, phase: 5.5, amp: 0.12 }
]
const BLOBS_LIGHT = [
  { color: '59, 130, 246',  r: 0.52, bx: 0.72, by: 0.30, speed: 0.00007, phase: 0.0, amp: 0.16 },
  { color: '37, 99, 235',   r: 0.46, bx: 0.16, by: 0.72, speed: 0.00009, phase: 2.1, amp: 0.13 },
  { color: '99, 102, 241',  r: 0.40, bx: 0.45, by: 0.85, speed: 0.00006, phase: 4.2, amp: 0.15 },
  { color: '6, 182, 212',   r: 0.30, bx: 0.85, by: 0.80, speed: 0.00011, phase: 1.3, amp: 0.10 },
  { color: '139, 92, 246',  r: 0.28, bx: 0.35, by: 0.20, speed: 0.00008, phase: 5.5, amp: 0.12 }
]
// 浅色模式下光晕作底色时透明度降低,不干扰前景内容
// 深色 alpha 0.9 对齐参考组件的亮蓝观感(实测采样 RGB 146,197,255 全亮)
const ALPHA = () => (theme.value === 'dark' ? 0.9 : 0.30)
const blobs = () => (theme.value === 'dark' ? BLOBS_DARK : BLOBS_LIGHT)

function resize() {
  const c = canvas.value
  if (!c) return
  // 低分辨率绘制,CSS 拉伸 + 轻模糊:成本极低且渐变天然平滑
  const scale = 0.2
  c.width = Math.max(2, Math.floor(window.innerWidth * scale))
  c.height = Math.max(2, Math.floor(window.innerHeight * scale))
}

function draw(t) {
  if (disposed || !ctx) return
  const c = canvas.value
  const w = c.width, h = c.height
  const alpha = ALPHA()
  const list = blobs()

  // 底色:深色纯黑 / 浅色纯白
  ctx.globalCompositeOperation = 'source-over'
  ctx.fillStyle = theme.value === 'dark' ? '#050505' : '#f5f6fa'
  ctx.fillRect(0, 0, w, h)

  // 光晕叠加(加色混合,深色下尤为透亮)
  ctx.globalCompositeOperation = theme.value === 'dark' ? 'lighter' : 'source-over'
  for (const b of list) {
    const x = (b.bx + Math.sin(t * b.speed + b.phase) * b.amp) * w
    const y = (b.by + Math.cos(t * b.speed * 0.8 + b.phase) * b.amp) * h
    const radius = b.r * Math.max(w, h)
    const g = ctx.createRadialGradient(x, y, 0, x, y, radius)
    g.addColorStop(0, `rgba(${b.color}, ${alpha})`)
    g.addColorStop(0.35, `rgba(${b.color}, ${alpha * 0.45})`)
    g.addColorStop(1, `rgba(${b.color}, 0)`)
    ctx.fillStyle = g
    ctx.beginPath()
    ctx.arc(x, y, radius, 0, Math.PI * 2)
    ctx.fill()
  }
  rafId = requestAnimationFrame(draw)
}

function onResize() {
  resize()
}

onMounted(() => {
  const c = canvas.value
  if (!c) return
  ctx = c.getContext('2d')
  resize()
  rafId = requestAnimationFrame(draw)
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
  /* 背景完全由 canvas 承担;此处不留实色兜底,避免盖住 canvas */
  background: transparent;
}

.ag-canvas {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  /* 低分辨率(0.2x)放大天然平滑,无需再叠 CSS blur——
     叠加 blur 会把光晕核心摊薄导致整体过暗 */
  transform: scale(1.06);
}

@media (prefers-reduced-motion: reduce) {
  .ag-canvas { animation: none; }
}
</style>
