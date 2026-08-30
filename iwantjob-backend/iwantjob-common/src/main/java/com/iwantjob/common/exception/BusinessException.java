package com.iwantjob.common.exception;

import com.iwantjob.common.result.ErrorCode;
import lombok.Getter;

/**
 * 业务异常
 */
@Getter
public class BusinessException extends RuntimeException {

    private final int code;

    public BusinessException(ErrorCode ec) {
        super(ec.getMessage());
        this.code = ec.getCode();
    }

    public BusinessException(ErrorCode ec, String message) {
        super(message);
        this.code = ec.getCode();
    }

    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}
