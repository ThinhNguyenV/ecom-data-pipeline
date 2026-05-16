package com.ecom.pipeline.controller;

import com.ecom.pipeline.dto.ApiResponse;
import com.ecom.pipeline.dto.ProductDto;
import com.ecom.pipeline.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "Product catalog management")
public class ProductController {

    private final ProductService productService;

    @GetMapping
    @Operation(summary = "List products", description = "Paginated product list with optional category/keyword filter")
    public ResponseEntity<ApiResponse<Page<ProductDto>>> list(
            @Parameter(description = "Filter by category") @RequestParam(required = false) String category,
            @Parameter(description = "Keyword search in product name") @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "name") String sortBy
    ) {
        Page<ProductDto> result = productService.findAll(
                category, keyword,
                PageRequest.of(page, size, Sort.by(sortBy))
        );
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/{productId}")
    @Operation(summary = "Get product by ID")
    public ResponseEntity<ApiResponse<ProductDto>> get(@PathVariable String productId) {
        return ResponseEntity.ok(ApiResponse.ok(productService.findById(productId)));
    }

    @GetMapping("/categories")
    @Operation(summary = "List all product categories")
    public ResponseEntity<ApiResponse<List<String>>> categories() {
        return ResponseEntity.ok(ApiResponse.ok(productService.findAllCategories()));
    }

    @PostMapping
    @Operation(summary = "Create a new product")
    public ResponseEntity<ApiResponse<ProductDto>> create(@Valid @RequestBody ProductDto dto) {
        ProductDto created = productService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Product created", created));
    }

    @PutMapping("/{productId}")
    @Operation(summary = "Update a product")
    public ResponseEntity<ApiResponse<ProductDto>> update(
            @PathVariable String productId,
            @Valid @RequestBody ProductDto dto
    ) {
        return ResponseEntity.ok(ApiResponse.ok("Product updated", productService.update(productId, dto)));
    }

    @DeleteMapping("/{productId}")
    @Operation(summary = "Delete a product")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String productId) {
        productService.delete(productId);
        return ResponseEntity.ok(ApiResponse.ok(null));
    }
}
