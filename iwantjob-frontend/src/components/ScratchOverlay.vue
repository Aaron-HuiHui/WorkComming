<template>
  <teleport to="body">
    <div
      v-if="visible"
      class="scratch-root"
      :class="{ leaving }"
      @mousemove="onMove"
      @mousedown="onDown"
      @mouseup="onUp"
      @mouseleave="onUp"
      @touchmove.prevent="onTouchMove"
      @touchstart.prevent="onTouchStart"
    >
      <!-- 涂层 canvas -->
      <canvas ref="coatCanvas" class="coat-canvas"></canvas>

      <!-- 粒子飞溅 canvas -->
      <canvas ref="sparkCanvas" class="spark-canvas"></canvas>

      <!-- 硬币光标 -->
      <div ref="coinRef" class="coin" :class="{ scratching: isDown }">
        <svg viewBox="0 0 64 64" width="44" height="44">
          <defs>
            <radialGradient id="coin-face" cx="35%" cy="30%" r="80%">
              <stop offset="0%" stop-color="#ffe9a8" />
              <stop offset="45%" stop-color="#f7c948" />
              <stop offset="80%" stop-color="#d4a017" />
              <stop offset="100%" stop-color="#9a7208" />
            </radialGradient>
            <radialGradient id="coin-edge" cx="50%" cy="50%" r="50%">
              <stop offset="70%" stop-color="#f7c948" />
              <stop offset="100%" stop-color="#8a6305" />
            </radialGradient>
          </defs>
          <circle cx="32" cy="32" r="29" fill="url(#coin-edge)" />
          <circle cx="32" cy="32" r="26" fill="url(#coin-face)" />
          <circle cx="32" cy="32" r="21" fill="none" stroke="#b8860b" stroke-width="1.4" stroke-dasharray="3 3" />
          <text x="32" y="38" text-anchor="middle" font-size="14" font-weight="700" fill="#8a6305" font-family="serif">工</text>
          <ellipse cx="24" cy="20" rx="9" ry="5" fill="rgba(255,255,255,.55)" transform="rotate(-25 24 20)" />
        </svg>
      </div>

      <!-- 顶部文案与跳过 -->
      <div class="scratch-hint">
        <p class="hint-title">开工大吉</p>
        <p class="hint-sub">按住鼠标,用硬币刮开涂层 · 见今日份好运</p>
      </div>
      <button class="skip-btn" @click.stop="finish">跳过</button>

      <!-- 进度 -->
      <div class="progress-wrap">
        <div class="progress-bar"><div class="progress-fill" :style="{ width: progress + '%' }"></div></div>
        <span class="progress-text">{{ progress }}%</span>
      </div>
    </div>
  </teleport>
</template>

<script setup>
// 刮刮乐整页入场遮罩:canvas 擦除 + 硬币光标 + 金属粒子飞溅 + 面积采样
import { ref, watch, onMounted, onBeforeUnmount, nextTick } from 'vue'

const props = defineProps({
  modelValue: { type: Boolean, default: false }
})
const emit = defineEmits(['done'])

const visible = ref(props.modelValue)

// 父组件常在自身 onMounted 里才置 true,创建时 props 可能仍是 false → 需跟踪变化
watch(() => props.modelValue, async (v) => {
  if (v) {
    visible.value = true
    finished = false
    leaving.value = false
    await nextTick()
    setup()
  }
})
const leaving = ref(false)
const isDown = ref(false)
const progress = ref(0)

const coatCanvas = ref(null)
const sparkCanvas = ref(null)
const coinRef = ref(null)

const ERASE_R = 44           // 擦除半径
const SAMPLE_STEP = 24       // 面积采样步长(像素,降频)
const FINISH_RATIO = 0.55    // 刮开比例阈值

let coatCtx = null
let sparkCtx = null
let sparks = []              // 飞溅粒子
let rafId = null
let lastPoint = null
let tiltX = 0, tiltY = 0     // 硬币速度倾斜
let finished = false
let width = 0, height = 0, dpr = 1

function drawCoat(ctx, w, h) {
  // 金属拉丝渐变涂层
  const g = ctx.createLinearGradient(0, 0, w, h)
  g.addColorStop(0, '#3a3f4a')
  g.addColorStop(0.5, '#232733')
  g.addColorStop(1, '#161a24')
  ctx.fillStyle = g
  ctx.fillRect(0, 0, w, h)

  // 拉丝纹理:细密横线
  ctx.save()
  ctx.globalAlpha = 0.05
  ctx.strokeStyle = '#ffffff'
  for (let y = 0; y < h; y += 3) {
    ctx.beginPath()
    ctx.moveTo(0, y + Math.random() * 2)
    ctx.lineTo(w, y + Math.random() * 2)
    ctx.stroke()
  }
  ctx.restore()

  // 纹字(刮开前提示)
  ctx.save()
  ctx.globalAlpha = 0.16
  ctx.fillStyle = '#ffffff'
  ctx.font = `600 ${Math.max(28, w / 26)}px -apple-system, "PingFang SC", sans-serif`
  ctx.textAlign = 'center'
  ctx.textBaseline = 'middle'
  ctx.fillText('我要工作 · IWANTJOB', w / 2, h / 2 - 24)
  ctx.font = `400 ${Math.max(14, w / 52)}px -apple-system, "PingFang SC", sans-serif`
  ctx.fillText('SCRADE TO REVEAL', w / 2, h / 2 + 28)
  ctx.restore()
}

function setup() {
  const coat = coatCanvas.value
  const spark = sparkCanvas.value
  if (!coat || !spark) return
  dpr = Math.min(window.devicePixelRatio || 1, 2)
  width = window.innerWidth
  height = window.innerHeight

  for (const c of [coat, spark]) {
    c.width = width * dpr
    c.height = height * dpr
    c.style.width = width + 'px'
    c.style.height = height + 'px'
    c.getContext('2d').setTransform(dpr, 0, 0, dpr, 0, 0)
  }
  coatCtx = coat.getContext('2d')
  sparkCtx = spark.getContext('2d')
  drawCoat(coatCtx, width, height)
  progress.value = 0
  finished = false
  rafId = requestAnimationFrame(tick)
}

function eraseAt(x, y) {
  coatCtx.save()
  coatCtx.globalCompositeOperation = 'destination-out'
  // 硬币刮痕:主圆 + 边缘不规则
  coatCtx.beginPath()
  coatCtx.arc(x, y, ERASE_R, 0, Math.PI * 2)
  coatCtx.fill()
  for (let i = 0; i < 4; i++) {
    const a = Math.random() * Math.PI * 2
    const d = ERASE_R * (0.5 + Math.random() * 0.5)
    coatCtx.beginPath()
    coatCtx.arc(x + Math.cos(a) * d, y + Math.sin(a) * d, ERASE_R * 0.32, 0, Math.PI * 2)
    coatCtx.fill()
  }
  coatCtx.restore()
}

function spawnSparks(x, y, count) {
  for (let i = 0; i < count; i++) {
    const a = Math.random() * Math.PI * 2
    const sp = 1.5 + Math.random() * 3.5
    sparks.push({
      x, y,
      vx: Math.cos(a) * sp,
      vy: Math.sin(a) * sp - 1.2,
      life: 1,
      decay: 0.02 + Math.random() * 0.03,
      size: 1 + Math.random() * 2.4,
      color: Math.random() > 0.4 ? '#f7c948' : '#ffe9a8'
    })
  }
}

function tick() {
  // 粒子更新
  sparkCtx.clearRect(0, 0, width, height)
  sparks = sparks.filter(p => p.life > 0)
  for (const p of sparks) {
    p.x += p.vx; p.y += p.vy
    p.vy += 0.12          // 重力
    p.vx *= 0.985
    p.life -= p.decay
    sparkCtx.globalAlpha = Math.max(0, p.life)
    sparkCtx.fillStyle = p.color
    sparkCtx.beginPath()
    sparkCtx.arc(p.x, p.y, p.size, 0, Math.PI * 2)
    sparkCtx.fill()
  }
  sparkCtx.globalAlpha = 1
  rafId = requestAnimationFrame(tick)
}

let sampleCounter = 0
function maybeSample() {
  // 每 10 次擦除做一次面积采样(降频)
  if (++sampleCounter % 10 !== 0) return
  if (finished) return
  const pw = Math.floor(width * dpr), ph = Math.floor(height * dpr)
  const img = coatCtx.getImageData(0, 0, pw, ph).data
  let clear = 0, total = 0
  // 物理像素坐标采样,步长也乘 dpr
  for (let py = 0; py < ph; py += Math.floor(SAMPLE_STEP * dpr)) {
    for (let px = 0; px < pw; px += Math.floor(SAMPLE_STEP * dpr)) {
      const idx = (py * pw + px) * 4 + 3
      total++
      if (img[idx] < 40) clear++
    }
  }
  const ratio = clear / total
  progress.value = Math.min(99, Math.round(ratio / FINISH_RATIO * 100))
  if (ratio >= FINISH_RATIO && !finished) finish()
}

function scratchTo(x, y) {
  if (finished) return
  if (lastPoint) {
    const dx = x - lastPoint.x, dy = y - lastPoint.y
    const dist = Math.hypot(dx, dy)
    const steps = Math.max(1, Math.ceil(dist / (ERASE_R * 0.4)))
    for (let i = 1; i <= steps; i++) {
      eraseAt(lastPoint.x + dx * i / steps, lastPoint.y + dy * i / steps)
    }
    if (dist > 2) spawnSparks(x, y, 3)
    // 硬币随速度轻微倾斜
    tiltX = Math.max(-14, Math.min(14, dx * 0.4))
    tiltY = Math.max(-10, Math.min(10, dy * 0.3))
    if (coinRef.value) {
      coinRef.value.style.transform = `translate(${x - 22}px, ${y - 22}px) rotateX(${-tiltY}deg) rotateY(${tiltX}deg)`
    }
  }
  lastPoint = { x, y }
  maybeSample()
}

function onDown() { isDown.value = true; lastPoint = null }
function onUp() { isDown.value = false; lastPoint = null }

function onMove(e) {
  moveCoin(e.clientX, e.clientY)
  if (isDown.value) scratchTo(e.clientX, e.clientY)
}

function onTouchStart(e) {
  const t = e.touches[0]
  if (coinRef.value) coinRef.value.style.opacity = '0'
  isDown.value = true
  lastPoint = { x: t.clientX, y: t.clientY }
  scratchTo(t.clientX, t.clientY)
}

function onTouchMove(e) {
  const t = e.touches[0]
  if (isDown.value) scratchTo(t.clientX, t.clientY)
}

function moveCoin(x, y) {
  if (coinRef.value) {
    coinRef.value.style.transform = `translate(${x - 22}px, ${y - 22}px)`
  }
}

function finish() {
  if (finished) return
  finished = true
  // 庆祝粒子
  spawnSparks(width / 2, height / 3, 60)
  setTimeout(() => {
    leaving.value = true
    setTimeout(() => {
      visible.value = false
      emit('done')
    }, 700)
  }, 350)
}

onMounted(async () => {
  if (visible.value) await nextTick().then(setup)
})

onBeforeUnmount(() => {
  if (rafId) cancelAnimationFrame(rafId)
})
</script>

<style scoped>
.scratch-root {
  position: fixed;
  inset: 0;
  z-index: 9999;
  cursor: none;
  transition: opacity 0.65s cubic-bezier(0.16, 1, 0.3, 1);
  touch-action: none;
  user-select: none;
}
.scratch-root.leaving { opacity: 0; pointer-events: none; }

.coat-canvas, .spark-canvas {
  position: absolute;
  inset: 0;
}
.spark-canvas { pointer-events: none; }

/* 硬币 */
.coin {
  position: absolute;
  top: 0;
  left: 0;
  width: 44px;
  height: 44px;
  pointer-events: none;
  filter: drop-shadow(0 6px 14px rgba(0, 0, 0, 0.45));
  transition: scale 0.25s cubic-bezier(0.16, 1, 0.3, 1);
  will-change: transform;
}
.coin.scratching { scale: 1.18; }
.coin svg { display: block; }

/* 文案 */
.scratch-hint {
  position: absolute;
  top: 12vh;
  left: 50%;
  transform: translateX(-50%);
  text-align: center;
  pointer-events: none;
}
.hint-title {
  font-size: 1.6rem;
  font-weight: 700;
  letter-spacing: 0.3em;
  color: rgba(255, 255, 255, 0.85);
  margin: 0 0 8px;
}
.hint-sub {
  font-size: 0.85rem;
  color: rgba(255, 255, 255, 0.5);
  letter-spacing: 0.1em;
  margin: 0;
}

/* 跳过 */
.skip-btn {
  position: absolute;
  top: 24px;
  right: 28px;
  padding: 8px 18px;
  border-radius: 9999px;
  border: 1px solid rgba(255, 255, 255, 0.28);
  background: rgba(255, 255, 255, 0.08);
  color: rgba(255, 255, 255, 0.82);
  font-size: 0.85rem;
  cursor: pointer;
  transition: all 0.3s;
}
.skip-btn:hover { background: rgba(255, 255, 255, 0.16); }

/* 进度 */
.progress-wrap {
  position: absolute;
  bottom: 8vh;
  left: 50%;
  transform: translateX(-50%);
  display: flex;
  align-items: center;
  gap: 12px;
  pointer-events: none;
}
.progress-bar {
  width: 180px;
  height: 3px;
  border-radius: 9999px;
  background: rgba(255, 255, 255, 0.15);
  overflow: hidden;
}
.progress-fill {
  height: 100%;
  border-radius: 9999px;
  background: linear-gradient(90deg, #2997ff, #a259ff, #ff5ca8);
  transition: width 0.3s;
}
.progress-text {
  font-size: 0.78rem;
  color: rgba(255, 255, 255, 0.55);
  font-variant-numeric: tabular-nums;
}
</style>
