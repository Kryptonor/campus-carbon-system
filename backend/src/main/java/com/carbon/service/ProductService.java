package com.carbon.service;

import com.carbon.dao.ProductRepository;
import com.carbon.dto.CreateProductRequest;
import com.carbon.entity.Product;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {
    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Product create(CreateProductRequest request) {
        Product product = new Product();
        product.setName(request.name());
        product.setDescription(request.description());
        product.setPricePoints(request.pricePoints());
        product.setStock(request.stock());
        product.setImageUrl(request.imageUrl());
        return productRepository.save(product);
    }

    public Product getById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("product not found"));
    }

    public Product update(Long id, CreateProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("product not found"));
        product.setName(request.name());
        product.setDescription(request.description());
        product.setPricePoints(request.pricePoints());
        product.setStock(request.stock());
        if (request.imageUrl() != null) {
            product.setImageUrl(request.imageUrl());
        }
        return productRepository.save(product);
    }

    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new IllegalArgumentException("product not found");
        }
        productRepository.deleteById(id);
    }

    public List<Product> list() {
        return productRepository.findAll();
    }
}
