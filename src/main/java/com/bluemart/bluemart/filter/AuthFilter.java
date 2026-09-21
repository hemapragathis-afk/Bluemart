package com.bluemart.bluemart.filter;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@WebFilter(urlPatterns = {"/api/v1/cart/*", "/api/v1/orders/*"})
public class AuthFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            resp.setContentType("application/json");
            resp.setStatus(401);
            resp.getWriter().write("{\"success\":false,\"data\":null,\"error\":\"UNAUTHENTICATED\"}");
            return;
        }

        chain.doFilter(request, response);
    }
}