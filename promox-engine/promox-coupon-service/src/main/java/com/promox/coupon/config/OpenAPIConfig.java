package com.promox.coupon.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAPIConfig {

    @Bean
    public OpenAPI couponServiceOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("PromoX Coupon Service API")
                        .description("REST API for managing coupons with bulk generation, validation, and redemption tracking")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("PromoX Team")
                                .email("support@promox.com"))
                        .license(new License()
                                .name("Apache 2.0")
                                .url("https://www.apache.org/licenses/LICENSE-2.0.html")));
    }
}
