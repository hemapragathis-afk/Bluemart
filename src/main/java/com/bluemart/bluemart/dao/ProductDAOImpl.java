package com.bluemart.bluemart.dao;

import com.bluemart.bluemart.listener.DataSourceListener;
import com.bluemart.bluemart.model.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ProductDAOImpl implements ProductDAO {

    @Override
    public Product findById(int id) throws SQLException {
        String sql = "SELECT * FROM products WHERE id = ?";
        try (Connection c = DataSourceListener.getDataSource().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? map(rs) : null;
            }
        }
    }

    @Override
    public List<Product> search(String keyword, String category) throws SQLException {
        StringBuilder sql = new StringBuilder("SELECT * FROM products WHERE 1=1");
        List<Object> params = new ArrayList<>();

        if (keyword != null && !keyword.isBlank()) {
            sql.append(" AND LOWER(name) LIKE ?");
            params.add("%" + keyword.toLowerCase() + "%");
        }
        if (category != null && !category.isBlank()) {
            sql.append(" AND category = ?");
            params.add(category);
        }
        sql.append(" ORDER BY created_at DESC");

        try (Connection c = DataSourceListener.getDataSource().getConnection();
             PreparedStatement ps = c.prepareStatement(sql.toString())) {
            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            try (ResultSet rs = ps.executeQuery()) {
                List<Product> out = new ArrayList<>();
                while (rs.next()) out.add(map(rs));
                return out;
            }
        }
    }

    @Override
    public List<Product> findBySeller(int sellerId) throws SQLException {
        String sql = "SELECT * FROM products WHERE seller_id = ? ORDER BY created_at DESC";
        try (Connection c = DataSourceListener.getDataSource().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, sellerId);
            try (ResultSet rs = ps.executeQuery()) {
                List<Product> out = new ArrayList<>();
                while (rs.next()) out.add(map(rs));
                return out;
            }
        }
    }

    @Override
    public int create(Product p) throws SQLException {
        String sql = "INSERT INTO products (seller_id, name, description, price, stock_qty, category, image_url, created_at) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
        try (Connection c = DataSourceListener.getDataSource().getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, p.getSellerId());
            ps.setString(2, p.getName());
            ps.setString(3, p.getDescription());
            ps.setBigDecimal(4, p.getPrice());
            ps.setInt(5, p.getStockQty());
            ps.setString(6, p.getCategory());
            ps.setString(7, p.getImageUrl());
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        throw new SQLException("Insert failed, no ID returned");
    }

    @Override
    public void update(Product p) throws SQLException {
        String sql = "UPDATE products SET name=?, description=?, price=?, stock_qty=?, category=?, image_url=? " +
                "WHERE id=? AND seller_id=?";
        try (Connection c = DataSourceListener.getDataSource().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setString(1, p.getName());
            ps.setString(2, p.getDescription());
            ps.setBigDecimal(3, p.getPrice());
            ps.setInt(4, p.getStockQty());
            ps.setString(5, p.getCategory());
            ps.setString(6, p.getImageUrl());
            ps.setInt(7, p.getId());
            ps.setInt(8, p.getSellerId());
            ps.executeUpdate();
        }
    }

    @Override
    public void delete(int id, int sellerId) throws SQLException {
        String sql = "DELETE FROM products WHERE id=? AND seller_id=?";
        try (Connection c = DataSourceListener.getDataSource().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.setInt(2, sellerId);
            ps.executeUpdate();
        }
    }

    @Override
    public void decrementStock(int productId, int quantity) throws SQLException {
        String sql = "UPDATE products SET stock_qty = stock_qty - ? WHERE id = ? AND stock_qty >= ?";
        try (Connection c = DataSourceListener.getDataSource().getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, quantity);
            ps.setInt(2, productId);
            ps.setInt(3, quantity);
            int rows = ps.executeUpdate();
            if (rows == 0) throw new SQLException("Insufficient stock for product " + productId);
        }
    }

    private Product map(ResultSet rs) throws SQLException {
        Product p = new Product();
        p.setId(rs.getInt("id"));
        p.setSellerId(rs.getInt("seller_id"));
        p.setName(rs.getString("name"));
        p.setDescription(rs.getString("description"));
        p.setPrice(rs.getBigDecimal("price"));
        p.setStockQty(rs.getInt("stock_qty"));
        p.setCategory(rs.getString("category"));
        p.setImageUrl(rs.getString("image_url"));
        p.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        return p;
    }
}
