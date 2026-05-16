package com.ecom.pipeline.controller;

import com.ecom.pipeline.dto.ApiResponse;
import com.ecom.pipeline.dto.CustomerDto;
import com.ecom.pipeline.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Tag(name = "Customers", description = "Customer analytics")
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping
    @Operation(summary = "List customers with optional country filter")
    public ResponseEntity<ApiResponse<Page<CustomerDto>>> list(
            @RequestParam(required = false) String country,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Page<CustomerDto> result = customerService.findAll(
                country, PageRequest.of(page, size, Sort.by("lastName"))
        );
        return ResponseEntity.ok(ApiResponse.ok(result));
    }

    @GetMapping("/{customerId}")
    @Operation(summary = "Get customer by ID")
    public ResponseEntity<ApiResponse<CustomerDto>> get(@PathVariable String customerId) {
        return ResponseEntity.ok(ApiResponse.ok(customerService.findById(customerId)));
    }
}
