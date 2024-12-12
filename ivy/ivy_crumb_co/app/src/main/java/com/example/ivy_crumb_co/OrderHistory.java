package com.example.ivy_crumb_co;

import java.util.List;

public class OrderHistory {
    private int orderId;
    private String orderDate;
    private String subtotal;
    private String discount;
    private String total;
    private String status;
    private List<OrderHistoryItem> items;

    public OrderHistory(int orderId, String orderDate, String subtotal, String discount, String total, String status) {
        this.orderId = orderId;
        this.orderDate = orderDate;
        this.subtotal = subtotal;
        this.discount = discount;
        this.total = total;
        this.status = status;
    }

    public int getOrderId() {
        return orderId;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public String getSubtotal() {
        return subtotal;
    }

    public String getDiscount() {
        return discount;
    }

    public String getTotal() {
        return total;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<OrderHistoryItem> getItems() {
        return items;
    }

    public void setItems(List<OrderHistoryItem> items) {
        this.items = items;
    }
}
