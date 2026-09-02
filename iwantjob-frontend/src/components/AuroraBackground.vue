<template>
  <div class="aurora-layer" aria-hidden="true">
    <div class="aurora-blob blob-a"></div>
    <div class="aurora-blob blob-b"></div>
    <div class="aurora-blob blob-c"></div>
    <div class="aurora-grid"></div>
  </div>
</template>

<script setup>
// 动态氛围背景:渐变光斑漂移 + 细网格,深浅两态由 CSS 变量驱动
</script>

<style scoped>
.aurora-layer {
  position: fixed;
  inset: 0;
  z-index: -1;
  overflow: hidden;
  background: var(--background);
}

.aurora-blob {
  position: absolute;
  border-radius: 50%;
  filter: blur(90px);
  opacity: 0.5;
  will-change: transform;
}

.blob-a {
  width: 46vw;
  height: 46vw;
  left: -10vw;
  top: -14vw;
  background: radial-gradient(circle, rgba(41, 151, 255, 0.5), transparent 65%);
  animation: drift-a 26s ease-in-out infinite alternate;
}

.blob-b {
  width: 40vw;
  height: 40vw;
  right: -8vw;
  top: 18vh;
  background: radial-gradient(circle, rgba(162, 89, 255, 0.42), transparent 65%);
  animation: drift-b 32s ease-in-out infinite alternate;
}

.blob-c {
  width: 36vw;
  height: 36vw;
  left: 28vw;
  bottom: -16vw;
  background: radial-gradient(circle, rgba(255, 92, 168, 0.32), transparent 65%);
  animation: drift-c 38s ease-in-out infinite alternate;
}

html.light .aurora-blob { opacity: 0.28; }
html.light .blob-a { background: radial-gradient(circle, rgba(0, 113, 227, 0.35), transparent 65%); }
html.light .blob-b { background: radial-gradient(circle, rgba(125, 79, 255, 0.28), transparent 65%); }
html.light .blob-c { background: radial-gradient(circle, rgba(255, 79, 154, 0.22), transparent 65%); }

/* 苹果风细网格,极低调 */
.aurora-grid {
  position: absolute;
  inset: 0;
  background-image:
    linear-gradient(var(--hairline) 1px, transparent 1px),
    linear-gradient(90deg, var(--hairline) 1px, transparent 1px);
  background-size: 72px 72px;
  opacity: 0.35;
  mask-image: radial-gradient(ellipse 80% 60% at 50% 40%, black 30%, transparent 75%);
  -webkit-mask-image: radial-gradient(ellipse 80% 60% at 50% 40%, black 30%, transparent 75%);
}

@keyframes drift-a {
  from { transform: translate(0, 0) scale(1); }
  to { transform: translate(8vw, 6vh) scale(1.15); }
}
@keyframes drift-b {
  from { transform: translate(0, 0) scale(1.08); }
  to { transform: translate(-7vw, -5vh) scale(0.92); }
}
@keyframes drift-c {
  from { transform: translate(0, 0) scale(0.95); }
  to { transform: translate(-6vw, -8vh) scale(1.12); }
}

@media (prefers-reduced-motion: reduce) {
  .aurora-blob { animation: none; }
}
</style>
