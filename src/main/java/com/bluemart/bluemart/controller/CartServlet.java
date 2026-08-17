package com.bluemart.bluemart.controller;

import com.bluemart.bluemart.exception.ValidationException;
import com.bluemart.bluemart.model.CartItem;
import com.bluemart.bluemart.service.CartService;
import com.google.gson.Gson;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@WebServlet("/api/v1/cart")
public class CartServlet extends HttpServlet {
    private final CartService cartService = new CartService();
    private final Gson gson = new Gson();

    private Integer currentUserId(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        return session == null ? null : (Integer) session.getAttribute("userId");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        Integer userId = currentUserId(req);
        if (userId == null) {
            resp.setStatus(401);
            resp.getWriter().write(gson.toJson(new Envelope(false, null, "UNAUTHENTICATED")));
            return;
        }
        try {
            List<CartItem> items = cartService.getCart(userId);
            resp.setStatus(200);
            resp.getWriter().write(gson.toJson(new Envelope(true, items, null)));
        } catch (SQLException e) {
            resp.setStatus(500);
            resp.getWriter().write(gson.toJson(new Envelope(false, null, "SERVER_ERROR")));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        Integer userId = currentUserId(req);
        if (userId == null) {
            resp.setStatus(401);
            resp.getWriter().write(gson.toJson(new Envelope(false, null, "UNAUTHENTICATED")));
            return;
        }
        CartRequest body = gson.fromJson(req.getReader(), CartRequest.class);
        try {
            cartService.addToCart(userId, body.productId(), body.quantity());
            resp.setStatus(201);
            resp.getWriter().write(gson.toJson(new Envelope(true, Map.of("added", true), null)));
        } catch (ValidationException e) {
            resp.setStatus(400);
            resp.getWriter().write(gson.toJson(new Envelope(false, null, e.getMessage())));
        } catch (SQLException e) {
            resp.setStatus(500);
            resp.getWriter().write(gson.toJson(new Envelope(false, null, "SERVER_ERROR")));
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        Integer userId = currentUserId(req);
        if (userId == null) {
            resp.setStatus(401);
            resp.getWriter().write(gson.toJson(new Envelope(false, null, "UNAUTHENTICATED")));
            return;
        }
        CartRequest body = gson.fromJson(req.getReader(), CartRequest.class);
        try {
            cartService.updateQuantity(userId, body.productId(), body.quantity());
            resp.setStatus(200);
            resp.getWriter().write(gson.toJson(new Envelope(true, Map.of("updated", true), null)));
        } catch (SQLException e) {
            resp.setStatus(500);
            resp.getWriter().write(gson.toJson(new Envelope(false, null, "SERVER_ERROR")));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        Integer userId = currentUserId(req);
        if (userId == null) {
            resp.setStatus(401);
            resp.getWriter().write(gson.toJson(new Envelope(false, null, "UNAUTHENTICATED")));
            return;
        }
        String productIdParam = req.getParameter("productId");
        try {
            cartService.removeItem(userId, Integer.parseInt(productIdParam));
            resp.setStatus(200);
            resp.getWriter().write(gson.toJson(new Envelope(true, Map.of("removed", true), null)));
        } catch (SQLException e) {
            resp.setStatus(500);
            resp.getWriter().write(gson.toJson(new Envelope(false, null, "SERVER_ERROR")));
        }
    }

    private record CartRequest(int productId, int quantity) {}
    private record Envelope(boolean success, Object data, String error) {}
}
