package com.promox.reward.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI rewardServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("PromoX Reward Service API")
                        .description("REST API for managing reward points, tiers, and achievements")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("PromoX Team")
                                .email("support@promox.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}
