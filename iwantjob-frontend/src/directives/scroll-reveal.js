/**
 * 滚动叙事指令（Scrollytelling，参考 AI建站-3D图片交互 设计规范）
 * <p>
 * 用法：<div v-reveal> 或 <div v-reveal="120">（值为延迟 ms，用于级联错峰）。
 * 元素进入视口时从下方淡入上浮，形成「滚动驱动内容显现」的连续镜头感。
 * prefers-reduced-motion 用户直接可见，不做动画。
 */
let observer = null

function getObserver() {
  if (!observer) {
    observer = new IntersectionObserver(
      entries => {
        for (const entry of entries) {
          if (entry.isIntersecting) {
            entry.target.classList.add('rv-in')
            observer.unobserve(entry.target)
          }
        }
      },
      { threshold: 0.12, rootMargin: '0px 0px -36px 0px' }
    )
  }
  return observer
}

export const scrollReveal = {
  mounted(el, binding) {
    if (window.matchMedia && window.matchMedia('(prefers-reduced-motion: reduce)').matches) {
      el.classList.add('rv-in')
      return
    }
    if (binding.value != null) {
      el.style.transitionDelay = binding.value + 'ms'
    }
    el.classList.add('rv')
    getObserver().observe(el)
  },
  unmounted(el) {
    if (observer) {
      observer.unobserve(el)
    }
  }
}
