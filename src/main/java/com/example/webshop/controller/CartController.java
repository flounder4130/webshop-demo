package com.example.webshop.controller;

import com.example.webshop.model.CartItem;
import com.example.webshop.model.Product;
import com.example.webshop.service.CartService;
import com.example.webshop.service.ProductService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;
    private final ProductService productService;

    public CartController(CartService cartService, ProductService productService) {
        this.cartService = cartService;
        this.productService = productService;
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getCart() {
        Map<String, Object> response = new HashMap<>();
        response.put("items", cartService.getItems());
        response.put("total", cartService.getTotal());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/add/{productId}")
    public ResponseEntity<Map<String, Object>> addToCart(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "1") int quantity) {
        Product product = productService.getProduct(productId);
        if (product == null) {
            return ResponseEntity.notFound().build();
        }
        cartService.addItem(product, quantity);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Added to cart");
        response.put("items", cartService.getItems());
        response.put("total", cartService.getTotal());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping
    public ResponseEntity<String> clearCart() {
        cartService.clear();
        return ResponseEntity.ok("Cart cleared");
    }
}
