package com.ecom.pipeline.repository;

import com.ecom.pipeline.entity.FactOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<FactOrder, String> {

    Page<FactOrder> findByCustomerId(String customerId, Pageable pageable);

    Page<FactOrder> findByStatus(String status, Pageable pageable);

    @Query("SELECT COALESCE(SUM(o.computedTotal), 0) FROM FactOrder o WHERE o.status = 'completed'")
    BigDecimal sumCompletedRevenue();

    @Query("SELECT COUNT(o) FROM FactOrder o WHERE o.status = :status")
    Long countByStatus(@Param("status") String status);

    /** Revenue per month: period = '2025-01' */
    @Query(value = """
            SELECT TO_CHAR(order_dt, 'YYYY-MM') AS period,
                   SUM(computed_total)           AS revenue,
                   COUNT(*)                      AS order_count
            FROM ecom_marts.fact_orders
            WHERE status = 'completed'
            GROUP BY 1
            ORDER BY 1
            LIMIT :months
            """, nativeQuery = true)
    List<Object[]> findMonthlyRevenueTrend(@Param("months") int months);

    /** Top products by revenue */
    @Query(value = """
            SELECT p.product_id,
                   p.name,
                   p.category,
                   COUNT(oi.order_item_id) AS order_count,
                   SUM(oi.quantity * oi.unit_price) AS total_revenue
            FROM ecom_raw.order_items oi
            JOIN ecom_marts.dim_product p ON oi.product_id = p.product_id
            JOIN ecom_marts.fact_orders o ON oi.order_id = o.order_id
            WHERE o.status = 'completed'
            GROUP BY p.product_id, p.name, p.category
            ORDER BY total_revenue DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<Object[]> findTopProducts(@Param("limit") int limit);

    /** Order count grouped by status */
    @Query(value = """
            SELECT status, COUNT(*) AS cnt
            FROM ecom_marts.fact_orders
            GROUP BY status
            """, nativeQuery = true)
    List<Object[]> findOrderCountByStatus();
}
