package com.university.sms;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * 学生信息管理系统启动类
 * 
 * @author SMS Team
 * @since 1.0.0
 */
@SpringBootApplication
@MapperScan("com.university.sms.*.mapper")
@EnableTransactionManagement
@EnableAsync
public class SmsApplication {

    public static void main(String[] args) {
        SpringApplication.run(SmsApplication.class, args);
    }
}
