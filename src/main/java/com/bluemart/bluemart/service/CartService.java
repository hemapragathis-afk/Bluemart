package com.bluemart.bluemart.service;

import com.bluemart.bluemart.dao.CartDAO;
import com.bluemart.bluemart.dao.CartDAOImpl;
import com.bluemart.bluemart.dao.ProductDAO;
import com.bluemart.bluemart.dao.ProductDAOImpl;
import com.bluemart.bluemart.exception.ValidationException;
import com.bluemart.bluemart.model.CartItem;
import com.bluemart.bluemart.model.Product;

import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;

public class CartService {
    private final CartDAO cartDAO = new CartDAOImpl();
    private final ProductDAO productDAO = new ProductDAOImpl();

    public List<CartItem> getCart(int userId) throws SQLException {
        return cartDAO.findByUser(userId);
    }

    public void addToCart(int userId, int productId, int quantity) throws SQLException {
        if (quantity <= 0) throw new ValidationException("Quantity must be positive");
        Product p = productDAO.findById(productId);
        if (p == null) throw new ValidationException("Product not found");
        if (p.getStockQty() < quantity) throw new ValidationException("Insufficient stock");
        cartDAO.addOrUpdate(userId, productId, quantity);
    }

    public void updateQuantity(int userId, int productId, int quantity) throws SQLException {
        if (quantity <= 0) {
            cartDAO.remove(userId, productId);
            return;
        }
        cartDAO.updateQuantity(userId, productId, quantity);
    }

    public void removeItem(int userId, int productId) throws SQLException {
        cartDAO.remove(userId, productId);
    }

    public BigDecimal calculateTotal(int userId) throws SQLException {
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem ci : cartDAO.findByUser(userId)) {
            total = total.add(ci.getUnitPrice().multiply(BigDecimal.valueOf(ci.getQuantity())));
        }
        return total;
    }
}
