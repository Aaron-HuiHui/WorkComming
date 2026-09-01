import { createRouter, createWebHashHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { public: true }
  },
  {
    path: '/',
    component: () => import('../layout/MainLayout.vue'),
    redirect: '/dashboard',
    children: [
      { path: 'dashboard', name: 'Dashboard', component: () => import('../views/Dashboard.vue'), meta: { title: '首页' } },
      // ===== 学生侧（学生/校友；管理员亦可浏览）=====
      { path: 'jobs', name: 'Jobs', component: () => import('../views/Jobs.vue'), meta: { title: '职位广场' } },
      { path: 'market', name: 'Market', component: () => import('../views/JobMarket.vue'), meta: { title: '岗位市场', roles: [0, 1, 9] } },
      { path: 'applied', name: 'Applied', component: () => import('../views/MyApplied.vue'), meta: { title: '我的投递', roles: [0, 1] } },
      { path: 'favorites', name: 'Favorites', component: () => import('../views/MyFavorites.vue'), meta: { title: '我的收藏', roles: [0, 1] } },
      { path: 'learning', name: 'Learning', component: () => import('../views/Learning.vue'), meta: { title: '学习中心', roles: [0, 1, 9] } },
      { path: 'resume-ai', name: 'ResumeAI', component: () => import('../views/ResumeAI.vue'), meta: { title: 'AI 简历助手', roles: [0, 1] } },
      { path: 'portfolio', name: 'Portfolio', component: () => import('../views/Portfolio.vue'), meta: { title: '作品广场', roles: [0, 1, 9] } },
      // ===== HR 侧（HR/管理员）=====
      { path: 'hr/jobs', name: 'HrJobs', component: () => import('../views/HrJobs.vue'), meta: { title: '职位管理', roles: [2, 9] } },
      { path: 'companies', name: 'Companies', component: () => import('../views/Companies.vue'), meta: { title: '企业主页' } },
      // ===== 通用 =====
      { path: 'simulator', name: 'Simulator', component: () => import('../views/Simulator.vue'), meta: { title: 'AI 模拟舱' } },
      { path: 'salary', name: 'Salary', component: () => import('../views/Salary.vue'), meta: { title: '薪资白皮书' } },
      { path: 'badges', name: 'Badges', component: () => import('../views/Badges.vue'), meta: { title: '我的徽章' } },
      { path: 'admin', name: 'Admin', component: () => import('../views/AdminDashboard.vue'), meta: { title: '运营看板', roles: [9] } }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/dashboard' }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

// 获取用户角色（缓存在 localStorage 的 user 信息里）
function currentUserRole() {
  try {
    const u = JSON.parse(localStorage.getItem('userInfo') || 'null')
    return u?.role
  } catch (e) {
    return undefined
  }
}

// 全局守卫：未登录跳登录页；角色不符跳首页（与侧栏菜单可见性同口径）
router.beforeEach(to => {
  const token = localStorage.getItem('accessToken')
  if (!to.meta.public && !token) {
    return { name: 'Login' }
  }
  if (to.meta.roles && token) {
    const role = currentUserRole()
    // role 为 undefined 时（token 有效但本地无缓存，如异常刷新）放行，由后端 403 兜底
    if (role !== undefined && !to.meta.roles.includes(role)) {
      return { path: '/dashboard', query: { denied: to.path } }
    }
  }
})

export default router
