package com.iwantjob.es;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import com.iwantjob.job.dto.JobSearchDTO;
import com.iwantjob.job.entity.Job;
import com.iwantjob.job.es.JobSearchEngine;
import com.iwantjob.job.mapper.JobMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Elasticsearch 职位搜索引擎实现（仅部署在职位服务 job-server）
 * <p>
 * 启动时 ApplicationRunner 全量同步 job 表 → iwantjob_job 索引（数据量小，幂等重建）；
 * 检索：multi_match(title^3, companyName^2, description, requirements) + 过滤条件；
 * 中文按 standard 分词器单字切分，配合 multi_match 的 best_fields 相关度仍可正确召回。
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EsJobSearchEngine implements JobSearchEngine, ApplicationRunner {

    public static final String INDEX = "iwantjob_job";

    private final ElasticsearchClient client;
    private final JobMapper jobMapper;

    /* ==================== 索引生命周期 ==================== */

    @Override
    public void run(ApplicationArguments args) {
        try {
            ensureIndex();
            fullSync();
        } catch (Exception e) {
            log.error("ES 索引初始化失败（搜索将自动回退 MySQL FULLTEXT）: {}", e.getMessage());
        }
    }

    private void ensureIndex() throws Exception {
        boolean exists = client.indices().exists(e -> e.index(INDEX)).value();
        if (exists) {
            return;
        }
        CreateIndexRequest req = new CreateIndexRequest.Builder().index(INDEX)
                .mappings(m -> m
                        .properties("title", p -> p.text(t -> t.analyzer("standard")))
                        .properties("companyName", p -> p.text(t -> t.analyzer("standard")))
                        .properties("description", p -> p.text(t -> t.analyzer("standard")))
                        .properties("requirements", p -> p.text(t -> t.analyzer("standard")))
                        .properties("location", p -> p.keyword(k -> k))
                        .properties("jobType", p -> p.integer(i -> i))
                        .properties("recruitmentBatch", p -> p.integer(i -> i))
                        .properties("companyId", p -> p.long_(l -> l))
                        .properties("status", p -> p.integer(i -> i)))
                .build();
        client.indices().create(req);
        log.info("ES 索引已创建: {}", INDEX);
    }

    /**
     * 全量同步（启动时执行，幂等：覆盖式 bulk index）
     */
    private void fullSync() throws Exception {
        List<Job> jobs = jobMapper.selectList(null);
        if (jobs.isEmpty()) {
            log.info("ES 全量同步跳过：job 表无数据");
            return;
        }
        BulkRequest.Builder bulk = new BulkRequest.Builder();
        for (Job job : jobs) {
            Map<String, Object> doc = toDoc(job);
            bulk.operations(op -> op.index(idx -> idx.index(INDEX).id(String.valueOf(job.getId())).document(doc)));
        }
        BulkResponse resp = client.bulk(bulk.build());
        if (resp.errors()) {
            log.error("ES 全量同步存在失败项: {}", resp.items().stream()
                    .filter(i -> i.error() != null).count());
        }
        log.info("ES 全量同步完成: 索引={}, 文档数={}", INDEX, jobs.size());
    }

    /* ==================== 检索 ==================== */

    @Override
    public EsHitPage searchIds(JobSearchDTO param) throws Exception {
        long from = (param.getPage() - 1) * param.getSize();
        BoolQuery.Builder bool = new BoolQuery.Builder()
                .must(m -> m.multiMatch(mm -> mm
                        .query(param.getKeyword())
                        .fields("title^3", "companyName^2", "description", "requirements")))
                .filter(f -> f.term(t -> t.field("status").value(1)));
        if (param.getType() != null) {
            bool.filter(f -> f.term(t -> t.field("jobType").value(param.getType())));
        }
        if (param.getCity() != null && !param.getCity().isBlank()) {
            bool.filter(f -> f.term(t -> t.field("location").value(param.getCity())));
        }
        if (param.getBatch() != null) {
            bool.filter(f -> f.term(t -> t.field("recruitmentBatch").value(param.getBatch())));
        }
        if (param.getCompanyId() != null) {
            bool.filter(f -> f.term(t -> t.field("companyId").value(param.getCompanyId())));
        }

        // ES Java 客户端的 builder 只能 build 一次，先构建为不可变 query 再复用于 count 与分页两次查询
        co.elastic.clients.elasticsearch._types.query_dsl.Query query =
                new co.elastic.clients.elasticsearch._types.query_dsl.Query.Builder()
                        .bool(bool.build()).build();

        long total = client.search(s -> s.index(INDEX)
                        .query(query)
                        .from(0).size(0).trackTotalHits(t -> t.enabled(true))
                        .source(src -> src.fetch(false)),
                Void.class).hits().total().value();
        if (total == 0 || from >= total) {
            return new EsHitPage(List.of(), total);
        }

        List<Long> ids = client.search(s -> s.index(INDEX)
                        .query(query)
                        .from((int) from).size((int) param.getSize())
                        .source(src -> src.fetch(false)),
                Void.class).hits().hits().stream()
                .map(Hit::id)
                .map(Long::valueOf)
                .toList();
        return new EsHitPage(ids, total);
    }

    /* ==================== 增量同步 ==================== */

    @Override
    public void upsert(Job job) throws Exception {
        client.index(i -> i.index(INDEX).id(String.valueOf(job.getId())).document(toDoc(job)));
        log.info("ES 索引已更新: jobId={}", job.getId());
    }

    @Override
    public void remove(Long jobId) throws Exception {
        client.delete(d -> d.index(INDEX).id(String.valueOf(jobId)));
        log.info("ES 索引已删除: jobId={}", jobId);
    }

    private Map<String, Object> toDoc(Job job) {
        Map<String, Object> doc = new HashMap<>();
        doc.put("title", job.getTitle());
        doc.put("companyName", job.getCompanyName());
        doc.put("description", job.getDescription());
        doc.put("requirements", job.getRequirements());
        doc.put("location", job.getLocation());
        doc.put("jobType", job.getJobType());
        doc.put("recruitmentBatch", job.getRecruitmentBatch());
        doc.put("companyId", job.getCompanyId());
        doc.put("status", job.getStatus());
        return doc;
    }
}
