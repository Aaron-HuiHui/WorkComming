package com.iwantjob.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 统一错误码
 * 段位规则：0=成功 | 1xxxx 通用 | 2xxxx 用户 | 3xxxx 职位 | 4xxxx 简历
 *          5xxxx 面试 | 6xxxx 社区/帮帮团 | 7xxxx 薪资 | 8xxxx 模拟舱 | 9xxxx 徽章
 */
@Getter
@AllArgsConstructor
public enum ErrorCode {

    SUCCESS(0, "success"),

    // 1xxxx 通用
    SYSTEM_ERROR(10000, "系统异常"),
    PARAM_ERROR(10001, "参数校验失败"),
    UNAUTHORIZED(10002, "未登录或token失效"),
    FORBIDDEN(10003, "无权限访问"),
    TOO_MANY_REQUESTS(10004, "请求过于频繁，请稍后再试"),
    IDEMPOTENT_REPEAT(10005, "重复请求"),
    NOT_FOUND(10006, "资源不存在"),
    OPERATION_FAILED(10007, "操作失败"),
    MQ_SEND_FAILED(10008, "消息发送失败"),

    // 2xxxx 用户
    USER_EXISTS(20001, "用户名已存在"),
    USER_NOT_FOUND(20002, "用户不存在"),
    PASSWORD_ERROR(20003, "密码错误"),
    USER_DISABLED(20004, "账号已被禁用"),
    TOKEN_EXPIRED(20005, "token已过期"),
    TOKEN_INVALID(20006, "token无效"),
    PROFILE_NOT_FOUND(20007, "个人资料不存在"),
    POINTS_NOT_ENOUGH(20008, "积分不足"),
    POINTS_CONCURRENT(20009, "积分变更冲突，请重试"),

    // 3xxxx 职位
    JOB_NOT_FOUND(30001, "职位不存在"),
    JOB_EXPIRED(30002, "职位已过期"),
    APPLY_DUPLICATE(30003, "已投递过该职位"),
    APPLICATION_NOT_FOUND(30004, "投递记录不存在"),
    NOT_JOB_OWNER(30005, "无权操作他人职位的投递记录"),
    APPLICATION_STATUS_INVALID(30006, "投递状态不合法"),
    QUESTION_NOT_FOUND(50003, "题目不存在"),

    // 4xxxx 简历
    RESUME_NOT_FOUND(40001, "简历不存在"),
    RESUME_PARSE_FAILED(40002, "简历解析失败"),

    // 5xxxx 面试
    INTERVIEW_NOT_FOUND(50001, "面试会话不存在"),
    INTERVIEW_ENDED(50002, "面试已结束"),

    // 6xxxx 社区/帮帮团
    POST_NOT_FOUND(60001, "帖子不存在"),
    ANSWER_NOT_FOUND(60002, "回答不存在"),
    NOT_POST_AUTHOR(60003, "非帖子作者，无权操作"),
    HELP_REQUEST_NOT_FOUND(60004, "求助请求不存在"),
    HELP_ALREADY_MATCHED(60005, "求助已匹配"),

    // 7xxxx 薪资
    SALARY_DATA_INVALID(70001, "薪资数据不合法"),
    SALARY_DUPLICATE(70002, "本月同岗位已贡献过"),
    SALARY_REJECTED(70003, "薪资数据审核未通过"),
    WHITEPAPER_NOT_FOUND(70004, "白皮书不存在"),
    WHITEPAPER_LOCKED(70005, "白皮书高级章节需积分解锁"),

    // 8xxxx 模拟舱
    SCENARIO_NOT_FOUND(80001, "场景不存在"),
    SESSION_NOT_FOUND(80002, "模拟会话不存在"),
    SESSION_ENDED(80003, "模拟会话已结束"),
    NODE_NOT_FOUND(80004, "节点不存在"),

    // 9xxxx 徽章
    BADGE_NOT_FOUND(90001, "徽章不存在"),
    BADGE_ALREADY_OWNED(90002, "已拥有该徽章"),
    BADGE_LOCKED(90003, "徽章已锁定，不可修改");

    private final int code;
    private final String message;
}
