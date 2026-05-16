package com.ecom.pipeline.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;

@Configuration
public class ElasticsearchConfig extends ElasticsearchConfiguration {

    @Value("${spring.elasticsearch.uris:http://localhost:9200}")
    private String elasticsearchUri;

    @Override
    public ClientConfiguration clientConfiguration() {
        // Strip the scheme prefix expected by ClientConfiguration
        String hostAndPort = elasticsearchUri
                .replace("http://", "")
                .replace("https://", "");

        return ClientConfiguration.builder()
                .connectedTo(hostAndPort)
                .build();
    }
}
