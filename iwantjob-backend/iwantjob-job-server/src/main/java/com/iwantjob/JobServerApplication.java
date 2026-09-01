package com.iwantjob;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 职位微服务启动器（端口 8082）
 * 与核心服务共享 MySQL/Redis，JWT 各自无状态验签（密钥一致）
 */
@SpringBootApplication(scanBasePackages = "com.iwantjob")
@MapperScan("com.iwantjob.**.mapper")
public class JobServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(JobServerApplication.class, args);
    }
}