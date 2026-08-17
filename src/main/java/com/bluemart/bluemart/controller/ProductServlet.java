package com.bluemart.bluemart.controller;

import com.bluemart.bluemart.model.Product;
import com.bluemart.bluemart.service.ProductService;
import com.google.gson.Gson;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

@WebServlet("/api/v1/products")
public class ProductServlet extends HttpServlet {
    private final ProductService productService = new ProductService();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        String keyword = req.getParameter("q");
        String category = req.getParameter("category");
        try {
            List<Product> results = productService.browse(keyword, category);
            resp.setStatus(200);
            resp.getWriter().write(gson.toJson(new Envelope(true, results, null)));
        } catch (SQLException e) {
            resp.setStatus(500);
            resp.getWriter().write(gson.toJson(new Envelope(false, null, "SERVER_ERROR")));
        }
    }

    private record Envelope(boolean success, Object data, String error) {}
}
