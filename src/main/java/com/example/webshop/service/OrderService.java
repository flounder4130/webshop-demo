package com.example.webshop.service;

import com.example.webshop.model.Invoice;
import com.example.webshop.model.Order;
import com.example.webshop.repository.OrderRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final InvoiceService invoiceService;

    public OrderService(OrderRepository orderRepository, InvoiceService invoiceService) {
        this.orderRepository = orderRepository;
        this.invoiceService = invoiceService;
    }

    public Invoice checkout(String customerName, BigDecimal amount) {
        Order order = new Order(customerName, amount);
        order = orderRepository.save(order);
        return invoiceService.createInvoice(order);
    }
}
