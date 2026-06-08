package com.carbon.controller;

import com.carbon.dto.CreateProductRequest;
import com.carbon.entity.Product;
import com.carbon.service.ProductService;
import jakarta.validation.Valid;
import com.carbon.dto.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping
    public ApiResponse<Product> create(@Valid @RequestBody CreateProductRequest request) {
        return ApiResponse.ok(productService.create(request));
    }

    @GetMapping("/{id}")
    public ApiResponse<Product> get(@PathVariable Long id) {
        return ApiResponse.ok(productService.getById(id));
    }

    @GetMapping
    public ApiResponse<List<Product>> list() {
        return ApiResponse.ok(productService.list());
    }
}
