package com.openrank.openrank;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.openrank.openrank.mapper")
@SpringBootApplication
public class OpenrankApplication {

    public static void main(String[] args) {
        SpringApplication.run(OpenrankApplication.class, args);
    }

}
