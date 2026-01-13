package com.example.webshop.controller;

import com.example.webshop.model.Invoice;
import com.example.webshop.repository.InvoiceRepository;
import com.example.webshop.service.CartService;
import com.example.webshop.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final CartService cartService;
    private final InvoiceRepository invoiceRepository;

    public OrderController(OrderService orderService, CartService cartService, InvoiceRepository invoiceRepository) {
        this.orderService = orderService;
        this.cartService = cartService;
        this.invoiceRepository = invoiceRepository;
    }

    @PostMapping("/checkout")
    public ResponseEntity<Map<String, Object>> checkout(@RequestParam String customerName) {
        if (cartService.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Cart is empty"));
        }

        Invoice invoice = orderService.checkout(customerName, cartService.getTotal());
        cartService.clear();

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Order placed successfully");
        response.put("invoiceNumber", invoice.getInvoiceNumber());
        response.put("orderId", invoice.getOrder().getId());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/invoices")
    public ResponseEntity<List<Invoice>> getAllInvoices() {
        return ResponseEntity.ok(invoiceRepository.findAll());
    }

    @GetMapping("/invoices/duplicates")
    public ResponseEntity<Map<String, Object>> checkDuplicates() {
        List<Object[]> duplicates = invoiceRepository.findDuplicateInvoiceNumbers();

        Map<String, Object> response = new HashMap<>();
        response.put("hasDuplicates", !duplicates.isEmpty());
        response.put("duplicateCount", duplicates.size());

        if (!duplicates.isEmpty()) {
            Map<String, Long> duplicateDetails = new HashMap<>();
            for (Object[] row : duplicates) {
                duplicateDetails.put((String) row[0], (Long) row[1]);
            }
            response.put("duplicates", duplicateDetails);
        }

        return ResponseEntity.ok(response);
    }
}
