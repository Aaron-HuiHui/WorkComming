# 我要工作 平台 - 数据库脚本说明

> 适用范围：`iwantjob-backend/db/` 目录
> 字符集：utf8mb4
> 数据库：MySQL 8.0+

## 一、脚本清单与执行顺序

| 序号 | 文件名 | 用途 | 是否可重复执行 |
|------|--------|------|----------------|
| 0 | `docker-compose.yml` | 本地中间件一键编排（MySQL/Redis/RabbitMQ/MinIO/ES） | 是 |
| 1 | `01-schema.sql` | 完整建表脚本（约 30 张表 + 触发器 + 索引） | 是（CREATE IF NOT EXISTS） |
| 2 | `02-init-data.sql` | 初始化数据（徽章模板/模拟舱场景节点/题库/薪资审核基准） | 是（含 TRUNCATE） |
| 3 | `03-drop.sql` | 清库脚本（开发阶段重建用，⚠️ 不可恢复） | 是 |

**推荐执行顺序**：

```
03-drop.sql  →  01-schema.sql  →  02-init-data.sql
```

首次部署可跳过 `03-drop.sql`，直接执行 `01 → 02`。

## 二、各脚本内容概要

### 1. docker-compose.yml
本地一键启动 5 个中间件：

| 服务 | 镜像 | 端口 | 用途 |
|------|------|------|------|
| MySQL | mysql:8.0 | 3306 | 主数据库，首次启动自动执行 `01-schema.sql` 与 `02-init-data.sql` |
| Redis | redis:7-alpine | 6379 | 缓存/会话/限流/幂等键 |
| RabbitMQ | rabbitmq:3.13-management | 5672 / 15672 | 异步消息（第七周起启用） |
| MinIO | minio/minio | 9000 / 9001 | 文件存储（简历/身份证） |
| Elasticsearch | elasticsearch:8.12.0 | 9200 / 9300 | 搜索引擎（第七周起启用，单节点） |

所有服务挂载持久化卷，加入 `iwantjob-net` 桥接网络，并配置 healthcheck。
MySQL 容器启动时自动挂载 `docker-entrypoint-initdb.d/`，首次启动会自动建表 + 写入基础数据。

### 2. 01-schema.sql
约 30 张表的完整建表脚本，设计要点：

- `CREATE DATABASE iwantjob` + `USE`
- 移除所有跨模块物理外键，仅保留索引（支撑未来分库）
- 业务表统一含 `is_deleted` / `created_at` / `updated_at`（MyBatis-Plus 自动填充）
- `mutual_points` 含 `version` 乐观锁字段
- `job` 与 `post` 含 `FULLTEXT` 索引（ES 未上时兜底）
- `user_badge` 表通过 `BEFORE UPDATE` / `BEFORE DELETE` 触发器实现防篡改（仅允许 INSERT）
- `help_group_request` 含 `match_tags` 字段支撑自动匹配
- 新增表：`sys_audit_log`、`salary_review_log`、`simulator_node`、`simulator_node_option`、`salary_baseline`

模块覆盖：用户/职位/简历/面试/社区/帮帮团/积分/AI对话/薪资白皮书/模拟舱/徽章/审计日志/薪资审核日志/模拟舱节点等。

### 3. 02-init-data.sql
插入基础数据：

- **徽章模板** `badge_template`：5 条，对应 `BadgeCondEnum` 的 5 种条件类型（0分享面经/1帮助他人/2薪资贡献/3模拟舱完成/4项目合作评价），threshold 为 3/5/3/2/1，rarity 为 0/1/1/2/2
- **模拟舱场景** `simulator_scenario`：4 条（入职/向上汇报/冲突处理/跨部门协作）
- **模拟舱节点 + 选项** `simulator_node` + `simulator_node_option`：
  - 入职场景完整分支：起始节点 3 选项 → 2 个结局节点
  - 冲突处理场景分支：起始节点 2 选项 → 2 个结局节点
- **题库** `question_bank`：10 条（技术 5 + 行为 3 + 综合 2），含 `expected_keywords`
- **薪资审核基准** `salary_baseline`：北京/上海/深圳/广州/杭州 × 本科/硕士，共 10 条

### 4. 03-drop.sql
开发用清库脚本：`DROP DATABASE IF EXISTS iwantjob;`，配合 `01`/`02` 可一键重建数据库。

## 三、快速启动（本地开发）

```bash
# 1. 启动所有中间件（MySQL 首启会自动建表+插数据）
cd iwantjob-backend/db
docker compose up -d

# 2. 查看中间件状态
docker compose ps

# 3. 查看 MySQL 启动日志（确认 init 脚本执行）
docker compose logs -f mysql

# 4. 连接 MySQL 验证
docker exec -it iwantjob-mysql mysql -uroot -proot -e "USE iwantjob; SHOW TABLES;"

# 5. 停止服务（保留数据）
docker compose down

# 6. 完全清空（含数据卷，慎用）
docker compose down -v
```

## 四、默认账号密码

| 服务 | 用户名 | 密码 | 备注 |
|------|--------|------|------|
| MySQL | root | root | 数据库 `iwantjob`，连接 `localhost:3306` |
| Redis | (无) | iwantjob | 连接 `localhost:6379`，已开启 AOF |
| RabbitMQ | iwantjob | iwantjob | 管理控制台 `http://localhost:15672` |
| MinIO | minioadmin | minioadmin | 控制台 `http://localhost:9001` |
| Elasticsearch | (无) | (无) | 已关闭 xpack 安全，`http://localhost:9200` |

> ⚠️ 以上为本地开发默认凭据，**生产环境必须替换**为强密码并启用认证。

## 五、手动重建数据库（不依赖 Docker init）

```bash
# 进入 mysql 容器执行
docker exec -i iwantjob-mysql mysql -uroot -proot < 03-drop.sql
docker exec -i iwantjob-mysql mysql -uroot -proot < 01-schema.sql
docker exec -i iwantjob-mysql mysql -uroot -proot iwantjob < 02-init-data.sql
```

或在 MySQL 客户端中按顺序执行三个 SQL 文件。

## 六、注意事项

1. **触发器**：`user_badge` 表的两个触发器（`trg_user_badge_no_update` / `trg_user_badge_no_delete`）会在任何 UPDATE/DELETE 时抛出 `SQLSTATE '45000'` 错误。这是设计如此，确保徽章铸造后不可篡改。如需在开发期重置徽章数据，请先 `DROP TRIGGER` 或 `TRUNCATE` 整表（TRUNCATE 不触发 BEFORE DELETE）。
2. **FULLTEXT 索引**：MySQL 8.0 默认使用 ngram 解析器对中文友好度有限，生产建议启用 ES。建表语句已为 `job` 与 `post` 添加 FULLTEXT 作为兜底。
3. **乐观锁**：`mutual_points.version` 字段需在 MyBatis-Plus 实体上配 `@Version` 注解。
4. **JSON 字段**：`resume.content_json`、`salary_whitepaper.report_json`、`simulator_choice.options_json` 等使用 JSON 类型，注意 MyBatis-Plus 需配 JSON 处理器。
5. **软删除**：所有含 `is_deleted` 字段的表使用 MyBatis-Plus `@TableLogic`，删除即 `is_deleted=1`，查询自动过滤。
