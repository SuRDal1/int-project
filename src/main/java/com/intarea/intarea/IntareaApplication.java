package com.intarea.intarea;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // 스케줄러 사용허가
public class IntareaApplication {

    public static void main(String[] args) {
        SpringApplication.run(IntareaApplication.class, args);
    }

}
