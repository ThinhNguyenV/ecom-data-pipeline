package com.ecom.pipeline.repository;

import com.ecom.pipeline.entity.DimCustomer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerRepository extends JpaRepository<DimCustomer, String> {

    Page<DimCustomer> findByCountry(String country, Pageable pageable);

    @Query("SELECT COUNT(DISTINCT c.customerId) FROM DimCustomer c")
    Long countActiveCustomers();
}
