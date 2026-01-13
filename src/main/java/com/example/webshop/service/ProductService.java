package com.example.webshop.service;

import com.example.webshop.model.Product;
import com.example.webshop.repository.ProductRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @PostConstruct
    public void initSampleProducts() {
        if (productRepository.count() == 0) {
            productRepository.save(new Product("Laptop", "High-performance laptop", new BigDecimal("999.99"), 50));
            productRepository.save(new Product("Headphones", "Wireless noise-canceling headphones", new BigDecimal("249.99"), 100));
            productRepository.save(new Product("Keyboard", "Mechanical gaming keyboard", new BigDecimal("129.99"), 75));
            productRepository.save(new Product("Mouse", "Ergonomic wireless mouse", new BigDecimal("59.99"), 150));
            productRepository.save(new Product("Monitor", "27-inch 4K display", new BigDecimal("449.99"), 30));
        }
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProduct(Long id) {
        return productRepository.findById(id).orElse(null);
    }
}
