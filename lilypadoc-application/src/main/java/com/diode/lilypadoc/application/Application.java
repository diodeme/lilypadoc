package com.diode.lilypadoc.application;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Slf4j
@EnableScheduling
@SpringBootApplication(exclude = {JacksonAutoConfiguration.class}, scanBasePackages = {"com.diode.lilypadoc"})
public class Application {

    public static void main(String[] args) {
        try {
            SpringApplication.run(Application.class, args);
            System.out.println("ApplicationStarted");
            log.info("ApplicationStarted");
        } catch (Throwable e) {
            log.error("ApplicationFailed",e);
        }
    }

}