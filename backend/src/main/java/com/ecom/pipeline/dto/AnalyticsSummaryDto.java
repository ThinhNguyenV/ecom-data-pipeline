package com.ecom.pipeline.dto;

import lombok.*;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyticsSummaryDto {

    private BigDecimal totalRevenue;
    private Long totalOrders;
    private Long activeCustomers;
    private Long totalProducts;

    /** Revenue grouped by time period */
    private List<RevenueTrendPoint> revenueTrend;

    /** Top-selling products by computed revenue */
    private List<TopProductPoint> topProducts;

    /** Order count grouped by status */
    private List<OrderStatusPoint> orderStatusDistribution;

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class RevenueTrendPoint {
        private String period;       // e.g. "2025-01", "2025-W03"
        private BigDecimal revenue;
        private Long orderCount;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class TopProductPoint {
        private String productId;
        private String name;
        private String category;
        private Long orderCount;
        private BigDecimal totalRevenue;
    }

    @Getter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class OrderStatusPoint {
        private String status;
        private Long count;
        private BigDecimal percentage;
    }
}
