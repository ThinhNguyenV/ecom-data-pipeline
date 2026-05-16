package com.ecom.pipeline.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Ecom Data Pipeline API")
                        .version("1.0.0")
                        .description("""
                                REST API serving the E-Commerce Data Pipeline analytics layer.
                                
                                **Data flow**: Kafka → Spark Streaming → Elasticsearch
                                and PostgreSQL (via dbt) → this API → clients.
                                
                                Endpoints cover product management, order analytics,
                                customer insights, and semantic product search.
                                """)
                        .contact(new Contact()
                                .name("Data Engineering Team")
                                .email("data-eng@ecom.example.com"))
                        .license(new License()
                                .name("MIT")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server().url("http://localhost:8090").description("Local development"),
                        new Server().url("http://backend:8090").description("Docker internal")
                ));
    }
}
