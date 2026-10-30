package com.atlasrh;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class AtlasrhApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(AtlasrhApiApplication.class, args);
    }
}