package com.wkom.banking.auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class AuthorisationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AuthorisationServiceApplication.class, args);
    }
}
