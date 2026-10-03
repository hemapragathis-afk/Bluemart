package com.bluemart.bluemart.service;

public interface ChatProvider {
    String getReply(String userMessage, String context);
}