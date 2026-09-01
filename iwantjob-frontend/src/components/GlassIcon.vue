<template>
  <svg
    class="glass-icon-svg"
    :width="size"
    :height="size"
    viewBox="0 0 24 24"
    fill="none"
    stroke="currentColor"
    stroke-width="1.6"
    stroke-linecap="round"
    stroke-linejoin="round"
    aria-hidden="true"
  >
    <template v-for="(el, i) in shapes" :key="i">
      <path v-if="el.tag === 'path'" :d="el.d" />
      <circle v-else-if="el.tag === 'circle'" :cx="el.cx" :cy="el.cy" :r="el.r" />
      <rect
        v-else-if="el.tag === 'rect'"
        :x="el.x"
        :y="el.y"
        :width="el.width"
        :height="el.height"
        :rx="el.rx"
      />
    </template>
  </svg>
</template>

<script setup>
/**
 * GlassIcon —— 毛玻璃设计系统线性图标（手绘，第八阶段 G1）
 * 规范：viewBox 24 / stroke currentColor / stroke-width 1.6 / fill none / 圆角线帽
 * 用法：<GlassIcon name="rocket" :size="24" />，放入 .glass-icon + .gi-* 容器
 */
import { computed } from 'vue'

const props = defineProps({
  name: { type: String, required: true },
  size: { type: [Number, String], default: 24 }
})

const ICONS = {
  // 闪电（互助积分）
  zap: [
    { tag: 'path', d: 'M13 2.6 5 13.4h6.1l-.5 8 8.4-10.8h-6.1L13 2.6Z' },
    { tag: 'path', d: 'M18.9 4.2l-1.2 1.1' }
  ],
  // 奖章 + 飘带（我的徽章）
  medal: [
    { tag: 'path', d: 'M8.7 2.8 11 9.6' },
    { tag: 'path', d: 'M15.3 2.8 13 9.6' },
    { tag: 'path', d: 'M8.7 2.8 12 6.2l3.3-3.4' },
    { tag: 'circle', cx: 12, cy: 14.6, r: 4.8 },
    { tag: 'path', d: 'M10.2 14.6l1.3 1.3 2.3-2.6' }
  ],
  // 游戏手柄（模拟演练）
  gamepad: [
    {
      tag: 'path',
      d: 'M6.8 7.2h10.4a4.6 4.6 0 0 1 4.5 5.6l-.8 3.6a2.7 2.7 0 0 1-4.7 1.2l-1.7-2.1H9.5l-1.7 2.1a2.7 2.7 0 0 1-4.7-1.2l-.8-3.6a4.6 4.6 0 0 1 4.5-5.6Z'
    },
    { tag: 'path', d: 'M8.6 10.6v3.2' },
    { tag: 'path', d: 'M7 12.2h3.2' },
    { tag: 'path', d: 'M15.7 11h.01' },
    { tag: 'path', d: 'M17.9 13.2h.01' }
  ],
  // 纸飞机（职位投递）
  send: [
    { tag: 'path', d: 'M21.3 2.7 3.4 9.9l7 2.7 2.6 7 8.3-16.9Z' },
    { tag: 'path', d: 'M21.3 2.7 10.4 12.6' }
  ],
  // 火箭 + 尾焰线（AI 职业模拟舱）
  rocket: [
    {
      tag: 'path',
      d: 'M12 2.6c2.6 1.9 4 4.7 4 7.8 0 2.1-.5 4.1-1.4 6H9.4c-.9-1.9-1.4-3.9-1.4-6 0-3.1 1.4-5.9 4-7.8Z'
    },
    { tag: 'circle', cx: 12, cy: 9.6, r: 1.7 },
    { tag: 'path', d: 'M8.2 12.9c-1.9.8-3 2.5-3.4 4.9 1.6-.2 3 .1 4.1.7' },
    { tag: 'path', d: 'M15.8 12.9c1.9.8 3 2.5 3.4 4.9-1.6-.2-3 .1-4.1.7' },
    { tag: 'path', d: 'M12 18.4v2.4' },
    { tag: 'path', d: 'M9.7 17.9l-.5 1.7' },
    { tag: 'path', d: 'M14.3 17.9l.5 1.7' }
  ],
  // 盾牌 + 内勾（防篡改徽章）
  'shield-check': [
    {
      tag: 'path',
      d: 'M12 2.8l7.3 2.8v5.3c0 4.7-2.9 8.3-7.3 10.3-4.4-2-7.3-5.6-7.3-10.3V5.6L12 2.8Z'
    },
    { tag: 'path', d: 'M9.1 11.8l2 2 3.8-4.1' }
  ],
  // 趋势图（薪资白皮书）
  chart: [
    { tag: 'path', d: 'M3.5 3.8v15.4a1.3 1.3 0 0 0 1.3 1.3h15.7' },
    { tag: 'path', d: 'M7 15.8l3.9-4.1 2.9 2.5 4.7-6' },
    { tag: 'path', d: 'M15.6 8.2h2.9v2.9' }
  ],
  // 公文包（职位/工作）
  briefcase: [
    { tag: 'rect', x: 3.5, y: 7.2, width: 17, height: 12.8, rx: 2.6 },
    { tag: 'path', d: 'M9 7.2V5.6A1.6 1.6 0 0 1 10.6 4h2.8A1.6 1.6 0 0 1 15 5.6v1.6' },
    { tag: 'path', d: 'M3.5 12.6c2.8 1.2 5.6 1.8 8.5 1.8s5.7-.6 8.5-1.8' },
    { tag: 'path', d: 'M12 14.4v1.6' }
  ],
  // 礼盒 / 盲盒
  gift: [
    { tag: 'rect', x: 3.8, y: 8.2, width: 16.4, height: 3.8, rx: 1 },
    { tag: 'path', d: 'M5.2 12v6.2A1.8 1.8 0 0 0 7 20h10a1.8 1.8 0 0 0 1.8-1.8V12' },
    { tag: 'path', d: 'M12 8.2V20' },
    { tag: 'path', d: 'M12 8.2c-1.9-3.4-5.8-3.2-5.4-.8.3 1.8 2.8 1.7 5.4.8Z' },
    { tag: 'path', d: 'M12 8.2c1.9-3.4 5.8-3.2 5.4-.8-.3 1.8-2.8 1.7-5.4.8Z' }
  ],
  // 星芒（提示 / 装饰）
  sparkles: [
    { tag: 'path', d: 'M11 4.8c.5 3 1.7 4.2 4.7 4.7-3 .5-4.2 1.7-4.7 4.7-.5-3-1.7-4.2-4.7-4.7 3-.5 4.2-1.7 4.7-4.7Z' },
    { tag: 'path', d: 'M18.2 13.6c.3 1.7 1 2.4 2.6 2.6-1.7.3-2.3 1-2.6 2.6-.3-1.7-1-2.3-2.6-2.6 1.6-.3 2.3-.9 2.6-2.6Z' },
    { tag: 'path', d: 'M6.2 15.9c.2 1.2.7 1.7 1.9 1.9-1.2.2-1.7.7-1.9 1.9-.2-1.2-.7-1.7-1.9-1.9 1.2-.2 1.7-.7 1.9-1.9Z' }
  ],
  // 箭头（卡片入口）
  'arrow-right': [
    { tag: 'path', d: 'M4.5 12h14.5' },
    { tag: 'path', d: 'M13.5 6.5 19 12l-5.5 5.5' }
  ],
  // 时钟（本周提示）
  clock: [
    { tag: 'circle', cx: 12, cy: 12, r: 8.6 },
    { tag: 'path', d: 'M12 7.6V12l2.9 1.9' }
  ],
  // 企业大楼
  building: [
    { tag: 'path', d: 'M4.5 21V5.4A1.9 1.9 0 0 1 6.4 3.5h7.2a1.9 1.9 0 0 1 1.9 1.9V21' },
    { tag: 'path', d: 'M15.5 9.5h2.1a1.9 1.9 0 0 1 1.9 1.9V21' },
    { tag: 'path', d: 'M2.8 21h18.4' },
    { tag: 'path', d: 'M8 7.4h1.6M11.4 7.4H13M8 11h1.6M11.4 11H13M8 14.6h1.6M11.4 14.6H13' },
    { tag: 'path', d: 'M10 21v-2.6a1.2 1.2 0 0 1 2.4 0V21' }
  ],
  // 地图定位（城市）
  'map-pin': [
    { tag: 'path', d: 'M12 21.2s-6.9-5.2-6.9-10.6a6.9 6.9 0 0 1 13.8 0c0 5.4-6.9 10.6-6.9 10.6Z' },
    { tag: 'circle', cx: 12, cy: 10.4, r: 2.6 }
  ],
  // 火焰（热度）
  flame: [
    { tag: 'path', d: 'M12 2.9c1 2.5.5 4.1-.7 5.7-1.3 1.8-3.3 3.2-3.3 5.8a4.9 4.9 0 0 0 4 4.9 5 5 0 0 0 5-5.3c0-4.1-2.7-6-5-11.1Z' },
    { tag: 'path', d: 'M12 19.3c-1.3 0-2.3-1-2.3-2.3 0-1.1.8-1.9 2.3-3.4 1.5 1.5 2.3 2.3 2.3 3.4 0 1.3-1 2.3-2.3 2.3Z' }
  ]
}

// 别名
ICONS.shield = ICONS['shield-check']
ICONS.box = ICONS.gift
ICONS.trend = ICONS.chart
ICONS.arrow = ICONS['arrow-right']

const shapes = computed(() => ICONS[props.name] || ICONS.sparkles)
</script>
