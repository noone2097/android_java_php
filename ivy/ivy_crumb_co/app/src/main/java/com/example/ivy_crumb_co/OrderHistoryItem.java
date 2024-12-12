package com.example.ivy_crumb_co;

public class OrderHistoryItem {
    private String itemName;
    private int quantity;
    private double price;
    private double subtotal;

    public OrderHistoryItem(String itemName, int quantity, double price, double subtotal) {
        this.itemName = itemName;
        this.quantity = quantity;
        this.price = price;
        this.subtotal = subtotal;
    }

    public String getItemName() {
        return itemName;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getPrice() {
        return price;
    }

    public double getSubtotal() {
        return subtotal;
    }
}
