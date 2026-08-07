package com.satyam.offline_upi_payment.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI offlineUpiOpenAPI() {

        final String securitySchemeName = "bearerAuth";

        return new OpenAPI()

                .info(
                        new Info()
                                .title("Offline UPI Payment API")
                                .version("1.0")
                                .description("Secure Offline UPI Payment Backend with Hybrid Encryption (AES + RSA)")
                                .contact(
                                        new Contact()
                                                .name("Satyam Kumar")
                                                .email("satyamkumarsharma245@gmail.com")
                                )
                )

                .addSecurityItem(
                        new SecurityRequirement()
                                .addList(securitySchemeName)
                )

                .components(
                        new Components()
                                .addSecuritySchemes(
                                        securitySchemeName,
                                        new SecurityScheme()
                                                .name("Authorization")
                                                .type(SecurityScheme.Type.HTTP)
                                                .scheme("bearer")
                                                .bearerFormat("JWT")
                                )
                );
    }
}