package com.bakeryshop;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties
public class BakeryShopApplication {
    public static void main(String[] args) {
        SpringApplication.run(BakeryShopApplication.class, args);
    }
} 