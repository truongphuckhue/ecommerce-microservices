package com.ecommerce.payment.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Payment Service API",
        version = "1.0",
        description = "Payment Service for E-Commerce Platform"
    )
)
public class OpenAPIConfig {
}
