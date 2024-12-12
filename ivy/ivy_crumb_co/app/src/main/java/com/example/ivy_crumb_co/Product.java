package com.example.ivy_crumb_co;

import java.io.Serializable;

public class Product implements Serializable {
    private String name;
    private double price;
    private int imageResourceId;
    private int quantity;

    public Product(String name, double price, int imageResourceId) {
        this.name = name;
        this.price = price;
        this.imageResourceId = imageResourceId;
        this.quantity = 0;
    }

    public String getName() {
        return name;
    }

    public double getPrice() {
        return price;
    }

    public int getImageResourceId() {
        return imageResourceId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    // Calculation methods
    public double getSubtotal() {
        return price * quantity;
    }

    public double getDiscount() {
        return getDiscount(quantity);
    }

    public double getDiscount(int quantity) {
        if (name.toLowerCase().contains("cake")) {
            // For cakes: 10% discount when buying 2 or more
            if (quantity >= 2) {
                return price * quantity * 0.10; // 10% discount
            }
        } else if (name.toLowerCase().contains("bread")) {
            // For breads: 15% discount when buying 5 or more
            if (quantity >= 5) {
                return price * quantity * 0.15; // 15% discount
            }
        }
        return 0.0;
    }

    public boolean hasDiscount() {
        return hasDiscount(quantity);
    }

    public boolean hasDiscount(int quantity) {
        if (name.toLowerCase().contains("cake")) {
            return quantity >= 2;
        } else if (name.toLowerCase().contains("bread")) {
            return quantity >= 5;
        }
        return false;
    }

    public String getDiscountMessage() {
        if (hasDiscount()) {
            if (name.toLowerCase().contains("cake")) {
                return "10% discount applied!";
            } else if (name.toLowerCase().contains("bread")) {
                return "15% discount applied!";
            }
        }
        if (name.toLowerCase().contains("cake")) {
            return quantity > 0 ? "Buy 2+ for 10% off" : "";
        } else if (name.toLowerCase().contains("bread")) {
            return quantity > 0 ? "Buy 5+ for 15% off" : "";
        }
        return quantity > 0 ? "" : "";
    }

    public double getTotal() {
        return getSubtotal() - getDiscount();
    }
}