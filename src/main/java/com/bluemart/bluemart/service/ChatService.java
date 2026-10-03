package com.bluemart.bluemart.service;

import com.bluemart.bluemart.exception.ValidationException;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ChatService {
    private static final int MAX_MESSAGE_LENGTH = 300;
    private static final int RATE_LIMIT_PER_MINUTE = 10;

    private final ChatProvider provider = new MockChatProvider();

    // sessionId -> cache of question -> answer (repeated-question caching)
    private final Map<String, Map<String, String>> sessionCache = new ConcurrentHashMap<>();
    // sessionId -> [windowStartMillis, count] for simple per-minute rate limiting
    private final Map<String, long[]> rateLimitTracker = new ConcurrentHashMap<>();

    public String chat(String sessionId, String userMessage) {
        if (userMessage == null || userMessage.isBlank()) {
            throw new ValidationException("Message cannot be empty");
        }
        if (userMessage.length() > MAX_MESSAGE_LENGTH) {
            throw new ValidationException("Message too long (max " + MAX_MESSAGE_LENGTH + " characters)");
        }

        enforceRateLimit(sessionId);

        String normalized = userMessage.trim().toLowerCase();
        Map<String, String> cache = sessionCache.computeIfAbsent(sessionId, k -> new ConcurrentHashMap<>());
        if (cache.containsKey(normalized)) {
            return cache.get(normalized);
        }

        String reply;
        try {
            reply = provider.getReply(userMessage, "bluemart-product-domain");
        } catch (Exception e) {
            // Guardrail: on provider failure, return a static degraded response instead of an error page
            reply = "Sorry, I'm having trouble answering right now. Please try again shortly.";
        }

        cache.put(normalized, reply);
        return reply;
    }

    private void enforceRateLimit(String sessionId) {
        long now = System.currentTimeMillis();
        long[] window = rateLimitTracker.computeIfAbsent(sessionId, k -> new long[]{now, 0});

        synchronized (window) {
            if (now - window[0] > 60_000) {
                window[0] = now;
                window[1] = 0;
            }
            window[1]++;
            if (window[1] > RATE_LIMIT_PER_MINUTE) {
                throw new ValidationException("Rate limit exceeded. Please wait a moment before sending more messages.");
            }
        }
    }
}