package com.ecom.pipeline.controller;

import com.ecom.pipeline.dto.AnalyticsSummaryDto;
import com.ecom.pipeline.dto.ApiResponse;
import com.ecom.pipeline.service.AnalyticsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@Tag(name = "Analytics", description = "Dashboard KPIs and trend data")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/summary")
    @Operation(
            summary = "Dashboard summary",
            description = "Returns total revenue, orders, customers, top products, revenue trend, and status distribution"
    )
    public ResponseEntity<ApiResponse<AnalyticsSummaryDto>> summary(
            @RequestParam(defaultValue = "12") int trendMonths,
            @RequestParam(defaultValue = "5") int topProducts
    ) {
        AnalyticsSummaryDto dto = analyticsService.getSummary(trendMonths, topProducts);
        return ResponseEntity.ok(ApiResponse.ok(dto));
    }
}
