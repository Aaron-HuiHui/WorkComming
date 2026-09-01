package com.iwantjob.job.es;

import com.iwantjob.job.dto.JobSearchDTO;
import com.iwantjob.job.entity.Job;

import java.util.List;

/**
 * 职位搜索引擎抽象
 * <p>
 * job 模块只依赖本接口；具体实现由部署形态决定：
 * <ul>
 *   <li>职位服务（job-server）：{@code EsJobSearchEngine}，走 Elasticsearch</li>
 *   <li>核心服务（core-server）：无实现 Bean，searchJobs 自动回退 MySQL FULLTEXT</li>
 * </ul>
 */
public interface JobSearchEngine {

    /**
     * 关键词检索（含 type/city/batch/companyId 过滤），返回按相关度排序的职位 ID 与总数
     *
     * @throws Exception 引擎不可用或查询失败（调用方负责回退 MySQL）
     */
    EsHitPage searchIds(JobSearchDTO param) throws Exception;

    /**
     * 职位新增/修改后同步索引（失败不应影响业务主流程）
     */
    void upsert(Job job) throws Exception;

    /**
     * 职位下架/删除后移除索引
     */
    void remove(Long jobId) throws Exception;

    /**
     * 检索结果：命中的职位 ID（按相关度降序）与总数
     */
    record EsHitPage(List<Long> ids, long total) {
    }
}
