package com.iwantjob.admin;

import com.iwantjob.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理员运营看板（聚合层实现：跨域统计 SQL 直查共享库）
 * 权限：[Admin]管理员
 */
@Slf4j
@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('9')")
@Tag(name = "运营看板", description = "全站数据统计：用户/职位/投递/作品/企业")
public class AdminController {

    private final JdbcTemplate jdbcTemplate;

    @GetMapping("/overview")
    @Operation(summary = "全站运营数据总览")
    public Result<Map<String, Object>> overview() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("totalUsers", count("SELECT COUNT(*) FROM sys_user WHERE is_deleted = 0"));
        data.put("totalJobs", count("SELECT COUNT(*) FROM job WHERE is_deleted = 0"));
        data.put("totalApplications", count("SELECT COUNT(*) FROM job_application WHERE is_deleted = 0"));
        data.put("totalPortfolios", count("SELECT COUNT(*) FROM portfolio WHERE is_deleted = 0"));
        data.put("totalCompanies", count("SELECT COUNT(*) FROM company WHERE is_deleted = 0"));
        data.put("totalResumes", count("SELECT COUNT(*) FROM resume WHERE is_deleted = 0"));

        // 用户角色分布
        data.put("userRoleDist", queryStat(
                "SELECT role AS k, COUNT(*) AS v FROM sys_user WHERE is_deleted = 0 GROUP BY role",
                r -> Map.of("name", roleLabel(asInt(r.get("k"))), "value", asLong(r.get("v")))));

        // 投递状态分布
        data.put("applicationStatusDist", queryStat(
                "SELECT status AS k, COUNT(*) AS v FROM job_application WHERE is_deleted = 0 GROUP BY status",
                r -> Map.of("name", statusLabel(asInt(r.get("k"))), "value", asLong(r.get("v")))));

        // 近 7 天注册趋势
        data.put("reg7d", queryStat(
                "SELECT DATE_FORMAT(created_at, '%m-%d') AS k, COUNT(*) AS v FROM sys_user " +
                        "WHERE is_deleted = 0 AND created_at >= DATE_SUB(NOW(), INTERVAL 7 DAY) GROUP BY DATE_FORMAT(created_at, '%m-%d') ORDER BY k",
                r -> Map.of("name", String.valueOf(r.get("k")), "value", asLong(r.get("v")))));

        // 热门职位 TOP5
        data.put("hotJobs", queryStat(
                "SELECT CONCAT(title, ' · ', company_name) AS k, view_count AS v FROM job " +
                        "WHERE is_deleted = 0 ORDER BY view_count DESC LIMIT 5",
                r -> Map.of("name", String.valueOf(r.get("k")), "value", asLong(r.get("v")))));

        // 招聘批次分布
        data.put("batchDist", queryStat(
                "SELECT recruitment_batch AS k, COUNT(*) AS v FROM job WHERE is_deleted = 0 AND status = 1 GROUP BY recruitment_batch",
                r -> Map.of("name", batchLabel(asInt(r.get("k"))), "value", asLong(r.get("v")))));

        return Result.success(data);
    }

    private Long count(String sql) {
        Long v = jdbcTemplate.queryForObject(sql, Long.class);
        return v == null ? 0L : v;
    }

    private List<Map<String, Object>> queryStat(String sql,
            java.util.function.Function<Map<String, Object>, Map<String, Object>> mapper) {
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> row : rows) {
            result.add(mapper.apply(row));
        }
        return result;
    }

    private int asInt(Object o) {
        return o == null ? 0 : ((Number) o).intValue();
    }

    private long asLong(Object o) {
        return o == null ? 0L : ((Number) o).longValue();
    }

    private String roleLabel(int role) {
        return switch (role) {
            case 0 -> "学生";
            case 1 -> "校友";
            case 2 -> "HR";
            case 3 -> "导师";
            case 9 -> "管理员";
            default -> "其他";
        };
    }

    private String statusLabel(int s) {
        return switch (s) {
            case 0 -> "已投递";
            case 1 -> "初筛通过";
            case 2 -> "面试中";
            case 3 -> "已录用";
            case 4 -> "未通过";
            default -> "其他";
        };
    }

    private String batchLabel(int b) {
        return switch (b) {
            case 1 -> "春招";
            case 2 -> "秋招";
            case 3 -> "实习批";
            default -> "日常";
        };
    }
}