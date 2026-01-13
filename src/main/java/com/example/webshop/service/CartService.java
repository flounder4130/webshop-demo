package com.example.webshop.service;

import com.example.webshop.model.CartItem;
import com.example.webshop.model.Product;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@SessionScope
public class CartService {

    private final List<CartItem> items = new ArrayList<>();

    public void addItem(Product product, int quantity) {
        for (CartItem item : items) {
            if (item.getProductId().equals(product.getId())) {
                item.setQuantity(item.getQuantity() + quantity);
                return;
            }
        }
        items.add(new CartItem(product.getId(), product.getName(), product.getPrice(), quantity));
    }

    public List<CartItem> getItems() {
        return new ArrayList<>(items);
    }

    public BigDecimal getTotal() {
        return items.stream()
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void clear() {
        items.clear();
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }
}
