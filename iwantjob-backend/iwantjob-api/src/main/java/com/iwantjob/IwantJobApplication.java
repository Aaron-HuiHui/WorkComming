package com.iwantjob;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication(scanBasePackages = "com.iwantjob")
@MapperScan("com.iwantjob.**.mapper")
@EnableAsync
public class IwantJobApplication {

    public static void main(String[] args) {
        SpringApplication.run(IwantJobApplication.class, args);
    }
}
