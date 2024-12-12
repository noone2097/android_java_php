package com.example.ivy_crumb_co;

import java.io.Serializable;

public class OrderItem implements Serializable {
    private Product product;
    private int quantity;

    public OrderItem(Product product, int quantity) {
        this.product = product;
        this.quantity = quantity;
    }

    public Product getProduct() {
        return product;
    }

    public double getDiscount() {
        double subtotal = getSubtotal();
        if (quantity >= 5) {
            return subtotal * 0.15; // 15% discount for 5 or more items
        } else if (quantity >= 3) {
            return subtotal * 0.10; // 10% discount for 3-4 items
        }
        return 0;
    }

    public int getQuantity() {
        return quantity;
    }

    public void addQuantity(int additionalQuantity) {
        this.quantity += additionalQuantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getSubtotal() {
        return product.getPrice() * quantity;
    }

    public double getTotal() {
        return getSubtotal() - getDiscount();
    }
}
