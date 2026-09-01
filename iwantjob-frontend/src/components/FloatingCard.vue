<template>
  <div class="floating-card" :style="cardStyle" aria-hidden="true">
    <span class="glass-icon lg" :class="'gi-' + theme">
      <GlassIcon :name="icon" :size="26" />
    </span>
  </div>
</template>

<script setup>
/**
 * 3D 悬浮玻璃卡（参考「floating cards in 3D space + camera reacts to mouse」）
 * 由父级传入归一化鼠标坐标 tilt={x,y}（-1~1），depth 决定视差强度，
 * 产生「相机随鼠标微动」的立体漂浮感。纯 CSS 3D 变换，无 WebGL 依赖。
 */
import { computed } from 'vue'
import GlassIcon from './GlassIcon.vue'

const props = defineProps({
  icon: { type: String, default: 'rocket' },
  theme: { type: String, default: 'violet' },
  depth: { type: Number, default: 18 },
  tilt: { type: Object, default: () => ({ x: 0, y: 0 }) }
})

const cardStyle = computed(() => ({
  transform:
    `translate3d(${(props.tilt.x * props.depth).toFixed(1)}px, ${(props.tilt.y * props.depth).toFixed(1)}px, 0) ` +
    `rotateY(${(props.tilt.x * 10).toFixed(1)}deg) rotateX(${(-props.tilt.y * 8).toFixed(1)}deg)`
}))
</script>

<style scoped>
.floating-card {
  position: absolute;
  z-index: 0;
  transition: transform 0.4s cubic-bezier(0.22, 0.61, 0.36, 1);
  transform-style: preserve-3d;
  will-change: transform;
  animation: fc-float 7s ease-in-out infinite;
}
.floating-card .glass-icon {
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.25), 0 14px 34px rgba(2, 6, 32, 0.5);
}
@keyframes fc-float {
  0%, 100% { margin-top: 0; }
  50% { margin-top: -14px; }
}
@media (prefers-reduced-motion: reduce) {
  .floating-card { animation: none; transition: none; }
}
</style>
