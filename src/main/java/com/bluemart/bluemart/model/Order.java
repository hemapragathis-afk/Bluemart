package com.bluemart.bluemart.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class Order {
    private int id;
    private int buyerId;
    private String status; // PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
    private List<OrderItem> items;

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getBuyerId() { return buyerId; }
    public void setBuyerId(int buyerId) { this.buyerId = buyerId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }
}
