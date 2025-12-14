package com.promox.flashsale.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI flashSaleServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("PromoX FlashSale Service API")
                        .description("REST API for managing flash sales with Redis atomic operations and distributed locking")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("PromoX Team")
                                .email("support@promox.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}
