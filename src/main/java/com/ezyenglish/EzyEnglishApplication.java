package com.ezyenglish;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class EzyEnglishApplication {

    public static void main(String[] args) {
        // Force TLS 1.2 for MongoDB Atlas compatibility with some Java 17 environments
        System.setProperty("jdk.tls.client.protocols", "TLSv1.2");
        SpringApplication.run(EzyEnglishApplication.class, args);
    }
}
