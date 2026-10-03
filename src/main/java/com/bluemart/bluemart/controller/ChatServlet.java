package com.bluemart.bluemart.controller;

import com.bluemart.bluemart.exception.ValidationException;
import com.bluemart.bluemart.service.ChatService;
import com.bluemart.bluemart.util.GsonUtil;
import com.google.gson.Gson;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.io.IOException;
import java.util.Map;

@WebServlet("/api/chat")
public class ChatServlet extends HttpServlet {
    private final ChatService chatService = new ChatService();
    private final Gson gson = GsonUtil.getGson();

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");

        HttpSession session = req.getSession(true);
        String sessionId = session.getId();

        try {
            ChatRequest body = gson.fromJson(req.getReader(), ChatRequest.class);
            String reply = chatService.chat(sessionId, body.message());
            resp.setStatus(200);
            resp.getWriter().write(gson.toJson(Map.of("reply", reply)));
        } catch (ValidationException e) {
            resp.setStatus(400);
            resp.getWriter().write(gson.toJson(Map.of("reply", e.getMessage())));
        } catch (Exception e) {
            e.printStackTrace();
            resp.setStatus(200); // degrade gracefully, never show an error page to the widget
            resp.getWriter().write(gson.toJson(Map.of("reply", "Sorry, something went wrong. Please try again.")));
        }
    }

    private record ChatRequest(String message) {}
}