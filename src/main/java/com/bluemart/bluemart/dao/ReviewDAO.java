package com.bluemart.bluemart.dao;

import com.bluemart.bluemart.model.Review;

import java.sql.SQLException;
import java.util.List;

public interface ReviewDAO {
    int create(Review r) throws SQLException;
    List<Review> findByProduct(int productId) throws SQLException;
    boolean hasUserReviewed(int productId, int userId) throws SQLException;
    boolean hasUserPurchased(int productId, int userId) throws SQLException;
}