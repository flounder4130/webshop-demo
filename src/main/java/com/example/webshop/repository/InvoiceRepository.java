package com.example.webshop.repository;

import com.example.webshop.model.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    @Query("SELECT MAX(CAST(SUBSTRING(i.invoiceNumber, 5) AS int)) FROM Invoice i")
    Integer findMaxInvoiceNumber();

    @Query("SELECT i.invoiceNumber, COUNT(i) FROM Invoice i GROUP BY i.invoiceNumber HAVING COUNT(i) > 1")
    List<Object[]> findDuplicateInvoiceNumbers();
}
