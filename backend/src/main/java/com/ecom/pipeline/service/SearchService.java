package com.ecom.pipeline.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.ecom.pipeline.dto.SearchResultDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * SearchService delegates kNN vector search to Elasticsearch.
 *
 * NOTE: This backend does NOT generate embeddings. It performs keyword-based
 * search using Elasticsearch's full-text capabilities. For semantic (vector)
 * search, call the Python search_service.py endpoint directly, or extend this
 * service to call that Python microservice via RestTemplate/WebClient.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    private final ElasticsearchClient esClient;

    @Value("${app.elasticsearch.index:products}")
    private String index;

    /**
     * Full-text product search using Elasticsearch multi_match query.
     *
     * @param query   user search string
     * @param topK    max number of results
     */
    @SuppressWarnings("unchecked")
    public List<SearchResultDto> search(String query, int topK) {
        log.info("Searching Elasticsearch for: '{}' (topK={})", query, topK);
        try {
            SearchResponse<Map> response = esClient.search(s -> s
                    .index(index)
                    .size(topK)
                    .query(q -> q
                            .multiMatch(m -> m
                                    .query(query)
                                    .fields(List.of("name^2", "description", "category"))
                            )
                    ),
                    Map.class
            );

            return response.hits().hits().stream()
                    .map(this::toSearchResult)
                    .collect(Collectors.toList());

        } catch (IOException e) {
            log.error("Elasticsearch search failed for query '{}': {}", query, e.getMessage());
            return List.of();
        }
    }

    @SuppressWarnings("unchecked")
    private SearchResultDto toSearchResult(Hit<Map> hit) {
        Map<String, Object> src = hit.source();
        return SearchResultDto.builder()
                .productId(String.valueOf(src.getOrDefault("product_id", "")))
                .name(String.valueOf(src.getOrDefault("name", "")))
                .category(String.valueOf(src.getOrDefault("category", "")))
                .description(String.valueOf(src.getOrDefault("description", "")))
                .score(hit.score())
                .build();
    }
}
