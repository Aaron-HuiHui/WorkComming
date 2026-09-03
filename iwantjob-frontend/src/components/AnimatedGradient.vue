<template>
  <div ref="containerRef" class="ag-container" aria-hidden="true">
    <canvas ref="canvasRef" class="ag-canvas"></canvas>
    <div v-if="noiseOpacity > 0" class="ag-noise" :style="noiseStyle"></div>
  </div>
</template>

<script setup>
/**
 * Animated Gradient 背景 —— tom_ui/animated-gradient 官方源码(MIT)的 Vue3 移植。
 * 官方 React 源:https://21st.dev/@tom_ui/components/animated-gradient
 * 实现方式与官方完全一致:WebGL2 片元着色器(噪声+涡旋+形状混合),
 * 6 个官方预设(Prism/Lava/Plasma/Pulse/Vortex/Mist)参数逐项保留。
 * 本站默认使用 Prism 预设(纯黑底 + 蓝白流动渐变),即官方 demo 同款。
 */
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import { useTheme } from '../composables/useTheme'

const { theme } = useTheme()

const props = defineProps({
  /** 预设名或 'custom';custom 需传完整参数 */
  preset: { type: String, default: 'Prism' },
  /** 自定义参数(preset==='custom' 时生效),字段同官方 CustomConfig */
  custom: { type: Object, default: null },
  /** 速度倍率(0-100),覆盖预设默认 */
  speed: { type: Number, default: null },
  /** 噪点层透明度(0-1),0 关闭;官方 demo 未开 */
  noiseOpacity: { type: Number, default: 0 },
  noiseScale: { type: Number, default: 1 }
})

const PatternShapes = { Checks: 0, Stripes: 1, Edge: 2 }

const presets = {
  Prism: {
    color1: '#050505', color2: '#66B3FF', color3: '#FFFFFF',
    rotation: -50, proportion: 1, scale: 0.01, speed: 30,
    distortion: 0, swirl: 50, swirlIterations: 16,
    softness: 47, offset: -299, shape: 'Checks', shapeSize: 45
  },
  Lava: {
    color1: '#FF9F21', color2: '#FF0303', color3: '#000000',
    rotation: 114, proportion: 100, scale: 0.52, speed: 30,
    distortion: 7, swirl: 18, swirlIterations: 20,
    softness: 100, offset: 717, shape: 'Edge', shapeSize: 12
  },
  Plasma: {
    color1: '#B566FF', color2: '#000000', color3: '#000000',
    rotation: 0, proportion: 63, scale: 0.75, speed: 30,
    distortion: 5, swirl: 61, swirlIterations: 5,
    softness: 100, offset: -168, shape: 'Checks', shapeSize: 28
  },
  Pulse: {
    color1: '#66FF85', color2: '#000000', color3: '#000000',
    rotation: -167, proportion: 92, scale: 0, speed: 20,
    distortion: 54, swirl: 75, swirlIterations: 3,
    softness: 28, offset: -813, shape: 'Checks', shapeSize: 79
  },
  Vortex: {
    color1: '#000000', color2: '#FFFFFF', color3: '#000000',
    rotation: 50, proportion: 41, scale: 0.4, speed: 20,
    distortion: 0, swirl: 100, swirlIterations: 3,
    softness: 5, offset: -744, shape: 'Stripes', shapeSize: 80
  },
  Mist: {
    color1: '#050505', color2: '#FF66B8', color3: '#050505',
    rotation: 0, proportion: 33, scale: 0.48, speed: 39,
    distortion: 4, swirl: 65, swirlIterations: 5,
    softness: 100, offset: -235, shape: 'Edge', shapeSize: 48
  }
}

const params = computed(() => {
  // 浅色主题:官方 Prism 结构不变,色板反转(白底+中蓝渐变),保证前景可读
  if (theme.value === 'light' && props.preset !== 'custom') {
    const preset = presets[props.preset] || presets.Prism
    return {
      ...preset,
      speed: props.speed ?? preset.speed,
      color1: '#F7F8FA',
      color2: '#9CC6FF',
      color3: '#FFFFFF'
    }
  }
  if (props.preset === 'custom' && props.custom) {
    return {
      color1: props.custom.color1,
      color2: props.custom.color2,
      color3: props.custom.color3,
      rotation: props.custom.rotation ?? 0,
      proportion: props.custom.proportion ?? 35,
      scale: props.custom.scale ?? 1,
      speed: props.custom.speed ?? 25,
      distortion: props.custom.distortion ?? 12,
      swirl: props.custom.swirl ?? 80,
      swirlIterations: props.custom.swirlIterations ?? 10,
      softness: props.custom.softness ?? 100,
      offset: props.custom.offset ?? 0,
      shape: props.custom.shape ?? 'Checks',
      shapeSize: props.custom.shapeSize ?? 10
    }
  }
  const preset = presets[props.preset] || presets.Prism
  return { ...preset, speed: props.speed ?? preset.speed }
})

const noiseStyle = computed(() => ({
  backgroundImage: `url("data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAADAAAAAwBAMAAAClLOS0AAAAElBMVEUAAAAAAAAAAAAAAAAAAAAAAADgKxmiAAAABnRSTlMCCgkGBAVJOAVJAAAASklEQVQ4y2NgGAWjYBSMglEwCgY/YGRgZBQUYmJiZGQEkYwMjIyMgoKCjIyMIJKBgRFIMjIyAklGRkYGRkFBYEcwMDIyMjAOUQAA1I4HwVwZAkYAAAAASUVORK5CYII=")`,
  backgroundSize: `${props.noiseScale * 200}px`,
  backgroundRepeat: 'repeat',
  opacity: props.noiseOpacity / 2,
  pointerEvents: 'none',
  position: 'absolute',
  inset: '0'
}))

/* ---------- 官方 FRAGMENT_SHADER,逐行保留 ---------- */
const FRAGMENT_SHADER = `#version 300 es
precision highp float;

uniform float u_time;
uniform float u_pixelRatio;
uniform vec2 u_resolution;

uniform float u_scale;
uniform float u_rotation;
uniform vec4 u_color1;
uniform vec4 u_color2;
uniform vec4 u_color3;
uniform float u_proportion;
uniform float u_softness;
uniform float u_shape;
uniform float u_shapeScale;
uniform float u_distortion;
uniform float u_swirl;
uniform float u_swirlIterations;

out vec4 fragColor;

#define TWO_PI 6.28318530718
#define PI 3.14159265358979323846

vec2 rotate(vec2 uv, float th) {
  return mat2(cos(th), sin(th), -sin(th), cos(th)) * uv;
}

float random(vec2 st) {
  return fract(sin(dot(st.xy, vec2(12.9898, 78.233))) * 43758.5453123);
}

float noise(vec2 st) {
  vec2 i = floor(st);
  vec2 f = fract(st);
  float a = random(i);
  float b = random(i + vec2(1.0, 0.0));
  float c = random(i + vec2(0.0, 1.0));
  float d = random(i + vec2(1.0, 1.0));

  vec2 u = f * f * (3.0 - 2.0 * f);

  float x1 = mix(a, b, u.x);
  float x2 = mix(c, d, u.x);
  return mix(x1, x2, u.y);
}

vec4 blend_colors(vec4 c1, vec4 c2, vec4 c3, float mixer, float edgesWidth, float edge_blur) {
    vec3 color1 = c1.rgb * c1.a;
    vec3 color2 = c2.rgb * c2.a;
    vec3 color3 = c3.rgb * c3.a;

    float r1 = smoothstep(.0 + .35 * edgesWidth, .7 - .35 * edgesWidth + .5 * edge_blur, mixer);
    float r2 = smoothstep(.3 + .35 * edgesWidth, 1. - .35 * edgesWidth + edge_blur, mixer);

    vec3 blended_color_2 = mix(color1, color2, r1);
    float blended_opacity_2 = mix(c1.a, c2.a, r1);

    vec3 c = mix(blended_color_2, color3, r2);
    float o = mix(blended_opacity_2, c3.a, r2);
    return vec4(c, o);
}

void main() {
    vec2 uv = gl_FragCoord.xy / u_resolution.xy;

    float t = .5 * u_time;

    float noise_scale = .0005 + .006 * u_scale;

    uv -= .5;
    uv *= (noise_scale * u_resolution);
    uv = rotate(uv, u_rotation * .5 * PI);
    uv /= u_pixelRatio;
    uv += .5;

    float n1 = noise(uv * 1. + t);
    float n2 = noise(uv * 2. - t);
    float angle = n1 * TWO_PI;
    uv.x += 4. * u_distortion * n2 * cos(angle);
    uv.y += 4. * u_distortion * n2 * sin(angle);

    float iterations_number = ceil(clamp(u_swirlIterations, 1., 30.));
    for (float i = 1.; i <= iterations_number; i++) {
        uv.x += clamp(u_swirl, 0., 2.) / i * cos(t + i * 1.5 * uv.y);
        uv.y += clamp(u_swirl, 0., 2.) / i * cos(t + i * 1. * uv.x);
    }

    float proportion = clamp(u_proportion, 0., 1.);

    float shape = 0.;
    float mixer = 0.;
    if (u_shape < .5) {
      vec2 checks_shape_uv = uv * (.5 + 3.5 * u_shapeScale);
      shape = .5 + .5 * sin(checks_shape_uv.x) * cos(checks_shape_uv.y);
      mixer = shape + .48 * sign(proportion - .5) * pow(abs(proportion - .5), .5);
    } else if (u_shape < 1.5) {
      vec2 stripes_shape_uv = uv * (.25 + 3. * u_shapeScale);
      float f = fract(stripes_shape_uv.y);
      shape = smoothstep(.0, .55, f) * smoothstep(1., .45, f);
      mixer = shape + .48 * sign(proportion - .5) * pow(abs(proportion - .5), .5);
    } else {
      float sh = 1. - uv.y;
      sh -= .5;
      sh /= (noise_scale * u_resolution.y);
      sh += .5;
      float shape_scaling = .2 * (1. - u_shapeScale);
      shape = smoothstep(.45 - shape_scaling, .55 + shape_scaling, sh + .3 * (proportion - .5));
      mixer = shape;
    }

    vec4 color_mix = blend_colors(u_color1, u_color2, u_color3, mixer, 1. - clamp(u_softness, 0., 1.), .01 + .01 * u_scale);

    fragColor = vec4(color_mix.rgb, color_mix.a);
}
`

/* ---------- 官方颜色解析工具,逐行保留 ---------- */
function hexToRgba(hex) {
  let r = 0, g = 0, b = 0, a = 1
  if (hex.startsWith('rgba(')) {
    const parts = hex.slice(5, -1).split(',')
    r = parseInt(parts[0]) / 255
    g = parseInt(parts[1]) / 255
    b = parseInt(parts[2]) / 255
    a = parseFloat(parts[3])
  } else if (hex.startsWith('rgb(')) {
    const parts = hex.slice(4, -1).split(',')
    r = parseInt(parts[0]) / 255
    g = parseInt(parts[1]) / 255
    b = parseInt(parts[2]) / 255
  } else if (hex.startsWith('hsla(') || hex.startsWith('hsl(')) {
    const isHsla = hex.startsWith('hsla(')
    const parts = hex.slice(isHsla ? 5 : 4, -1).split(',')
    const h = parseFloat(parts[0]) / 360
    const s = parseFloat(parts[1]) / 100
    const l = parseFloat(parts[2]) / 100
    a = isHsla ? parseFloat(parts[3]) : 1
    ;[r, g, b] = hslToRgb(h, s, l)
  } else if (hex.startsWith('#')) {
    const c = hex.slice(1)
    if (c.length === 3) {
      r = parseInt(c[0] + c[0], 16) / 255
      g = parseInt(c[1] + c[1], 16) / 255
      b = parseInt(c[2] + c[2], 16) / 255
    } else if (c.length >= 6) {
      r = parseInt(c.slice(0, 2), 16) / 255
      g = parseInt(c.slice(2, 4), 16) / 255
      b = parseInt(c.slice(4, 6), 16) / 255
      if (c.length === 8) a = parseInt(c.slice(6, 8), 16) / 255
    }
  }
  return [r, g, b, a]
}

function hslToRgb(h, s, l) {
  let r, g, b
  if (s === 0) {
    r = g = b = l
  } else {
    const hue2rgb = (p, q, t) => {
      if (t < 0) t += 1
      if (t > 1) t -= 1
      if (t < 1 / 6) return p + (q - p) * 6 * t
      if (t < 1 / 2) return q
      if (t < 2 / 3) return p + (q - p) * (2 / 3 - t) * 6
      return p
    }
    const q = l < 0.5 ? l * (1 + s) : l + s - l * s
    const p = 2 * l - q
    r = hue2rgb(p, q, h + 1 / 3)
    g = hue2rgb(p, q, h)
    b = hue2rgb(p, q, h - 1 / 3)
  }
  return [r, g, b]
}

const containerRef = ref(null)
const canvasRef = ref(null)
let frameId = 0
let startTime = 0
let disposed = false
let gl = null

onMounted(() => {
  const canvas = canvasRef.value
  const container = containerRef.value
  if (!canvas || !container) return

  gl = canvas.getContext('webgl2', { premultipliedAlpha: true, alpha: true, antialias: true })
  if (!gl) {
    console.warn('WebGL2 不可用,背景降级为静态底色')
    return
  }

  const vertexShaderSource = `#version 300 es
    in vec4 a_position;
    void main() {
      gl_Position = a_position;
    }`

  const vertexShader = gl.createShader(gl.VERTEX_SHADER)
  gl.shaderSource(vertexShader, vertexShaderSource)
  gl.compileShader(vertexShader)

  const fragmentShader = gl.createShader(gl.FRAGMENT_SHADER)
  gl.shaderSource(fragmentShader, FRAGMENT_SHADER)
  gl.compileShader(fragmentShader)

  const program = gl.createProgram()
  gl.attachShader(program, vertexShader)
  gl.attachShader(program, fragmentShader)
  gl.linkProgram(program)
  gl.useProgram(program)

  const positionBuffer = gl.createBuffer()
  gl.bindBuffer(gl.ARRAY_BUFFER, positionBuffer)
  gl.bufferData(gl.ARRAY_BUFFER, new Float32Array([-1, -1, 1, -1, -1, 1, -1, 1, 1, -1, 1, 1]), gl.STATIC_DRAW)

  const positionLocation = gl.getAttribLocation(program, 'a_position')
  gl.enableVertexAttribArray(positionLocation)
  gl.vertexAttribPointer(positionLocation, 2, gl.FLOAT, false, 0, 0)

  const uniforms = {
    u_time: gl.getUniformLocation(program, 'u_time'),
    u_resolution: gl.getUniformLocation(program, 'u_resolution'),
    u_pixelRatio: gl.getUniformLocation(program, 'u_pixelRatio'),
    u_scale: gl.getUniformLocation(program, 'u_scale'),
    u_rotation: gl.getUniformLocation(program, 'u_rotation'),
    u_color1: gl.getUniformLocation(program, 'u_color1'),
    u_color2: gl.getUniformLocation(program, 'u_color2'),
    u_color3: gl.getUniformLocation(program, 'u_color3'),
    u_proportion: gl.getUniformLocation(program, 'u_proportion'),
    u_softness: gl.getUniformLocation(program, 'u_softness'),
    u_shape: gl.getUniformLocation(program, 'u_shape'),
    u_shapeScale: gl.getUniformLocation(program, 'u_shapeScale'),
    u_distortion: gl.getUniformLocation(program, 'u_distortion'),
    u_swirl: gl.getUniformLocation(program, 'u_swirl'),
    u_swirlIterations: gl.getUniformLocation(program, 'u_swirlIterations')
  }

  const resize = () => {
    const width = container.clientWidth
    const height = container.clientHeight
    const pixelRatio = window.devicePixelRatio || 1
    canvas.width = width * pixelRatio
    canvas.height = height * pixelRatio
    canvas.style.width = width + 'px'
    canvas.style.height = height + 'px'
    gl.viewport(0, 0, canvas.width, canvas.height)
  }

  resize()
  const resizeObserver = new ResizeObserver(resize)
  resizeObserver.observe(container)

  startTime = performance.now()

  const animate = (time) => {
    if (disposed) return
    const p = params.value
    const elapsed = (time - startTime) / 1000
    const speed = (p.speed / 100) * 5

    gl.uniform1f(uniforms.u_time, elapsed * speed + p.offset * 0.01)
    gl.uniform2f(uniforms.u_resolution, canvas.width, canvas.height)
    gl.uniform1f(uniforms.u_pixelRatio, window.devicePixelRatio || 1)
    gl.uniform1f(uniforms.u_scale, p.scale)
    gl.uniform1f(uniforms.u_rotation, (p.rotation * Math.PI) / 180)

    const c1 = hexToRgba(p.color1)
    const c2 = hexToRgba(p.color2)
    const c3 = hexToRgba(p.color3)
    gl.uniform4f(uniforms.u_color1, c1[0], c1[1], c1[2], c1[3])
    gl.uniform4f(uniforms.u_color2, c2[0], c2[1], c2[2], c2[3])
    gl.uniform4f(uniforms.u_color3, c3[0], c3[1], c3[2], c3[3])

    gl.uniform1f(uniforms.u_proportion, p.proportion / 100)
    gl.uniform1f(uniforms.u_softness, p.softness / 100)
    gl.uniform1f(uniforms.u_shape, PatternShapes[p.shape])
    gl.uniform1f(uniforms.u_shapeScale, p.shapeSize / 100)
    gl.uniform1f(uniforms.u_distortion, p.distortion / 50)
    gl.uniform1f(uniforms.u_swirl, p.swirl / 100)
    gl.uniform1f(uniforms.u_swirlIterations, p.swirl === 0 ? 0 : p.swirlIterations)

    gl.drawArrays(gl.TRIANGLES, 0, 6)
    frameId = requestAnimationFrame(animate)
  }

  frameId = requestAnimationFrame(animate)
})

onBeforeUnmount(() => {
  disposed = true
  if (frameId) cancelAnimationFrame(frameId)
})
</script>

<style scoped>
.ag-container {
  position: fixed;
  inset: 0;
  z-index: -1;
  overflow: hidden;
  border-radius: 0;
  /* 不铺底色:canvas 是全屏 quad,shader 输出即完整画面 */
  background: transparent;
}
.ag-canvas {
  display: block;
  width: 100%;
  height: 100%;
}
</style>
