package com.bluemart.bluemart.dao;

import com.bluemart.bluemart.listener.DataSourceListener;
import com.bluemart.bluemart.model.Order;
import com.bluemart.bluemart.model.OrderItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class OrderDAOImpl implements OrderDAO {

    @Override
    public int createOrder(Connection c, int buyerId, java.math.BigDecimal total) throws SQLException {
        String sql = "INSERT INTO orders (buyer_id, status, total_amount, created_at) " +
                "VALUES (?, 'PENDING', ?, CURRENT_TIMESTAMP)";
        try (PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, buyerId);
            ps.setBigDecimal(2, total);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        throw new SQLException("Order insert failed");
    }

    @Override
    public void addOrderItem(Connection c, int orderId, int productId, int quantity, java.math.BigDecimal unitPrice) throws SQLException {
        String sql = "INSERT INTO order_items (order_id, product_id, quantity, unit_price) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            ps.setInt(2, productId);
            ps.setInt(3, quantity);
            ps.setBigDecimal(4, unitPrice);
            ps.executeUpdate();
        }
    }

    @Override
    public List<Order> findByBuyer(int buyerId) throws SQLException {
        String sql = "SELECT * FROM orders WHERE buyer_id=? ORDER BY created_at DESC";
        try (Connection c = DataSourceListener.getDataSource().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, buyerId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Order> out = new ArrayList<>();
                while (rs.next()) out.add(mapOrder(rs));
                return out;
            }
        }
    }

    @Override
    public List<Order> findBySeller(int sellerId) throws SQLException {
        String sql = "SELECT DISTINCT o.* FROM orders o " +
                "JOIN order_items oi ON oi.order_id = o.id " +
                "JOIN products p ON oi.product_id = p.id " +
                "WHERE p.seller_id = ? ORDER BY o.created_at DESC";
        try (Connection c = DataSourceListener.getDataSource().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, sellerId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Order> out = new ArrayList<>();
                while (rs.next()) out.add(mapOrder(rs));
                return out;
            }
        }
    }

    @Override
    public Order findById(int orderId) throws SQLException {
        String sql = "SELECT * FROM orders WHERE id=?";
        Order order;
        try (Connection c = DataSourceListener.getDataSource().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                order = mapOrder(rs);
            }
        }
        String itemSql = "SELECT oi.*, p.name AS product_name FROM order_items oi " +
                "JOIN products p ON oi.product_id = p.id WHERE oi.order_id=?";
        try (Connection c = DataSourceListener.getDataSource().getConnection();
             PreparedStatement ps = c.prepareStatement(itemSql)) {
            ps.setInt(1, orderId);
            try (ResultSet rs = ps.executeQuery()) {
                List<OrderItem> items = new ArrayList<>();
                while (rs.next()) {
                    OrderItem oi = new OrderItem();
                    oi.setId(rs.getInt("id"));
                    oi.setOrderId(rs.getInt("order_id"));
                    oi.setProductId(rs.getInt("product_id"));
                    oi.setQuantity(rs.getInt("quantity"));
                    oi.setUnitPrice(rs.getBigDecimal("unit_price"));
                    oi.setProductName(rs.getString("product_name"));
                    items.add(oi);
                }
                order.setItems(items);
            }
        }
        return order;
    }

    private Order mapOrder(ResultSet rs) throws SQLException {
        Order o = new Order();
        o.setId(rs.getInt("id"));
        o.setBuyerId(rs.getInt("buyer_id"));
        o.setStatus(rs.getString("status"));
        o.setTotalAmount(rs.getBigDecimal("total_amount"));
        o.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return o;
    }
}
