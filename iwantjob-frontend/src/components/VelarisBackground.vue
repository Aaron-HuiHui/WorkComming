<template>
  <div ref="wrapRef" class="velaris" :style="{ height }">
    <canvas ref="canvasRef" class="velaris-canvas"></canvas>
    <div class="velaris-fallback" v-if="fallback"></div>
    <div class="velaris-content">
      <slot />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue'

defineProps({
  height: { type: String, default: '500px' }
})

const wrapRef = ref(null)
const canvasRef = ref(null)
const fallback = ref(false)

/* ---------------- WebGL Shader（分层 simplex 噪声 + 多色混合 + 晕影 + 胶片颗粒） ---------------- */

const VERT = `
attribute vec2 aPos;
varying vec2 vUv;
void main() {
  vUv = aPos * 0.5 + 0.5;
  gl_Position = vec4(aPos, 0.0, 1.0);
}
`

const FRAG = `
precision highp float;
varying vec2 vUv;
uniform float uTime;
uniform vec2  uResolution;
uniform vec2  uMouse;
uniform float uIntensity;

/* Ashima 2D simplex noise */
vec3 permute(vec3 x) { return mod(((x * 34.0) + 1.0) * x, 289.0); }
float snoise(vec2 v) {
  const vec4 C = vec4(0.211324865405187, 0.366025403784439, -0.577350269189626, 0.024390243902439);
  vec2 i  = floor(v + dot(v, C.yy));
  vec2 x0 = v - i + dot(i, C.xx);
  vec2 i1 = (x0.x > x0.y) ? vec2(1.0, 0.0) : vec2(0.0, 1.0);
  vec4 x12 = x0.xyxy + C.xxzz;
  x12.xy -= i1;
  i = mod(i, 289.0);
  vec3 p = permute(permute(i.y + vec3(0.0, i1.y, 1.0)) + i.x + vec3(0.0, i1.x, 1.0));
  vec3 m = max(0.5 - vec3(dot(x0, x0), dot(x12.xy, x12.xy), dot(x12.zw, x12.zw)), 0.0);
  m = m * m; m = m * m;
  vec3 x = 2.0 * fract(p * C.www) - 1.0;
  vec3 h = abs(x) - 0.5;
  vec3 ox = floor(x + 0.5);
  vec3 a0 = x - ox;
  m *= 1.79284291400159 - 0.85373472095314 * (a0 * a0 + h * h);
  vec3 g;
  g.x  = a0.x * x0.x + h.x * x0.y;
  g.yz = a0.yz * x12.xz + h.yz * x12.yw;
  return 130.0 * dot(m, g);
}

void main() {
  vec2 aspect = vec2(uResolution.x / max(uResolution.y, 1.0), 1.0);
  vec2 p = vUv * aspect;
  float t = uTime * 0.16;

  /* 鼠标交互：光标附近的噪声场隆起扭曲 */
  vec2 mp = uMouse * aspect;
  vec2 toM = p - mp;
  float md = length(toM);
  float ripple = exp(-md * 3.0) * 0.24 * uIntensity;
  p += normalize(toM + 1e-5) * ripple;

  /* 三层噪声（不同尺度/速度/扰动耦合） */
  float n1 = snoise(p * 1.35 + vec2(t, -t * 0.8));
  float n2 = snoise(p * 2.75 - vec2(t * 0.6, t * 1.1) + n1 * 0.38);
  float n3 = snoise(p * 5.5 + vec2(-t * 0.4, t * 0.65) + n2 * 0.26);

  /* 多色混合：深海蓝黑 → 靛蓝 → 天青 → 紫（与全局 Aurora 冷调一致） */
  vec3 c1 = vec3(0.03, 0.05, 0.14);
  vec3 c2 = vec3(0.23, 0.30, 0.85);
  vec3 c3 = vec3(0.15, 0.62, 0.86);
  vec3 c4 = vec3(0.58, 0.30, 0.90);

  vec3 col = mix(c1, c2, smoothstep(-0.7, 0.75, n1));
  col = mix(col, c3, smoothstep(-0.35, 0.85, n2) * 0.62);
  col = mix(col, c4, smoothstep(0.12, 0.95, n3) * 0.42);

  /* 噪声峰值处的辉光 */
  float glow = smoothstep(0.5, 0.95, n2 * 0.5 + n3 * 0.5);
  col += vec3(0.26, 0.19, 0.46) * glow * 0.34;

  /* 晕影（四角压暗，中心提亮） */
  float vig = smoothstep(1.28, 0.22, length((vUv - 0.5) * vec2(aspect.x * 0.82, 1.0)));
  col *= mix(0.42, 1.06, vig);

  /* 胶片颗粒 */
  float grain = fract(sin(dot(vUv * uResolution + uTime * 60.0, vec2(12.9898, 78.233))) * 43758.5453);
  col += (grain - 0.5) * 0.045;

  gl_FragColor = vec4(col, 1.0);
}
`

let gl = null
let rafId = 0
let resizeObs = null
let mouseListener = null
let touchListener = null
let startTime = 0

const mouse = { x: 0.5, y: 0.5 }
const target = { x: 0.5, y: 0.5 }

function compile(type, source) {
  const sh = gl.createShader(type)
  gl.shaderSource(sh, source)
  gl.compileShader(sh)
  if (!gl.getShaderParameter(sh, gl.COMPILE_STATUS)) {
    console.error('Velaris shader:', gl.getShaderInfoLog(sh))
    return null
  }
  return sh
}

function frame(now) {
  const el = Math.max(0, now - startTime) / 1000
  /* 鼠标平滑跟随（缓动） */
  mouse.x += (target.x - mouse.x) * 0.055
  mouse.y += (target.y - mouse.y) * 0.055

  const w = gl.drawingBufferWidth
  const h = gl.drawingBufferHeight
  gl.viewport(0, 0, w, h)
  gl.uniform1f(gl.getUniformLocation(gl.program, 'uTime'), el)
  gl.uniform2f(gl.getUniformLocation(gl.program, 'uResolution'), w, h)
  gl.uniform2f(gl.getUniformLocation(gl.program, 'uMouse'), mouse.x, mouse.y)
  gl.uniform1f(gl.getUniformLocation(gl.program, 'uIntensity'), 1.0)
  gl.drawArrays(gl.TRIANGLE_STRIP, 0, 4)

  rafId = requestAnimationFrame(frame)
}

function setTargetFromEvent(e) {
  const rect = wrapRef.value.getBoundingClientRect()
  const cx = (e.clientX - rect.left) / rect.width
  const cy = 1 - (e.clientY - rect.top) / rect.height
  target.x = Math.min(1.2, Math.max(-0.2, cx))
  target.y = Math.min(1.2, Math.max(-0.2, cy))
}

onMounted(() => {
  const canvas = canvasRef.value
  gl = canvas.getContext('webgl', { antialias: false, alpha: false })
  if (!gl) {
    fallback.value = true /* 无 WebGL 时退化为静态渐变 */
    return
  }

  const vs = compile(gl.VERTEX_SHADER, VERT)
  const fs = compile(gl.FRAGMENT_SHADER, FRAG)
  if (!vs || !fs) {
    fallback.value = true
    return
  }
  const prog = gl.createProgram()
  gl.attachShader(prog, vs)
  gl.attachShader(prog, fs)
  gl.linkProgram(prog)
  gl.useProgram(prog)
  gl.program = prog

  /* 全屏四边形 */
  const buf = gl.createBuffer()
  gl.bindBuffer(gl.ARRAY_BUFFER, buf)
  gl.bufferData(gl.ARRAY_BUFFER, new Float32Array([-1, -1, 1, -1, -1, 1, 1, 1]), gl.STATIC_DRAW)
  const loc = gl.getAttribLocation(prog, 'aPos')
  gl.enableVertexAttribArray(loc)
  gl.vertexAttribPointer(loc, 2, gl.FLOAT, false, 0, 0)

  const fit = () => {
    const dpr = Math.min(window.devicePixelRatio || 1, 2)
    const w = wrapRef.value.clientWidth
    const h = wrapRef.value.clientHeight
    canvas.width = Math.max(1, Math.round(w * dpr))
    canvas.height = Math.max(1, Math.round(h * dpr))
    canvas.style.width = w + 'px'
    canvas.style.height = h + 'px'
  }
  fit()
  resizeObs = new ResizeObserver(fit)
  resizeObs.observe(wrapRef.value)

  mouseListener = e => setTargetFromEvent(e)
  touchListener = e => {
    if (e.touches && e.touches[0]) setTargetFromEvent(e.touches[0])
  }
  wrapRef.value.addEventListener('mousemove', mouseListener)
  wrapRef.value.addEventListener('touchmove', touchListener, { passive: true })

  startTime = performance.now()

  /* 减少动态效果偏好：只渲染一帧 */
  if (window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
    frame(performance.now())
    cancelAnimationFrame(rafId)
  } else {
    rafId = requestAnimationFrame(frame)
  }
})

onBeforeUnmount(() => {
  cancelAnimationFrame(rafId)
  if (resizeObs) resizeObs.disconnect()
  if (wrapRef.value) {
    if (mouseListener) wrapRef.value.removeEventListener('mousemove', mouseListener)
    if (touchListener) wrapRef.value.removeEventListener('touchmove', touchListener)
  }
  if (gl) {
    const lose = gl.getExtension('WEBGL_lose_context')
    if (lose) lose.loseContext()
    gl = null
  }
})
</script>

<style scoped>
.velaris {
  position: relative;
  width: 100%;
  overflow: hidden;
  border-radius: 16px;
  isolation: isolate;
}
.velaris-canvas {
  position: absolute;
  inset: 0;
  width: 100%;
  height: 100%;
  display: block;
}
.velaris-fallback {
  position: absolute;
  inset: 0;
  background:
    radial-gradient(60% 80% at 20% 20%, rgba(124, 58, 237, 0.55), transparent 60%),
    radial-gradient(50% 70% at 80% 30%, rgba(37, 99, 235, 0.5), transparent 60%),
    radial-gradient(60% 60% at 60% 85%, rgba(217, 70, 239, 0.4), transparent 65%),
    #0b0a1a;
}
.velaris-content {
  position: relative;
  z-index: 1;
  height: 100%;
  width: 100%;
}
</style>
