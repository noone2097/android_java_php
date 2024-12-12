package com.example.ivy_crumb_co;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Order implements Serializable {
    private List<OrderItem> orderItems;
    private String username; // Declare the username field

    public Order() {
        this.orderItems = new ArrayList<>();
    }

    public Order(String username) {
        this.username = username; // Now this will work
        this.orderItems = new ArrayList<>();
    }

    public void addItem(Product product, int quantity) {
        OrderItem existingItem = findOrderItem(product);
        if (existingItem != null) {
            existingItem.addQuantity(quantity);
        } else {
            orderItems.add(new OrderItem(product, quantity));
        }
    }

    public void removeItem(OrderItem item) {
        orderItems.remove(item);
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public double getSubtotal() {
        double subtotal = 0;
        for (OrderItem item : orderItems) {
            subtotal += item.getSubtotal();
        }
        return subtotal;
    }

    public double getDiscount() {
        double totalDiscount = 0;
        for (OrderItem item : orderItems) {
            totalDiscount += item.getDiscount();
        }
        return totalDiscount;
    }

    public double getTotal() {
        return getSubtotal() - getDiscount();
    }

    private OrderItem findOrderItem(Product product) {
        for (OrderItem item : orderItems) {
            if (item.getProduct().equals(product)) {
                return item;
            }
        }
        return null;
    }

    public boolean isEmpty() {
        return orderItems.isEmpty();
    }
}