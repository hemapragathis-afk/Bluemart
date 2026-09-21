package com.bluemart.bluemart.controller;

import com.bluemart.bluemart.exception.ValidationException;
import com.bluemart.bluemart.model.Product;
import com.bluemart.bluemart.service.ProductService;
import com.bluemart.bluemart.util.GsonUtil;
import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@WebServlet("/api/v1/products")
public class ProductServlet extends HttpServlet {
    private final ProductService productService = new ProductService();
    private final Gson gson = GsonUtil.getGson();

    private Integer currentUserId(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        return session == null ? null : (Integer) session.getAttribute("userId");
    }

    private String currentRole(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        return session == null ? null : (String) session.getAttribute("role");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        String keyword = req.getParameter("q");
        String category = req.getParameter("category");
        String mine = req.getParameter("mine"); // ?mine=true -> seller's own listings

        try {
            List<Product> results;
            if ("true".equals(mine)) {
                Integer userId = currentUserId(req);
                if (userId == null) {
                    resp.setStatus(401);
                    resp.getWriter().write(gson.toJson(new Envelope(false, null, "UNAUTHENTICATED")));
                    return;
                }
                results = productService.getSellerProducts(userId);
            } else {
                results = productService.browse(keyword, category);
            }
            resp.setStatus(200);
            resp.getWriter().write(gson.toJson(new Envelope(true, results, null)));
        } catch (SQLException e) {
            e.printStackTrace();
            resp.setStatus(500);
            resp.getWriter().write(gson.toJson(new Envelope(false, null, "SERVER_ERROR")));
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        Integer userId = currentUserId(req);
        String role = currentRole(req);
        if (userId == null) {
            resp.setStatus(401);
            resp.getWriter().write(gson.toJson(new Envelope(false, null, "UNAUTHENTICATED")));
            return;
        }
        if (!"SELLER".equals(role)) {
            resp.setStatus(403);
            resp.getWriter().write(gson.toJson(new Envelope(false, null, "FORBIDDEN_NOT_A_SELLER")));
            return;
        }

        try {
            ProductRequest body = gson.fromJson(req.getReader(), ProductRequest.class);
            int productId = productService.createProduct(
                    userId, body.name(), body.description(),
                    body.price() != null ? BigDecimal.valueOf(body.price()) : null,
                    body.stockQty(), body.category(), body.imageUrl());
            resp.setStatus(201);
            resp.getWriter().write(gson.toJson(new Envelope(true, Map.of("productId", productId), null)));
        } catch (ValidationException e) {
            resp.setStatus(400);
            resp.getWriter().write(gson.toJson(new Envelope(false, null, e.getMessage())));
        } catch (SQLException e) {
            e.printStackTrace();
            resp.setStatus(500);
            resp.getWriter().write(gson.toJson(new Envelope(false, null, "SERVER_ERROR")));
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        Integer userId = currentUserId(req);
        String role = currentRole(req);
        if (userId == null) {
            resp.setStatus(401);
            resp.getWriter().write(gson.toJson(new Envelope(false, null, "UNAUTHENTICATED")));
            return;
        }
        if (!"SELLER".equals(role)) {
            resp.setStatus(403);
            resp.getWriter().write(gson.toJson(new Envelope(false, null, "FORBIDDEN_NOT_A_SELLER")));
            return;
        }

        String idParam = req.getParameter("id");
        if (idParam == null) {
            resp.setStatus(400);
            resp.getWriter().write(gson.toJson(new Envelope(false, null, "MISSING_ID")));
            return;
        }

        try {
            int id = Integer.parseInt(idParam);
            ProductRequest body = gson.fromJson(req.getReader(), ProductRequest.class);
            productService.updateProduct(
                    id, userId, body.name(), body.description(),
                    body.price() != null ? BigDecimal.valueOf(body.price()) : null,
                    body.stockQty(), body.category(), body.imageUrl());
            resp.setStatus(200);
            resp.getWriter().write(gson.toJson(new Envelope(true, Map.of("updated", true), null)));
        } catch (SQLException e) {
            e.printStackTrace();
            resp.setStatus(500);
            resp.getWriter().write(gson.toJson(new Envelope(false, null, "SERVER_ERROR")));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        Integer userId = currentUserId(req);
        String role = currentRole(req);
        if (userId == null) {
            resp.setStatus(401);
            resp.getWriter().write(gson.toJson(new Envelope(false, null, "UNAUTHENTICATED")));
            return;
        }
        if (!"SELLER".equals(role)) {
            resp.setStatus(403);
            resp.getWriter().write(gson.toJson(new Envelope(false, null, "FORBIDDEN_NOT_A_SELLER")));
            return;
        }

        String idParam = req.getParameter("id");
        if (idParam == null) {
            resp.setStatus(400);
            resp.getWriter().write(gson.toJson(new Envelope(false, null, "MISSING_ID")));
            return;
        }

        try {
            int id = Integer.parseInt(idParam);
            productService.deleteProduct(id, userId);
            resp.setStatus(200);
            resp.getWriter().write(gson.toJson(new Envelope(true, Map.of("deleted", true), null)));
        } catch (SQLException e) {
            e.printStackTrace();
            resp.setStatus(500);
            resp.getWriter().write(gson.toJson(new Envelope(false, null, "SERVER_ERROR")));
        }
    }

    private record ProductRequest(String name, String description, Double price,
                                   int stockQty, String category, String imageUrl) {}
    private record Envelope(boolean success, Object data, String error) {}
}