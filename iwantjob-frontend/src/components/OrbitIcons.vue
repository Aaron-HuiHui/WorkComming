<template>
  <div class="orbit-stage" :style="{ width: size + 'px', height: size + 'px' }">
    <!-- 点阵底纹(radial-orbital-timeline 同款) -->
    <div class="dot-grid"></div>

    <!-- 中心:渐变球 + ping 波纹环 -->
    <div class="core-orb">
      <slot name="center">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round" class="core-icon">
          <rect x="3" y="7.5" width="18" height="12.5" rx="2.5" />
          <path d="M9 7.5V6a2 2 0 0 1 2-2h2a2 2 0 0 1 2 2v1.5" />
          <path d="M3 12.8h18" />
          <path d="M9.5 11.2v3M14.5 11.2v3" />
        </svg>
      </slot>
    </div>
    <div class="ring ping-ring r1"></div>
    <div class="ring ping-ring r2"></div>
    <div class="ring static-ring"></div>

    <!-- 轨道节点(伪 3D:x/y 由角度算,z 由深度映射 z-index/透明度/缩放) -->
    <div
      v-for="(n, i) in nodes"
      :key="i"
      class="orbit-node"
      :style="nodeStyle(n)"
      @click="$emit('select', n)"
    >
      <div class="node-chip">
        <component :is="n.icon" :size="18" :stroke-width="1.9" />
      </div>
      <div class="node-label">{{ n.label }}</div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'

const props = defineProps({
  /** [{ icon: Component, label: string }] */
  nodes: { type: Array, default: () => [] },
  /** 舞台边长(px) */
  size: { type: Number, default: 380 },
  /** 公转周期(秒) */
  period: { type: Number, default: 26 },
  /** 椭圆纵向压扁比(伪 3D) */
  tilt: { type: Number, default: 0.42 }
})

defineEmits(['select'])

const TAU = Math.PI * 2
let angle = -Math.PI / 2
let rafId = null
let disposed = false
let last = 0
// 每个节点的实时状态(触发响应式重绘)
const states = ref([])

function tick(now) {
  if (disposed) return
  const dt = last ? (now - last) / 1000 : 0
  last = now
  angle = (angle + (dt * TAU) / props.period) % TAU
  const R = props.size / 2 - 44
  const next = props.nodes.map((_, i) => {
    const a = angle + (i / props.nodes.length) * TAU
    const depth = Math.cos(a) // 1=最近  -1=最远
    return {
      x: R * Math.sin(a),
      y: R * Math.cos(a) * props.tilt,
      z: depth,
      scale: 0.72 + 0.3 * (depth + 1) / 2,
      opacity: 0.45 + 0.5 * (depth + 1) / 2
    }
  })
  states.value = next
  rafId = requestAnimationFrame(tick)
}

function nodeStyle(n) {
  const s = states.value[props.nodes.indexOf(n)] || { x: 0, y: 0, scale: 1, opacity: 0.6, z: 0 }
  return {
    transform: `translate(${Math.round(s.x)}px, ${Math.round(s.y)}px) scale(${s.scale.toFixed(3)})`,
    opacity: s.opacity.toFixed(2),
    zIndex: Math.round(100 + s.z * 50)
  }
}

onMounted(() => {
  rafId = requestAnimationFrame(tick)
})
onBeforeUnmount(() => {
  disposed = true
  if (rafId) cancelAnimationFrame(rafId)
})
</script>

<style scoped>
.orbit-stage {
  position: relative;
  flex-shrink: 0;
}

/* 点阵底纹 */
.dot-grid {
  position: absolute;
  inset: -30px;
  background-image: radial-gradient(var(--hairline-strong) 1px, transparent 1px);
  background-size: 18px 18px;
  mask-image: radial-gradient(circle, black 30%, transparent 72%);
  -webkit-mask-image: radial-gradient(circle, black 30%, transparent 72%);
  opacity: 0.5;
}

/* 中心渐变球(radial-orbital 同款紫→蓝→青) */
.core-orb {
  position: absolute;
  left: 50%;
  top: 50%;
  width: 64px;
  height: 64px;
  margin: -32px 0 0 -32px;
  border-radius: 50%;
  background: linear-gradient(135deg, #a855f7, #3b82f6 55%, #2dd4bf);
  display: grid;
  place-items: center;
  color: #fff;
  z-index: 150;
  animation: orb-pulse 2s cubic-bezier(0.4, 0, 0.6, 1) infinite;
  box-shadow: 0 10px 40px rgba(59, 130, 246, 0.45);
}
.core-icon { width: 26px; height: 26px; }
@keyframes orb-pulse {
  0%, 100% { transform: scale(1); }
  50% { transform: scale(1.07); }
}

/* 波纹环 + 轨道细环 */
.ring {
  position: absolute;
  left: 50%;
  top: 50%;
  transform: translate(-50%, -50%);
  border-radius: 50%;
  pointer-events: none;
}
.ping-ring.r1 {
  width: 84px; height: 84px;
  border: 0.8px solid rgba(255, 255, 255, 0.2);
  animation: ring-ping 2.4s cubic-bezier(0, 0, 0.2, 1) infinite;
}
.ping-ring.r2 {
  width: 120px; height: 120px;
  border: 0.8px solid rgba(255, 255, 255, 0.1);
  animation: ring-ping 2.4s cubic-bezier(0, 0, 0.2, 1) infinite;
  animation-delay: 0.6s;
}
html.light .ping-ring.r1 { border-color: rgba(0, 0, 0, 0.16); }
html.light .ping-ring.r2 { border-color: rgba(0, 0, 0, 0.08); }
@keyframes ring-ping {
  0% { transform: translate(-50%, -50%) scale(1); opacity: 0.7; }
  80%, 100% { transform: translate(-50%, -50%) scale(2.1); opacity: 0; }
}
.static-ring {
  width: calc(100% - 88px);
  height: calc(100% - 88px);
  border: 1px dashed var(--hairline-strong);
}

/* 轨道节点 */
.orbit-node {
  position: absolute;
  left: 50%;
  top: 50%;
  margin: -26px 0 0 -26px;
  width: 52px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  cursor: pointer;
  transition: opacity 0.2s;
  will-change: transform;
}
.node-chip {
  width: 44px;
  height: 44px;
  border-radius: 14px;
  display: grid;
  place-items: center;
  background: var(--card-strong);
  border: 1px solid var(--hairline-strong);
  color: var(--foreground);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.18);
  transition: border-color 0.3s, color 0.3s, box-shadow 0.3s;
}
.orbit-node:hover .node-chip {
  border-color: var(--primary);
  color: var(--primary);
  box-shadow: 0 6px 24px var(--primary-soft);
}
.node-label {
  font-size: 0.68rem;
  font-weight: 560;
  color: var(--foreground-muted);
  white-space: nowrap;
  letter-spacing: 0.02em;
}
</style>
