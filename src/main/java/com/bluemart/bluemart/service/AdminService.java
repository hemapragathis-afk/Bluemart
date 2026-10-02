package com.bluemart.bluemart.service;

import com.bluemart.bluemart.dao.OrderDAO;
import com.bluemart.bluemart.dao.OrderDAOImpl;
import com.bluemart.bluemart.dao.ProductDAO;
import com.bluemart.bluemart.dao.ProductDAOImpl;
import com.bluemart.bluemart.dao.UserDAO;
import com.bluemart.bluemart.dao.UserDAOImpl;
import com.bluemart.bluemart.model.Order;
import com.bluemart.bluemart.model.Product;
import com.bluemart.bluemart.model.User;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class AdminService {
    private final UserDAO userDAO = new UserDAOImpl();
    private final OrderDAO orderDAO = new OrderDAOImpl();
    private final ProductDAO productDAO = new ProductDAOImpl();

    public record UserSummary(int id, String name, String email, String role, Object createdAt) {}

    public List<UserSummary> getAllUsers() throws SQLException {
        return userDAO.findAll().stream()
                .map(u -> new UserSummary(u.getId(), u.getName(), u.getEmail(), u.getRole(), u.getCreatedAt()))
                .collect(Collectors.toList());
    }

    public List<Order> getAllOrders() throws SQLException {
        return orderDAO.findAll();
    }

    public List<Product> getAllProducts(String keyword, String category) throws SQLException {
        return productDAO.search(keyword, category);
    }

    public void removeProduct(int productId) throws SQLException {
        Product p = productDAO.findById(productId);
        if (p == null) return;
        // Admin override: delete regardless of seller_id ownership
        productDAO.delete(productId, p.getSellerId());
    }
}