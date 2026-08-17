package com.bluemart.bluemart.service;

import com.bluemart.bluemart.dao.ProductDAO;
import com.bluemart.bluemart.dao.ProductDAOImpl;
import com.bluemart.bluemart.model.Product;

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
}
