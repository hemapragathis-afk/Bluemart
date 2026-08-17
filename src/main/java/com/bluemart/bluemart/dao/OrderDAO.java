package com.bluemart.bluemart.dao;

import com.bluemart.bluemart.model.Order;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public interface OrderDAO {
    // placeOrder runs on a single Connection so the service can control the transaction
    int createOrder(Connection c, int buyerId, java.math.BigDecimal total) throws SQLException;
    void addOrderItem(Connection c, int orderId, int productId, int quantity, java.math.BigDecimal unitPrice) throws SQLException;
    List<Order> findByBuyer(int buyerId) throws SQLException;
    List<Order> findBySeller(int sellerId) throws SQLException;
    Order findById(int orderId) throws SQLException;
}
