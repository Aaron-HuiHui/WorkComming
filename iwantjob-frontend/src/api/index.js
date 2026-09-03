import request from './request'

// ==================== 认证 ====================
export const authApi = {
  login: data => request.post('/auth/login', data),
  register: data => request.post('/auth/register', data),
  logout: accessToken => request.post('/auth/logout', null, { headers: { 'X-Idempotency-Key': '' } })
}

// ==================== 用户/积分 ====================
export const userApi = {
  me: () => request.get('/user/me'),
  points: () => request.get('/points/me'),
  myBadges: () => request.get('/user/badges')
}

// ==================== 职位 ====================
export const jobApi = {
  search: params => request.get('/jobs', { params }),
  detail: id => request.get(`/jobs/${id}`),
  publish: data => request.post('/jobs', data, {
    headers: { 'X-Idempotency-Key': crypto.randomUUID() }
  }),
  apply: (id, data) => request.post(`/jobs/${id}/apply`, data, {
    headers: { 'X-Idempotency-Key': crypto.randomUUID() }
  }),
  myApplied: params => request.get('/jobs/me/applied', { params }),
  appliedIds: () => request.get('/jobs/me/applied-ids'),
  // 岗位市场统计（学生可视化）
  stats: () => request.get('/jobs/stats/overview'),
  // ===== 收藏 =====
  toggleFavorite: id => request.post(`/jobs/${id}/favorite`),
  myFavorites: params => request.get('/jobs/me/favorites', { params }),
  favoriteIds: () => request.get('/jobs/me/favorite-ids'),
  // ===== HR 候选人管理 =====
  myPublished: params => request.get('/jobs/me/published', { params }),
  jobApplications: (jobId, params) => request.get(`/jobs/${jobId}/applications`, { params }),
  candidateDetail: appId => request.get(`/jobs/applications/${appId}/candidate`),
  updateApplicationStatus: (appId, data) => request.put(`/jobs/applications/${appId}/status`, data, {
    headers: { 'X-Idempotency-Key': crypto.randomUUID() }
  })
}

// ==================== 面试/题库 ====================
export const interviewApi = {
  questions: params => request.get('/interview/questions', { params }),
  questionDetail: id => request.get(`/interview/questions/${id}`),
  start: data => request.post('/interview/start', data, {
    headers: { 'X-Idempotency-Key': crypto.randomUUID() }
  }),
  answer: data => request.post('/interview/answer', data),
  history: params => request.get('/interview/history', { params })
}

// ==================== 简历（AI 智能体） ====================
export const resumeApi = {
  myResumes: () => request.get('/resume/me'),
  detail: id => request.get(`/resume/${id}`),
  create: data => request.post('/resume', data, {
    headers: { 'X-Idempotency-Key': crypto.randomUUID() }
  }),
  optimize: data => request.post('/resume/optimize', data),
  score: resumeId => request.post(`/resume/score?resumeId=${resumeId}`, null, {
    headers: { 'X-Idempotency-Key': crypto.randomUUID() }
  }),
  match: (resumeId, jobId) => request.get('/resume/match', { params: { resumeId, jobId } })
}

// ==================== 模拟舱（创新2） ====================
export const simulatorApi = {
  scenarios: () => request.get('/simulator/scenarios'),
  start: scenarioId => request.post(`/simulator/start?scenarioId=${scenarioId}`, null, {
    headers: { 'X-Idempotency-Key': crypto.randomUUID() }
  }),
  choose: data => request.post('/simulator/choose', data),
  report: sessionId => request.get(`/simulator/session/${sessionId}/report`),
  mySessions: params => request.get('/simulator/sessions/me', { params })
}

// ==================== AI 智能体自由问答 ====================
export const agentApi = {
  // DeepSeek 为推理模型,长回答可达 40s+,单独放宽超时(全局默认 15s)
  ask: data => request.post('/agent/ask', data, { timeout: 120000 })
}

// ==================== 徽章（创新3） ====================
export const badgeApi = {
  templates: () => request.get('/badges/templates')
}

// ==================== 薪资白皮书（创新1） ====================
export const salaryApi = {
  contribute: data => request.post('/salary/contribute', data),
  myContributions: params => request.get('/salary/contributions/me', { params }),
  latestWhitepaper: () => request.get('/salary/whitepaper/latest')
}
// ==================== 企业信息 ====================
export const companyApi = {
  list: params => request.get('/companies', { params }),
  detail: id => request.get(`/companies/${id}`),
  update: (id, data) => request.put(`/companies/${id}`, data)
}

// ==================== 作品集 ====================
export const portfolioApi = {
  list: params => request.get('/portfolio', { params }),
  mine: params => request.get('/portfolio/me', { params }),
  detail: id => request.get(`/portfolio/${id}`),
  create: data => request.post('/portfolio', data, {
    headers: { 'X-Idempotency-Key': crypto.randomUUID() }
  }),
  update: (id, data) => request.put(`/portfolio/${id}`, data),
  remove: id => request.delete(`/portfolio/${id}`),
  toggleLike: id => request.post(`/portfolio/${id}/like`)
}

// ==================== 站内通知 ====================
export const notifyApi = {
  myNotifications: params => request.get('/notify/me', { params }),
  unreadCount: () => request.get('/notify/me/unread-count'),
  markRead: id => request.put(`/notify/${id}/read`),
  markAllRead: () => request.put('/notify/me/read-all')
}

// ==================== 管理员运营看板 ====================
export const adminApi = {
  overview: () => request.get('/admin/overview'),
  // ===== 薪资审核 =====
  salaryPending: params => request.get('/admin/salary/pending', { params }),
  salaryReview: (id, data) => request.put(`/admin/salary/${id}/review`, data, {
    headers: { 'X-Idempotency-Key': crypto.randomUUID() }
  }),
  generateWhitepaper: () => request.post('/admin/whitepaper/generate', null, {
    headers: { 'X-Idempotency-Key': crypto.randomUUID() }
  }),
  // ===== 徽章模板管理 =====
  badgeTemplates: () => request.get('/admin/badges/templates'),
  createBadgeTemplate: data => request.post('/admin/badges/templates', data, {
    headers: { 'X-Idempotency-Key': crypto.randomUUID() }
  })
}
