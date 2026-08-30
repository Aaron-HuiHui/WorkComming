package com.iwantjob.common.result;

import lombok.Data;
import java.io.Serializable;

/**
 * 统一分页请求参数
 */
@Data
public class PageParam implements Serializable {

    private long page = 1;
    private long size = 10;

    public long getOffset() {
        return (page - 1) * size;
    }
}
