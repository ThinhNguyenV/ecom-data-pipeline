package com.ecom.pipeline.service;

import com.ecom.pipeline.dto.ProductDto;
import com.ecom.pipeline.entity.DimProduct;
import com.ecom.pipeline.exception.ResourceNotFoundException;
import com.ecom.pipeline.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Unit Tests")
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private DimProduct sampleProduct;

    @BeforeEach
    void setUp() {
        sampleProduct = DimProduct.builder()
                .productId("P001")
                .name("Giày chạy bộ Nike")
                .sku("SKU-001")
                .category("Footwear")
                .price(BigDecimal.valueOf(1500000))
                .build();
    }

    @Test
    @DisplayName("findById - returns DTO when product exists")
    void findById_returnsDto_whenExists() {
        when(productRepository.findById("P001")).thenReturn(Optional.of(sampleProduct));

        ProductDto result = productService.findById("P001");

        assertThat(result.getProductId()).isEqualTo("P001");
        assertThat(result.getName()).isEqualTo("Giày chạy bộ Nike");
        assertThat(result.getCategory()).isEqualTo("Footwear");
    }

    @Test
    @DisplayName("findById - throws ResourceNotFoundException when not found")
    void findById_throwsNotFoundException_whenMissing() {
        when(productRepository.findById("UNKNOWN")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.findById("UNKNOWN"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("UNKNOWN");
    }

    @Test
    @DisplayName("findAll - returns paged results")
    void findAll_returnsPagedResults() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<DimProduct> mockPage = new PageImpl<>(List.of(sampleProduct));
        when(productRepository.search(any(), any(), any())).thenReturn(mockPage);

        Page<ProductDto> result = productService.findAll(null, null, pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getProductId()).isEqualTo("P001");
    }

    @Test
    @DisplayName("create - saves and returns DTO")
    void create_savesAndReturnsDto() {
        ProductDto dto = ProductDto.builder()
                .productId("P002")
                .name("Áo thun Adidas")
                .sku("SKU-002")
                .category("Apparel")
                .price(BigDecimal.valueOf(450000))
                .build();

        DimProduct saved = DimProduct.builder()
                .productId("P002").name("Áo thun Adidas")
                .sku("SKU-002").category("Apparel")
                .price(BigDecimal.valueOf(450000))
                .build();

        when(productRepository.save(any())).thenReturn(saved);

        ProductDto result = productService.create(dto);

        assertThat(result.getProductId()).isEqualTo("P002");
        verify(productRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("delete - throws exception when product does not exist")
    void delete_throwsException_whenNotFound() {
        when(productRepository.existsById("MISSING")).thenReturn(false);

        assertThatThrownBy(() -> productService.delete("MISSING"))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(productRepository, never()).deleteById(any());
    }

    @Test
    @DisplayName("findAllCategories - delegates to repository")
    void findAllCategories_delegatesToRepository() {
        when(productRepository.findAllCategories()).thenReturn(List.of("Footwear", "Apparel"));

        List<String> categories = productService.findAllCategories();

        assertThat(categories).containsExactly("Footwear", "Apparel");
    }
}
