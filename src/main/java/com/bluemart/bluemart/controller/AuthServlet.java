package com.bluemart.bluemart.controller;

import com.bluemart.bluemart.exception.ValidationException;
import com.bluemart.bluemart.model.User;
import com.bluemart.bluemart.service.AuthService;
import com.google.gson.Gson;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;

@WebServlet("/api/v1/auth/*")
public class AuthServlet extends HttpServlet {
    private final AuthService authService = new AuthService();
    private final Gson gson = new Gson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        String path = req.getPathInfo(); // "/register" or "/login"

        if ("/register".equals(path)) {
            handleRegister(req, resp);
        } else if ("/login".equals(path)) {
            handleLogin(req, resp);
        } else {
            resp.setStatus(404);
            resp.getWriter().write(gson.toJson(new Envelope(false, null, "NOT_FOUND")));
        }
    }

    private void handleRegister(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            RegisterRequest body = gson.fromJson(req.getReader(), RegisterRequest.class);
            int userId = authService.register(body.name(), body.email(), body.password(), body.role());
            resp.setStatus(201);
            resp.getWriter().write(gson.toJson(new Envelope(true, Map.of("userId", userId), null)));
        } catch (ValidationException e) {
            resp.setStatus(400);
            resp.getWriter().write(gson.toJson(new Envelope(false, null, e.getMessage())));
        } catch (SQLException e) {
            resp.setStatus(500);
            resp.getWriter().write(gson.toJson(new Envelope(false, null, "SERVER_ERROR")));
        }
    }

    private void handleLogin(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            LoginRequest body = gson.fromJson(req.getReader(), LoginRequest.class);
            User user = authService.login(body.email(), body.password());

            // Regenerate session ID on login per spec's engineering rules
            HttpSession oldSession = req.getSession(false);
            if (oldSession != null) oldSession.invalidate();
            HttpSession session = req.getSession(true);
            session.setAttribute("userId", user.getId());
            session.setAttribute("role", user.getRole());
            session.setMaxInactiveInterval(30 * 60); // 30 min timeout

            resp.setStatus(200);
            resp.getWriter().write(gson.toJson(new Envelope(true,
                    Map.of("userId", user.getId(), "name", user.getName(), "role", user.getRole()), null)));
        } catch (ValidationException e) {
            resp.setStatus(401);
            resp.getWriter().write(gson.toJson(new Envelope(false, null, e.getMessage())));
        } catch (SQLException e) {
            resp.setStatus(500);
            resp.getWriter().write(gson.toJson(new Envelope(false, null, "SERVER_ERROR")));
        }
    }

    private record RegisterRequest(String name, String email, String password, String role) {}
    private record LoginRequest(String email, String password) {}
    private record Envelope(boolean success, Object data, String error) {}
}