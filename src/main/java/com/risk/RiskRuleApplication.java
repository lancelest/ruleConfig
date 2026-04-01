package com.risk;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 预警规则配置服务启动类
 */
@SpringBootApplication
@MapperScan("com.risk.mapper")
public class RiskRuleApplication {

    public static void main(String[] args) {
        SpringApplication.run(RiskRuleApplication.class, args);
    }
}
