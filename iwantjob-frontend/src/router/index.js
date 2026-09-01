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
      // ===== 学生侧 =====
      { path: 'jobs', name: 'Jobs', component: () => import('../views/Jobs.vue'), meta: { title: '职位广场' } },
      { path: 'market', name: 'Market', component: () => import('../views/JobMarket.vue'), meta: { title: '岗位市场' } },
      { path: 'applied', name: 'Applied', component: () => import('../views/MyApplied.vue'), meta: { title: '我的投递' } },
      { path: 'learning', name: 'Learning', component: () => import('../views/Learning.vue'), meta: { title: '学习中心' } },
      { path: 'resume-ai', name: 'ResumeAI', component: () => import('../views/ResumeAI.vue'), meta: { title: 'AI 简历助手' } },
      { path: 'portfolio', name: 'Portfolio', component: () => import('../views/Portfolio.vue'), meta: { title: '作品广场' } },
      { path: 'favorites', name: 'Favorites', component: () => import('../views/MyFavorites.vue'), meta: { title: '我的收藏' } },
      // ===== HR 侧 =====
      { path: 'hr/jobs', name: 'HrJobs', component: () => import('../views/HrJobs.vue'), meta: { title: '职位管理' } },
      { path: 'companies', name: 'Companies', component: () => import('../views/Companies.vue'), meta: { title: '企业主页' } },
      // ===== 通用 =====
      { path: 'simulator', name: 'Simulator', component: () => import('../views/Simulator.vue'), meta: { title: 'AI 模拟舱' } },
      { path: 'salary', name: 'Salary', component: () => import('../views/Salary.vue'), meta: { title: '薪资白皮书' } },
      { path: 'badges', name: 'Badges', component: () => import('../views/Badges.vue'), meta: { title: '我的徽章' } },
      { path: 'admin', name: 'Admin', component: () => import('../views/AdminDashboard.vue'), meta: { title: '运营看板' } }
    ]
  },
  { path: '/:pathMatch(.*)*', redirect: '/dashboard' }
]

const router = createRouter({
  history: createWebHashHistory(),
  routes
})

// 全局守卫：未登录跳登录页
router.beforeEach(to => {
  const token = localStorage.getItem('accessToken')
  if (!to.meta.public && !token) {
    return { name: 'Login' }
  }
})

export default router