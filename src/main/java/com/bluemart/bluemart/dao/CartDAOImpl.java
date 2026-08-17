package com.bluemart.bluemart.dao;

import com.bluemart.bluemart.listener.DataSourceListener;
import com.bluemart.bluemart.model.CartItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CartDAOImpl implements CartDAO {

    @Override
    public List<CartItem> findByUser(int userId) throws SQLException {
        String sql = "SELECT ci.id, ci.user_id, ci.product_id, ci.quantity, " +
                "p.name AS product_name, p.price AS unit_price " +
                "FROM cart_items ci JOIN products p ON ci.product_id = p.id " +
                "WHERE ci.user_id = ?";
        try (Connection c = DataSourceListener.getDataSource().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            try (ResultSet rs = ps.executeQuery()) {
                List<CartItem> items = new ArrayList<>();
                while (rs.next()) {
                    CartItem ci = new CartItem();
                    ci.setId(rs.getInt("id"));
                    ci.setUserId(rs.getInt("user_id"));
                    ci.setProductId(rs.getInt("product_id"));
                    ci.setQuantity(rs.getInt("quantity"));
                    ci.setProductName(rs.getString("product_name"));
                    ci.setUnitPrice(rs.getBigDecimal("unit_price"));
                    items.add(ci);
                }
                return items;
            }
        }
    }

    @Override
    public void addOrUpdate(int userId, int productId, int quantity) throws SQLException {
        String find = "SELECT id, quantity FROM cart_items WHERE user_id=? AND product_id=?";
        try (Connection c = DataSourceListener.getDataSource().getConnection();
             PreparedStatement ps = c.prepareStatement(find)) {
            ps.setInt(1, userId);
            ps.setInt(2, productId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int newQty = rs.getInt("quantity") + quantity;
                    updateQuantity(userId, productId, newQty);
                    return;
                }
            }
        }
        String insert = "INSERT INTO cart_items (user_id, product_id, quantity) VALUES (?, ?, ?)";
        try (Connection c = DataSourceListener.getDataSource().getConnection();
             PreparedStatement ps = c.prepareStatement(insert)) {
            ps.setInt(1, userId);
            ps.setInt(2, productId);
            ps.setInt(3, quantity);
            ps.executeUpdate();
        }
    }

    @Override
    public void updateQuantity(int userId, int productId, int quantity) throws SQLException {
        String sql = "UPDATE cart_items SET quantity=? WHERE user_id=? AND product_id=?";
        try (Connection c = DataSourceListener.getDataSource().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setInt(2, userId);
            ps.setInt(3, productId);
            ps.executeUpdate();
        }
    }

    @Override
    public void remove(int userId, int productId) throws SQLException {
        String sql = "DELETE FROM cart_items WHERE user_id=? AND product_id=?";
        try (Connection c = DataSourceListener.getDataSource().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setInt(2, productId);
            ps.executeUpdate();
        }
    }

    @Override
    public void clear(int userId) throws SQLException {
        String sql = "DELETE FROM cart_items WHERE user_id=?";
        try (Connection c = DataSourceListener.getDataSource().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.executeUpdate();
        }
    }
}
