package com.bluemart.bluemart.dao;

import com.bluemart.bluemart.model.CartItem;
import java.sql.SQLException;
import java.util.List;

public interface CartDAO {
    List<CartItem> findByUser(int userId) throws SQLException;
    void addOrUpdate(int userId, int productId, int quantity) throws SQLException;
    void updateQuantity(int userId, int productId, int quantity) throws SQLException;
    void remove(int userId, int productId) throws SQLException;
    void clear(int userId) throws SQLException;
}
