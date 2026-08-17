package com.bluemart.bluemart.controller;

import com.bluemart.bluemart.exception.ValidationException;
import com.bluemart.bluemart.model.Order;
import com.bluemart.bluemart.service.OrderService;
import com.google.gson.Gson;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@WebServlet("/api/v1/orders")
public class OrderServlet extends HttpServlet {
    private final OrderService orderService = new OrderService();
    private final Gson gson = new Gson();

    private Integer currentUserId(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        return session == null ? null : (Integer) session.getAttribute("userId");
    }

    // POST /api/v1/orders -> place order from current cart (mock payment confirmation)
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
            int orderId = orderService.placeOrder(userId);
            resp.setStatus(201);
            resp.getWriter().write(gson.toJson(new Envelope(true, Map.of("orderId", orderId), null)));
        } catch (ValidationException e) {
            resp.setStatus(400);
            resp.getWriter().write(gson.toJson(new Envelope(false, null, e.getMessage())));
        } catch (SQLException e) {
            resp.setStatus(500);
            resp.getWriter().write(gson.toJson(new Envelope(false, null, "SERVER_ERROR")));
        }
    }

    // GET /api/v1/orders -> buyer's order history (role read from session)
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        Integer userId = currentUserId(req);
        if (userId == null) {
            resp.setStatus(401);
            resp.getWriter().write(gson.toJson(new Envelope(false, null, "UNAUTHENTICATED")));
            return;
        }
        String view = req.getParameter("view"); // "seller" or default buyer
        try {
            List<Order> orders = "seller".equals(view)
                    ? orderService.getSellerOrders(userId)
                    : orderService.getBuyerOrders(userId);
            resp.setStatus(200);
            resp.getWriter().write(gson.toJson(new Envelope(true, orders, null)));
        } catch (SQLException e) {
            resp.setStatus(500);
            resp.getWriter().write(gson.toJson(new Envelope(false, null, "SERVER_ERROR")));
        }
    }

    private record Envelope(boolean success, Object data, String error) {}
}
