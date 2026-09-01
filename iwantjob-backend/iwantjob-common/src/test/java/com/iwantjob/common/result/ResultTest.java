package com.iwantjob.common.result;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 统一响应体单元测试
 */
class ResultTest {

    @Test
    void successShouldHaveZeroCodeAndData() {
        Result<String> r = Result.success("hello");
        assertEquals(0, r.getCode());
        assertEquals("success", r.getMessage());
        assertEquals("hello", r.getData());
        assertTrue(r.isSuccess());
    }

    @Test
    void successWithoutDataShouldHaveNullData() {
        Result<Void> r = Result.success();
        assertEquals(0, r.getCode());
        assertNull(r.getData());
        assertTrue(r.isSuccess());
    }

    @Test
    void successWithCustomMessageShouldKeepCodeZero() {
        Result<String> r = Result.success("data", "自定义消息");
        assertEquals(0, r.getCode());
        assertEquals("自定义消息", r.getMessage());
        assertTrue(r.isSuccess());
    }

    @Test
    void failWithErrorCodeShouldCopyCodeAndMessage() {
        Result<Void> r = Result.fail(ErrorCode.USER_EXISTS);
        assertEquals(ErrorCode.USER_EXISTS.getCode(), r.getCode());
        assertEquals(ErrorCode.USER_EXISTS.getMessage(), r.getMessage());
        assertNull(r.getData());
        assertFalse(r.isSuccess());
    }

    @Test
    void failWithRawCodeAndMessageShouldKeepValues() {
        Result<Void> r = Result.fail(500, "服务器内部错误");
        assertEquals(500, r.getCode());
        assertEquals("服务器内部错误", r.getMessage());
        assertFalse(r.isSuccess());
    }

    @Test
    void timestampShouldBeSetOnCreation() {
        long before = System.currentTimeMillis();
        Result<Void> r = Result.success();
        long after = System.currentTimeMillis();
        assertTrue(r.getTimestamp() >= before && r.getTimestamp() <= after);
    }
}
