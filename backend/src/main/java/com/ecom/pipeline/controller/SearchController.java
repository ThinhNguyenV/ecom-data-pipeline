package com.ecom.pipeline.controller;

import com.ecom.pipeline.dto.ApiResponse;
import com.ecom.pipeline.dto.SearchResultDto;
import com.ecom.pipeline.service.SearchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
@Tag(name = "Search", description = "Product search via Elasticsearch")
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    @Operation(
            summary = "Search products",
            description = "Full-text multi-match search across name, description, and category fields in Elasticsearch"
    )
    public ResponseEntity<ApiResponse<List<SearchResultDto>>> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "10") int limit
    ) {
        if (q == null || q.isBlank()) {
            throw new IllegalArgumentException("Search query 'q' must not be empty");
        }
        List<SearchResultDto> results = searchService.search(q, limit);
        return ResponseEntity.ok(ApiResponse.ok(
                String.format("Found %d results for '%s'", results.size(), q),
                results
        ));
    }
}
