package com.iwantjob.framework.datascope;

/**
 * 数据权限范围类型
 * <p>
 * 对应开发文档 8.7 节角色数据范围矩阵的通用抽象。
 * 管理员（UserRoleEnum.ADMIN）在拦截器层统一放行。
 */
public enum ScopeType {

    /**
     * 本人数据：column = 当前用户ID。
     * 典型场景：学生/校友查看本人投递、简历、会话记录。
     */
    SELF,

    /**
     * HR 本人发布的职位：poster_id = 当前用户ID。
     * 对应"HR：本公司发布的职位及对应投递"（本实现按发布者维度收口）。
     * 非 HR 角色调用时同样拼接该条件，作为防越权兜底（查询结果为空）。
     */
    HR_COMPANY
}
