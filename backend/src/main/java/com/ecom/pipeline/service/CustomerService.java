package com.ecom.pipeline.service;

import com.ecom.pipeline.dto.CustomerDto;
import com.ecom.pipeline.entity.DimCustomer;
import com.ecom.pipeline.exception.ResourceNotFoundException;
import com.ecom.pipeline.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    public Page<CustomerDto> findAll(String country, Pageable pageable) {
        if (country != null && !country.isBlank()) {
            return customerRepository.findByCountry(country, pageable).map(this::toDto);
        }
        return customerRepository.findAll(pageable).map(this::toDto);
    }

    public CustomerDto findById(String customerId) {
        return customerRepository.findById(customerId)
                .map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + customerId));
    }

    public CustomerDto toDto(DimCustomer e) {
        return CustomerDto.builder()
                .customerId(e.getCustomerId())
                .firstName(e.getFirstName())
                .lastName(e.getLastName())
                .country(e.getCountry())
                .createdAt(e.getCreatedAt())
                .emailHash(e.getEmailHash())
                .build();
    }
}
