import { watch } from 'vue'
import { useTheme } from './useTheme'

/**
 * 图表配色:实时读取主题 CSS 变量(echarts canvas 不感知 CSS,需手动传入)
 */
export function chartPalette() {
  const cs = getComputedStyle(document.documentElement)
  const v = (name) => cs.getPropertyValue(name).trim()
  const isDark = !document.documentElement.classList.contains('light')
  return {
    isDark,
    textPrimary: v('--foreground'),
    textSecondary: v('--foreground-muted'),
    textMuted: v('--foreground-subtle'),
    axisLine: v('--hairline'),
    splitLine: v('--hairline'),
    tooltipBg: isDark ? 'rgba(18, 18, 26, 0.95)' : 'rgba(255, 255, 255, 0.97)',
    tooltipBorder: v('--hairline-strong'),
    tooltipText: v('--foreground'),
    /** 饼图扇区描边:深色用底色融合,浅色用白描边 */
    itemBorder: isDark ? 'rgba(10, 10, 15, 0.9)' : '#ffffff',
    /** axisPointer 阴影 */
    shadow: isDark ? 'rgba(255, 255, 255, 0.05)' : 'rgba(0, 0, 0, 0.04)'
  }
}

/**
 * 主题切换时重建图表:dispose 全部 → 调用 rebuild() 重新 setOption
 * rebuild 内部应重新调用 chartPalette() 取新配色
 */
export function onChartThemeRebuild(rebuild) {
  const { theme } = useTheme()
  watch(theme, () => {
    // 等 CSS 变量与 class 应用完成
    requestAnimationFrame(() => rebuild())
  })
}
