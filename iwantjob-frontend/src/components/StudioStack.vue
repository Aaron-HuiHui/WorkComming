<template>
  <div class="studio-stack" aria-hidden="true">
    <div class="ss-inner" :style="innerStyle">
      <div
        v-for="i in 4"
        :key="i"
        class="ss-layer"
        :class="'ss-l' + i"
        :style="layerStyle(i - 1)"
      >
        <!-- 首层叠加一点「迷你界面」细节，增强卡片质感 -->
        <template v-if="i === 1">
          <span class="ss-dots">
            <span class="ss-dot"></span>
            <span class="ss-dot"></span>
            <span class="ss-dot"></span>
          </span>
          <span class="ss-line"></span>
          <span class="ss-line ss-line-short"></span>
        </template>
      </div>
    </div>
  </div>
</template>

<script setup>
/**
 * 3D 堆叠玻璃片（暗黑创意工作室风格）
 * 四层半透明白玻璃圆角矩形：宽度递减、交错旋转 ±8°、纵向错落堆叠。
 * 父级传入归一化鼠标坐标 tilt={x,y}（-1~1）：
 *  - 整体随鼠标做 rotateX/rotateY 微倾（0.4s 缓动）
 *  - 每层按不同深度做 translate3d 视差漂移，形成立体层叠感
 * 叠加缓慢上下浮动动画（走 margin 通道，避免与 transform 冲突）。
 */
import { computed } from 'vue'

const props = defineProps({
  tilt: { type: Object, default: () => ({ x: 0, y: 0 }) }
})

/* 每层静态旋转角（交错 ±8° 以内）与视差深度 */
const ROTATIONS = [-8, 6, -5, 8]
const DEPTHS = [8, 16, 26, 38]

const innerStyle = computed(() => ({
  transform:
    `rotateY(${(props.tilt.x * 12).toFixed(1)}deg) ` +
    `rotateX(${(-props.tilt.y * 9).toFixed(1)}deg)`
}))

function layerStyle(i) {
  const d = DEPTHS[i]
  return {
    transform:
      `translate3d(${(props.tilt.x * d).toFixed(1)}px, ${(props.tilt.y * d * 0.6).toFixed(1)}px, ${i * 14}px) ` +
      `rotate(${ROTATIONS[i]}deg)`
  }
}
</script>

<style scoped>
.studio-stack {
  position: relative;
  width: 250px;
  height: 214px;
  perspective: 900px;
  pointer-events: none;
}
.ss-inner {
  position: relative;
  width: 100%;
  height: 100%;
  transform-style: preserve-3d;
  transition: transform 0.4s cubic-bezier(0.22, 0.61, 0.36, 1);
  animation: ss-float 7s ease-in-out infinite;
  will-change: transform;
}

/* ---------- 四层半透明白玻璃圆角矩形 ---------- */
.ss-layer {
  position: absolute;
  left: 50%;
  border-radius: 18px;
  border: 1px solid rgba(255, 255, 255, 0.24);
  background: linear-gradient(160deg, rgba(255, 255, 255, 0.18), rgba(255, 255, 255, 0.05));
  backdrop-filter: blur(6px) saturate(1.3);
  -webkit-backdrop-filter: blur(6px) saturate(1.3);
  box-shadow: 0 18px 44px rgba(2, 6, 32, 0.5), inset 0 1px 0 rgba(255, 255, 255, 0.28);
  transition: transform 0.4s cubic-bezier(0.22, 0.61, 0.36, 1);
  will-change: transform;
}
/* 宽度递减、纵向错落 */
.ss-l1 { width: 240px; height: 148px; top: 0;    margin-left: -120px; z-index: 4; }
.ss-l2 { width: 198px; height: 124px; top: 36px; margin-left: -99px;  z-index: 3; opacity: 0.85; }
.ss-l3 { width: 160px; height: 102px; top: 66px; margin-left: -80px;  z-index: 2; opacity: 0.7; }
.ss-l4 { width: 126px; height: 82px;  top: 92px; margin-left: -63px;  z-index: 1; opacity: 0.55; }

/* 首层迷你界面细节 */
.ss-dots {
  position: absolute;
  top: 13px; left: 15px;
  display: flex;
  gap: 5px;
}
.ss-dot {
  width: 6px; height: 6px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.55);
}
.ss-dot:nth-child(2) { background: rgba(255, 255, 255, 0.34); }
.ss-dot:nth-child(3) { background: rgba(255, 255, 255, 0.2); }
.ss-line {
  position: absolute;
  left: 15px; right: 44px; top: 38px;
  height: 7px;
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.22);
}
.ss-line-short {
  top: 54px; right: 78px;
  background: rgba(255, 255, 255, 0.13);
}

/* ---------- 浮动动画（margin 通道，避免与 tilt transform 冲突） ---------- */
@keyframes ss-float {
  0%, 100% { margin-top: 0; }
  50% { margin-top: -16px; }
}

/* ---------- 响应式：窄屏隐藏部分堆叠层 ---------- */
@media (max-width: 1100px) {
  .ss-l3, .ss-l4 { display: none; }
}
@media (prefers-reduced-motion: reduce) {
  .ss-inner { animation: none; transition: none; }
  .ss-layer { transition: none; }
}
</style>
