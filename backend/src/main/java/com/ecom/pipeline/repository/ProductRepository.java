package com.ecom.pipeline.repository;

import com.ecom.pipeline.entity.DimProduct;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductRepository extends JpaRepository<DimProduct, String> {

    Page<DimProduct> findByCategory(String category, Pageable pageable);

    Page<DimProduct> findByNameContainingIgnoreCase(String keyword, Pageable pageable);

    List<DimProduct> findByCategory(String category);

    @Query("SELECT DISTINCT p.category FROM DimProduct p ORDER BY p.category")
    List<String> findAllCategories();

    @Query("""
            SELECT p FROM DimProduct p
            WHERE (:category IS NULL OR p.category = :category)
            AND (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<DimProduct> search(
            @Param("category") String category,
            @Param("keyword") String keyword,
            Pageable pageable
    );
}
