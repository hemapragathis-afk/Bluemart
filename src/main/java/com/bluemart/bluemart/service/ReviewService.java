package com.bluemart.bluemart.service;

import com.bluemart.bluemart.dao.ReviewDAO;
import com.bluemart.bluemart.dao.ReviewDAOImpl;
import com.bluemart.bluemart.exception.ValidationException;
import com.bluemart.bluemart.model.Review;

import java.sql.SQLException;
import java.util.List;

public class ReviewService {
    private final ReviewDAO reviewDAO = new ReviewDAOImpl();

    public int addReview(int productId, int userId, int rating, String comment) throws SQLException {
        if (rating < 1 || rating > 5) {
            throw new ValidationException("Rating must be between 1 and 5");
        }
        if (!reviewDAO.hasUserPurchased(productId, userId)) {
            throw new ValidationException("You can only review products from delivered orders");
        }
        if (reviewDAO.hasUserReviewed(productId, userId)) {
            throw new ValidationException("You have already reviewed this product");
        }

        Review r = new Review();
        r.setProductId(productId);
        r.setUserId(userId);
        r.setRating(rating);
        r.setComment(comment);
        return reviewDAO.create(r);
    }

    public List<Review> getProductReviews(int productId) throws SQLException {
        return reviewDAO.findByProduct(productId);
    }
}