package com.bluemart.bluemart.service;

import com.bluemart.bluemart.dao.ProductDAO;
import com.bluemart.bluemart.dao.ProductDAOImpl;
import com.bluemart.bluemart.exception.ValidationException;
import com.bluemart.bluemart.model.Product;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class ProductService {
    private final ProductDAO productDAO = new ProductDAOImpl();

    public List<Product> browse(String keyword, String category) throws SQLException {
        return productDAO.search(keyword, category);
    }

    public Product getProduct(int id) throws SQLException {
        return productDAO.findById(id);
    }

    public List<Product> getSellerProducts(int sellerId) throws SQLException {
        return productDAO.findBySeller(sellerId);
    }

    public int createProduct(int sellerId, String name, String description,
                              BigDecimal price, int stockQty, String category, String imageUrl) throws SQLException {
        if (name == null || name.isBlank()) {
            throw new ValidationException("Product name is required");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw new ValidationException("Price must be greater than zero");
        }
        if (stockQty < 0) {
            throw new ValidationException("Stock quantity cannot be negative");
        }

        Product p = new Product();
        p.setSellerId(sellerId);
        p.setName(name);
        p.setDescription(description);
        p.setPrice(price);
        p.setStockQty(stockQty);
        p.setCategory(category);
        p.setImageUrl(imageUrl);
        return productDAO.create(p);
    }

    public void updateProduct(int id, int sellerId, String name, String description,
                               BigDecimal price, int stockQty, String category, String imageUrl) throws SQLException {
        Product p = new Product();
        p.setId(id);
        p.setSellerId(sellerId);
        p.setName(name);
        p.setDescription(description);
        p.setPrice(price);
        p.setStockQty(stockQty);
        p.setCategory(category);
        p.setImageUrl(imageUrl);
        productDAO.update(p);
    }

    public void deleteProduct(int id, int sellerId) throws SQLException {
        productDAO.delete(id, sellerId);
    }
}