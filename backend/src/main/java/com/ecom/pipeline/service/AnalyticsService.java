package com.ecom.pipeline.service;

import com.ecom.pipeline.dto.AnalyticsSummaryDto;
import com.ecom.pipeline.dto.AnalyticsSummaryDto.*;
import com.ecom.pipeline.repository.CustomerRepository;
import com.ecom.pipeline.repository.OrderRepository;
import com.ecom.pipeline.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final OrderRepository orderRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;

    /** Full analytics summary (KPIs + trends) for dashboard. */
    public AnalyticsSummaryDto getSummary(int trendMonths, int topProductsLimit) {
        log.debug("Building analytics summary");

        BigDecimal totalRevenue = orderRepository.sumCompletedRevenue();
        Long totalOrders = orderRepository.count();
        Long activeCustomers = customerRepository.countActiveCustomers();
        Long totalProducts = productRepository.count();

        List<RevenueTrendPoint> trend = buildRevenueTrend(trendMonths);
        List<TopProductPoint> top = buildTopProducts(topProductsLimit);
        List<OrderStatusPoint> statusDist = buildStatusDistribution(totalOrders);

        return AnalyticsSummaryDto.builder()
                .totalRevenue(totalRevenue)
                .totalOrders(totalOrders)
                .activeCustomers(activeCustomers)
                .totalProducts(totalProducts)
                .revenueTrend(trend)
                .topProducts(top)
                .orderStatusDistribution(statusDist)
                .build();
    }

    private List<RevenueTrendPoint> buildRevenueTrend(int months) {
        return orderRepository.findMonthlyRevenueTrend(months).stream()
                .map(row -> RevenueTrendPoint.builder()
                        .period(String.valueOf(row[0]))
                        .revenue(new BigDecimal(String.valueOf(row[1])))
                        .orderCount(((Number) row[2]).longValue())
                        .build())
                .collect(Collectors.toList());
    }

    private List<TopProductPoint> buildTopProducts(int limit) {
        return orderRepository.findTopProducts(limit).stream()
                .map(row -> TopProductPoint.builder()
                        .productId(String.valueOf(row[0]))
                        .name(String.valueOf(row[1]))
                        .category(String.valueOf(row[2]))
                        .orderCount(((Number) row[3]).longValue())
                        .totalRevenue(new BigDecimal(String.valueOf(row[4])))
                        .build())
                .collect(Collectors.toList());
    }

    private List<OrderStatusPoint> buildStatusDistribution(long total) {
        List<Object[]> rows = orderRepository.findOrderCountByStatus();
        return rows.stream()
                .map(row -> {
                    long count = ((Number) row[1]).longValue();
                    BigDecimal pct = total > 0
                            ? BigDecimal.valueOf(count * 100.0 / total).setScale(2, RoundingMode.HALF_UP)
                            : BigDecimal.ZERO;
                    return OrderStatusPoint.builder()
                            .status(String.valueOf(row[0]))
                            .count(count)
                            .percentage(pct)
                            .build();
                })
                .collect(Collectors.toList());
    }
}
