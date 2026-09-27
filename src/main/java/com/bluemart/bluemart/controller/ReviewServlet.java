package com.bluemart.bluemart.controller;

import com.bluemart.bluemart.exception.ValidationException;
import com.bluemart.bluemart.model.Review;
import com.bluemart.bluemart.service.ReviewService;
import com.bluemart.bluemart.util.GsonUtil;
import com.google.gson.Gson;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@WebServlet("/api/v1/reviews")
public class ReviewServlet extends HttpServlet {
    private final ReviewService reviewService = new ReviewService();
    private final Gson gson = GsonUtil.getGson();

    private Integer currentUserId(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        return session == null ? null : (Integer) session.getAttribute("userId");
    }

    // GET /api/v1/reviews?productId=X -> list reviews for a product (public, no auth needed)
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        String productIdParam = req.getParameter("productId");
        if (productIdParam == null) {
            resp.setStatus(400);
            resp.getWriter().write(gson.toJson(new Envelope(false, null, "MISSING_PRODUCT_ID")));
            return;
        }
        try {
            int productId = Integer.parseInt(productIdParam);
            List<Review> reviews = reviewService.getProductReviews(productId);
            resp.setStatus(200);
            resp.getWriter().write(gson.toJson(new Envelope(true, reviews, null)));
        } catch (SQLException e) {
            e.printStackTrace();
            resp.setStatus(500);
            resp.getWriter().write(gson.toJson(new Envelope(false, null, "SERVER_ERROR")));
        }
    }

    // POST /api/v1/reviews -> submit a review (buyer only, must have a DELIVERED order for this product)
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        Integer userId = currentUserId(req);
        if (userId == null) {
            resp.setStatus(401);
            resp.getWriter().write(gson.toJson(new Envelope(false, null, "UNAUTHENTICATED")));
            return;
        }

        try {
            ReviewRequest body = gson.fromJson(req.getReader(), ReviewRequest.class);
            int reviewId = reviewService.addReview(body.productId(), userId, body.rating(), body.comment());
            resp.setStatus(201);
            resp.getWriter().write(gson.toJson(new Envelope(true, Map.of("reviewId", reviewId), null)));
        } catch (ValidationException e) {
            resp.setStatus(400);
            resp.getWriter().write(gson.toJson(new Envelope(false, null, e.getMessage())));
        } catch (SQLException e) {
            e.printStackTrace();
            resp.setStatus(500);
            resp.getWriter().write(gson.toJson(new Envelope(false, null, "SERVER_ERROR")));
        }
    }

    private record ReviewRequest(int productId, int rating, String comment) {}
    private record Envelope(boolean success, Object data, String error) {}
}