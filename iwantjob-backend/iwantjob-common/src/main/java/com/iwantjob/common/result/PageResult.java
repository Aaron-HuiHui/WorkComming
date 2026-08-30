package com.iwantjob.common.result;

import lombok.Data;
import java.io.Serializable;
import java.util.List;

/**
 * 统一分页响应
 */
@Data
public class PageResult<T> implements Serializable {

    private List<T> records;
    private long total;
    private long page;
    private long size;

    public static <T> PageResult<T> of(List<T> records, long total, long page, long size) {
        PageResult<T> r = new PageResult<>();
        r.setRecords(records);
        r.setTotal(total);
        r.setPage(page);
        r.setSize(size);
        return r;
    }
}
