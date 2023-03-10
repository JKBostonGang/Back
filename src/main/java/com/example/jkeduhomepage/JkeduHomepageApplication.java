package com.example.jkeduhomepage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing
@EnableScheduling
public class JkeduHomepageApplication {

    public static void main(String[] args) {
        SpringApplication.run(JkeduHomepageApplication.class, args);
    }

}
