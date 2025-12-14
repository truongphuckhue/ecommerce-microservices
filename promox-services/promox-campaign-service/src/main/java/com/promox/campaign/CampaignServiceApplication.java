package com.promox.campaign;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class CampaignServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CampaignServiceApplication.class, args);
    }
}
