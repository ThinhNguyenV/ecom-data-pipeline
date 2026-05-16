package com.ecom.pipeline.service;

import com.ecom.pipeline.dto.ProductDto;
import com.ecom.pipeline.entity.DimProduct;
import com.ecom.pipeline.exception.ResourceNotFoundException;
import com.ecom.pipeline.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    public Page<ProductDto> findAll(String category, String keyword, Pageable pageable) {
        log.debug("Fetching products - category={}, keyword={}", category, keyword);
        return productRepository.search(category, keyword, pageable)
                .map(this::toDto);
    }

    public ProductDto findById(String productId) {
        return productRepository.findById(productId)
                .map(this::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));
    }

    public List<String> findAllCategories() {
        return productRepository.findAllCategories();
    }

    @Transactional
    public ProductDto create(ProductDto dto) {
        log.info("Creating product: {}", dto.getName());
        DimProduct entity = toEntity(dto);
        DimProduct saved = productRepository.save(entity);
        return toDto(saved);
    }

    @Transactional
    public ProductDto update(String productId, ProductDto dto) {
        DimProduct existing = productRepository.findById(productId)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));
        existing.setName(dto.getName());
        existing.setSku(dto.getSku());
        existing.setCategory(dto.getCategory());
        existing.setPrice(dto.getPrice());
        return toDto(productRepository.save(existing));
    }

    @Transactional
    public void delete(String productId) {
        if (!productRepository.existsById(productId)) {
            throw new ResourceNotFoundException("Product not found: " + productId);
        }
        productRepository.deleteById(productId);
        log.info("Deleted product: {}", productId);
    }

    // ---- Mapping ----

    public ProductDto toDto(DimProduct entity) {
        return ProductDto.builder()
                .productId(entity.getProductId())
                .name(entity.getName())
                .sku(entity.getSku())
                .category(entity.getCategory())
                .price(entity.getPrice())
                .build();
    }

    private DimProduct toEntity(ProductDto dto) {
        return DimProduct.builder()
                .productId(dto.getProductId())
                .name(dto.getName())
                .sku(dto.getSku())
                .category(dto.getCategory())
                .price(dto.getPrice())
                .build();
    }
}
