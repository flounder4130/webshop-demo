package com.example.webshop.service;

import com.example.webshop.model.Invoice;
import com.example.webshop.model.Order;
import com.example.webshop.repository.InvoiceRepository;
import org.springframework.stereotype.Service;

@Service
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private InvoiceNumberGenerator generator;

    public InvoiceService(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = invoiceRepository;
    }

    public Invoice createInvoice(Order order) {
        if (generator == null) {
            generator = createGenerator();
        }
        String invoiceNumber = generator.nextNumber();
        Invoice invoice = new Invoice(invoiceNumber, order);
        return invoiceRepository.save(invoice);
    }

    private InvoiceNumberGenerator createGenerator() {
        Integer maxNumber = invoiceRepository.findMaxInvoiceNumber();
        int startingNumber = (maxNumber != null) ? maxNumber : 0;
        return new InvoiceNumberGenerator(startingNumber);
    }
}




































