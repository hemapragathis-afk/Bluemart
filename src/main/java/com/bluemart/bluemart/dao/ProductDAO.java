package com.bluemart.bluemart.dao;

import com.bluemart.bluemart.model.Product;
import java.sql.SQLException;
import java.util.List;

public interface ProductDAO {
    Product findById(int id) throws SQLException;
    List<Product> search(String keyword, String category) throws SQLException;
    List<Product> findBySeller(int sellerId) throws SQLException;
    int create(Product p) throws SQLException;
    void update(Product p) throws SQLException;
    void delete(int id, int sellerId) throws SQLException;
    void decrementStock(int productId, int quantity) throws SQLException;
}
