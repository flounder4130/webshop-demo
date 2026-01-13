package com.example.webshop.controller;

import com.example.webshop.model.Invoice;
import com.example.webshop.service.OrderService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/diagnostics")
public class DiagnosticsController {

    private final OrderService orderService;

    public DiagnosticsController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/create-order")
    public ResponseEntity<Map<String, Object>> createOrder(
            @RequestParam(defaultValue = "TestCustomer") String customerName,
            @RequestParam(defaultValue = "99.99") BigDecimal amount) {

        Invoice invoice = orderService.checkout(customerName, amount);

        Map<String, Object> response = new HashMap<>();
        response.put("invoiceNumber", invoice.getInvoiceNumber());
        response.put("orderId", invoice.getOrder().getId());
        return ResponseEntity.ok(response);
    }
}
