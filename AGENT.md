# AGENT.md — 多智能体协作日志

> 本文件持续记录「我要工作」平台开发过程中各任务的状态。
> 团队负责人（team-lead）维护，各子智能体完成后汇报，负责人统一更新。

**当前分支**：`feat/phase1-foundation-auth`
**当前阶段**：第一阶段 — 工程骨架 + 用户认证 + 公共规范
**基准文档**：`我要工作-开发文档（优化版最终稿v2.0）.md`

---

## 任务总览

| ID | 任务名称 | 负责人 | 状态 | 简要说明 |
|----|----------|--------|------|----------|
| T0 | Git 初始化与分支创建 | team-lead | ✅ 已完成 | main 提交文档，创建 feat/phase1-foundation-auth |
| T1 | AGENT.md 协作追踪 | team-lead | ✅ 已完成 | 本文件 |
| T2 | Maven 多模块工程骨架 | team-lead | ✅ 已完成 | 父pom + 全部子模块pom + 目录结构（13个pom） |
| T3 | common 公共层 | team-lead | ✅ 已完成 | Result/ErrorCode/PageResult/PageParam + 9个枚举 + BusinessException |
| T4 | framework 基础设施层 | team-lead | ✅ 已完成 | 全局异常/TraceId/MyBatisPlus/Redis/Knife4j/Security/JWT/幂等/限流/审计切面 |
| T5 | iwantjob-user 用户与认证模块 | agent-user | ✅ 已完成 | 30个Java文件：5实体+5Mapper+8DTO+4Service+4Controller，8个API |
| T6 | iwantjob-ai AI 基础设施模块 | agent-ai | ✅ 已完成 | 7个Java文件：接口+Mock+千问实现+限流+重试 |
| T7 | 数据库脚本与初始化数据 | agent-db | ✅ 已完成 | 5个文件：docker-compose + 35张表建表SQL + 初始数据 + 清库脚本 + README |

## 并行划分与冲突避免策略

- **前置串行（T2→T3→T4）**：由 team-lead 完成，是并行任务的基础依赖。
- **并行阶段（T5/T6/T7）**：T4 完成后同时启动，三者文件目录互不交叉：
  - agent-user → `iwantjob-backend/iwantjob-modules/iwantjob-user/`
  - agent-ai → `iwantjob-backend/iwantjob-ai/`
  - agent-db → `iwantjob-backend/db/`
- **冲突避免**：各子智能体仅在自己负责目录内创建文件，禁止修改 common/framework/其他业务模块。
- **结果**：✅ 零冲突，三个子智能体全部独立完成，文件无交叉。

## 状态图例

- ⏳ 待开始（被前置阻塞或等待分派）
- 🔄 进行中
- ✅ 已完成
- ⚠️ 有阻塞（见备注）

## 第一阶段产出统计

- **Java 文件总数**：68 个
- **数据库脚本**：5 个（含 35 张表）
- **API 接口**：8 个（注册/登录/刷新/登出/用户信息/资料更新/积分查询/权益解锁）
- **公共规范**：统一响应体、错误码（9段位）、全局异常、日志(traceId)、幂等、限流、审计、数据权限

## 完成记录

### 2026-08-30
- [T0] Git 初始化完成，分支 feat/phase1-foundation-auth 已创建。
- [T1] AGENT.md 创建完成。
- [T2] Maven 多模块骨架完成：父pom + common/framework/ai/modules(9业务模块)/api 共13个pom。
- [T3] common 公共层完成：Result、ErrorCode(46个错误码)、PageResult、PageParam、BusinessException、9个业务枚举(UserRole/JobType/ApplicationStatus/BadgeCond/Rarity/ScenarioType/Edu/Benefit/PointReason)。
- [T4] framework 基础设施层完成：GlobalExceptionHandler、TraceIdFilter、MyBatisPlusConfig(分页+乐观锁+自动填充)、RedisConfig、Knife4jConfig、SecurityConfig、JwtUtils、JwtAuthFilter、SecurityUtils、LoginUser、Idempotent+Aspect、RateLimit+Aspect、AuditLog+Aspect。
- [T5] agent-user 并行完成：用户认证模块30个Java文件，实现8个API（注册/登录/刷新/登出/用户信息/资料更新/积分查询/导师咨询解锁），含乐观锁积分扣减、JWT黑名单登出、幂等控制。
- [T6] agent-ai 并行完成：AI基础设施7个Java文件，含千问SDK封装(重试3次+限流10次/分)+Mock实现(默认启用,无需API key可联调)。
- [T7] agent-db 并行完成：docker-compose(MySQL/Redis/RabbitMQ/MinIO/ES)、35张表建表SQL(含防篡改触发器+FULLTEXT+乐观锁)、初始数据(徽章模板5条/模拟舱场景4条/节点分支2组/题库10条/薪资基准10条)。

---

## 第二阶段待办（就绪评估后更新 23:50）

> 详细评估见 `迭代状态报告.md`

### 前置串行（team-lead，约1-2h）

| ID | 任务 | 状态 | 优先级 | 依赖 | 说明 |
|----|------|:----:|:------:|------|------|
| T8 | 编译验证与修复 | ⏳ | P0 | 无 | mvn compile，修复编译错误 |
| T11 | 文件上传服务(MinIO) | ⏳ | P1 | T8 | 解除 job/resume 阻塞 |
| T12 | BadgeTriggerEvent 事件基类 | ⏳ | P1 | T8 | common/event 定义，解除 badge 阻塞 |

### 并行开发（T8/T11/T12 完成后同步启动）

| ID | 任务 | 负责人 | 状态 | 优先级 | 依赖 | 就绪 |
|----|------|--------|:----:|:------:|------|:----:|
| T9 | iwantjob-job 职位模块 | agent-job | ⏳ | P2 | T8 | ✅ |
| T10 | iwantjob-resume 简历模块 | agent-resume | ⏳ | P2 | T8,T11 | ⚠️待T11 |
| T13 | iwantjob-interview 面试模块 | agent-interview | ⏳ | P2 | T8 | ✅ |
| T14 | iwantjob-community 社区模块 | agent-community | ⏳ | P3 | T8 | ✅ |
| T15 | iwantjob-helpgroup 帮帮团 | agent-helpgroup | ⏳ | P3 | T8 | ✅ |
| T16 | iwantjob-salary 薪资白皮书(创新1) | agent-salary | ⏳ | P3 | T8 | ✅ |
| T17 | iwantjob-simulator 模拟舱(创新2) | agent-simulator | ⏳ | P3 | T8 | ✅ |
| T18 | iwantjob-badge 徽章(创新3) | agent-badge | ⏳ | P3 | T8,T12 | ⚠️待T12 |

### 当前阻塞问题

| # | 问题 | 等级 | 解决方案 | 负责人 |
|---|------|:----:|----------|--------|
| B1 | 编译验证未执行 | P0 | 本地 mvn compile | team-lead |
| B2 | 文件上传未实现 | P1 | T11 实现 | team-lead/agent-file |
| B3 | 事件基类未定义 | P1 | T12 定义 | team-lead |
| B4 | 千问key未配置 | P2 | Mock已可用，配key后联调 | 用户 |
| B6 | @DataScope未实现 | P2 | 第二阶段按需实现 | team-lead |

### 就绪度结论
- 6 模块可直接编码：job/interview/salary/simulator/community/helpgroup
- 3 模块需补前置：resume(待T11)、badge(待T12)、文件上传本身
- 全部 9 模块的表结构与 API 定义均已就位，无遗漏

---

_最后更新：team-lead (2026-08-30 23:50) | 详细报告：迭代状态报告.md_
