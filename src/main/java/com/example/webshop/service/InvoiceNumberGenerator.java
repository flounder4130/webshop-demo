package com.example.webshop.service;

import java.util.concurrent.atomic.AtomicInteger;

public class InvoiceNumberGenerator {

    private final AtomicInteger counter;

    public InvoiceNumberGenerator(int startingNumber) {
        this.counter = new AtomicInteger(startingNumber);
    }

    public String nextNumber() {
        int next = counter.incrementAndGet();
        return String.format("INV-%05d", next);
    }
}
