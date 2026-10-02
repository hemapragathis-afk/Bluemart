package com.bluemart.bluemart.controller;

import com.bluemart.bluemart.model.Order;
import com.bluemart.bluemart.model.Product;
import com.bluemart.bluemart.service.AdminService;
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

@WebServlet("/api/v1/admin/*")
public class AdminServlet extends HttpServlet {
    private final AdminService adminService = new AdminService();
    private final Gson gson = GsonUtil.getGson();

    private boolean isAdmin(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        if (session == null) return false;
        return "ADMIN".equals(session.getAttribute("role"));
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        if (!isAdmin(req)) {
            resp.setStatus(403);
            resp.getWriter().write(gson.toJson(new Envelope(false, null, "FORBIDDEN_NOT_ADMIN")));
            return;
        }

        String path = req.getPathInfo(); // "/users" or "/orders" or "/products"
        try {
            if ("/users".equals(path)) {
                var users = adminService.getAllUsers();
                resp.setStatus(200);
                resp.getWriter().write(gson.toJson(new Envelope(true, users, null)));
            } else if ("/orders".equals(path)) {
                List<Order> orders = adminService.getAllOrders();
                resp.setStatus(200);
                resp.getWriter().write(gson.toJson(new Envelope(true, orders, null)));
            } else if ("/products".equals(path)) {
                String keyword = req.getParameter("q");
                String category = req.getParameter("category");
                List<Product> products = adminService.getAllProducts(keyword, category);
                resp.setStatus(200);
                resp.getWriter().write(gson.toJson(new Envelope(true, products, null)));
            } else {
                resp.setStatus(404);
                resp.getWriter().write(gson.toJson(new Envelope(false, null, "NOT_FOUND")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            resp.setStatus(500);
            resp.getWriter().write(gson.toJson(new Envelope(false, null, "SERVER_ERROR")));
        }
    }

    // DELETE /api/v1/admin/products?id=X -> remove any listing (moderation)
    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        if (!isAdmin(req)) {
            resp.setStatus(403);
            resp.getWriter().write(gson.toJson(new Envelope(false, null, "FORBIDDEN_NOT_ADMIN")));
            return;
        }

        String path = req.getPathInfo();
        if (!"/products".equals(path)) {
            resp.setStatus(404);
            resp.getWriter().write(gson.toJson(new Envelope(false, null, "NOT_FOUND")));
            return;
        }

        String idParam = req.getParameter("id");
        if (idParam == null) {
            resp.setStatus(400);
            resp.getWriter().write(gson.toJson(new Envelope(false, null, "MISSING_ID")));
            return;
        }

        try {
            int productId = Integer.parseInt(idParam);
            adminService.removeProduct(productId);
            resp.setStatus(200);
            resp.getWriter().write(gson.toJson(new Envelope(true, Map.of("removed", true), null)));
        } catch (SQLException e) {
            e.printStackTrace();
            resp.setStatus(500);
            resp.getWriter().write(gson.toJson(new Envelope(false, null, "SERVER_ERROR")));
        }
    }

    private record Envelope(boolean success, Object data, String error) {}
}