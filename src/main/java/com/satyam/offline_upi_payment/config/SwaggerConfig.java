package com.satyam.offline_upi_payment.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI offlineUpiOpenAPI() {

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
                );
    }
}