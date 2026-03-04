package com.example.webshop.service;

import com.example.webshop.model.Invoice;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import java.math.BigDecimal;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class InvoiceServiceTest {

    @Autowired
    private OrderService orderService;

    @Test
    void firstTwoOrdersGetInvoiceNumbersOneAndTwo() {
        CompletableFuture<Invoice> alice = CompletableFuture.supplyAsync(
                () -> orderService.checkout("Alice", BigDecimal.TEN));
        CompletableFuture<Invoice> bob = CompletableFuture.supplyAsync(
                () -> orderService.checkout("Bob", BigDecimal.TEN));

        String num1 = alice.join().getInvoiceNumber();
        String num2 = bob.join().getInvoiceNumber();

        assertEquals(Set.of("INV-00001", "INV-00002"), Set.of(num1, num2));
    }
}
