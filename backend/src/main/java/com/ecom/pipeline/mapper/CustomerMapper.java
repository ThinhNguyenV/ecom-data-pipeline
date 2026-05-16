package com.ecom.pipeline.mapper;

import com.ecom.pipeline.dto.CustomerDto;
import com.ecom.pipeline.entity.DimCustomer;
import org.springframework.stereotype.Component;

@Component
public class CustomerMapper {

    public CustomerDto toDto(DimCustomer entity) {
        if (entity == null) return null;
        return CustomerDto.builder()
                .customerId(entity.getCustomerId())
                .firstName(entity.getFirstName())
                .lastName(entity.getLastName())
                .country(entity.getCountry())
                .createdAt(entity.getCreatedAt())
                .emailHash(entity.getEmailHash())
                .build();
    }
}
