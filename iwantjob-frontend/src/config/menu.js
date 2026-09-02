// 全站导航单一配置源:路由 meta、侧边栏菜单、角色口径均由此派生
// 图标为 lucide-vue-next 组件(现代线性图标)
import {
  Home, Building2, Drama, TrendingUp, Award,
  BarChart3, Briefcase, FileText, Star, BookOpen,
  Sparkles, LayoutGrid, ClipboardList, Gauge
} from 'lucide-vue-next'

export const MENU_GROUPS = [
  {
    label: '总览',
    items: [
      { path: '/dashboard', title: '首页', icon: Home, roles: null },
      { path: '/companies', title: '企业主页', icon: Building2, roles: null },
      { path: '/simulator', title: 'AI 模拟舱', icon: Drama, roles: null },
      { path: '/salary', title: '薪资白皮书', icon: TrendingUp, roles: null },
      { path: '/badges', title: '我的徽章', icon: Award, roles: null }
    ]
  },
  {
    label: '求职',
    items: [
      { path: '/market', title: '岗位市场', icon: BarChart3, roles: [0, 1, 9] },
      { path: '/jobs', title: '职位广场', icon: Briefcase, roles: null },
      { path: '/applied', title: '我的投递', icon: FileText, roles: [0, 1] },
      { path: '/favorites', title: '我的收藏', icon: Star, roles: [0, 1] },
      { path: '/learning', title: '学习中心', icon: BookOpen, roles: [0, 1, 9] },
      { path: '/resume-ai', title: 'AI 简历助手', icon: Sparkles, roles: [0, 1] },
      { path: '/portfolio', title: '作品广场', icon: LayoutGrid, roles: [0, 1, 9] }
    ]
  },
  {
    label: '招聘',
    items: [
      { path: '/hr/jobs', title: '职位管理', icon: ClipboardList, roles: [2, 9] }
    ]
  },
  {
    label: '运营',
    items: [
      { path: '/admin', title: '运营看板', icon: Gauge, roles: [9] }
    ]
  }
]

// 拍平全量(路由守卫口径同步用)
export const ALL_MENU_ITEMS = MENU_GROUPS.flatMap(g => g.items)

/** 当前角色可见菜单组(roles=null 表示全员) */
export function visibleGroups(role) {
  if (role === undefined || role === null) return MENU_GROUPS.map(g => ({ ...g, items: g.items.filter(i => !i.roles) }))
  return MENU_GROUPS
    .map(g => ({ ...g, items: g.items.filter(i => !i.roles || i.roles.includes(role)) }))
    .filter(g => g.items.length > 0)
}
