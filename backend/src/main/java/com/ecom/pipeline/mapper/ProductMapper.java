package com.ecom.pipeline.mapper;

import com.ecom.pipeline.dto.ProductDto;
import com.ecom.pipeline.entity.DimProduct;
import org.springframework.stereotype.Component;

/**
 * ProductMapper — converts between DimProduct entity and ProductDto.
 *
 * Using manual mapping instead of MapStruct to avoid annotation processor
 * dependency complexity; swap to MapStruct @Mapper if needed.
 *
 * Rule: Services must NEVER expose entities directly — always use mappers.
 */
@Component
public class ProductMapper {

    public ProductDto toDto(DimProduct entity) {
        if (entity == null) return null;
        return ProductDto.builder()
                .productId(entity.getProductId())
                .name(entity.getName())
                .sku(entity.getSku())
                .category(entity.getCategory())
                .price(entity.getPrice())
                .build();
    }

    public DimProduct toEntity(ProductDto dto) {
        if (dto == null) return null;
        return DimProduct.builder()
                .productId(dto.getProductId())
                .name(dto.getName())
                .sku(dto.getSku())
                .category(dto.getCategory())
                .price(dto.getPrice())
                .build();
    }
}
