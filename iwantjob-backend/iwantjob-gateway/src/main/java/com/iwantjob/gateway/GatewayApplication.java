package com.iwantjob.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * API 网关启动器
 * 职责：统一入口(8000)、按路径路由到职位服务/核心服务、透传 JWT（各服务无状态验签）
 */
@SpringBootApplication
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}