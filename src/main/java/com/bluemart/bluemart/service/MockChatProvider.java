package com.bluemart.bluemart.service;

public class MockChatProvider implements ChatProvider {

    @Override
    public String getReply(String userMessage, String context) {
        String msg = userMessage == null ? "" : userMessage.toLowerCase();

        if (containsAny(msg, "track", "order status", "where is my order")) {
            return "You can check your order status anytime on the 'Order History' page after logging in. Orders move through PENDING, CONFIRMED, SHIPPED, and DELIVERED.";
        }
        if (containsAny(msg, "return", "refund", "cancel")) {
            return "Currently, BlueMart supports order status tracking, but automated returns/refunds aren't available yet in this version. Please contact the seller directly for return requests.";
        }
        if (containsAny(msg, "payment", "pay", "checkout")) {
            return "BlueMart uses a mock payment confirmation at checkout for this project demo — no real payment is processed.";
        }
        if (containsAny(msg, "shipping", "delivery time", "how long")) {
            return "Delivery timelines depend on the seller. Once a seller marks your order as SHIPPED, you'll see the updated status on your Order History page.";
        }
        if (containsAny(msg, "seller", "sell", "become a seller", "list a product")) {
            return "To sell on BlueMart, register with the 'Seller' role, then use your seller dashboard to create product listings.";
        }
        if (containsAny(msg, "review", "rating", "rate a product")) {
            return "You can leave a review and star rating on any product once your order for it has been marked DELIVERED.";
        }
        if (containsAny(msg, "cart", "add to cart")) {
            return "You can add products to your cart from the product listing or detail page, then adjust quantities or checkout from the Cart page.";
        }
        if (containsAny(msg, "account", "register", "sign up", "login", "password")) {
            return "You can register as a Buyer or Seller from the Register page. If you're having trouble logging in, double-check your email and password are correct.";
        }
        if (containsAny(msg, "hello", "hi", "hey")) {
            return "Hi! I'm BlueMart's assistant. Ask me about orders, products, cart, reviews, or your account.";
        }
        if (containsAny(msg, "thank")) {
            return "You're welcome! Let me know if there's anything else I can help with.";
        }

        return "I'm not sure about that one yet, but I can help with questions about orders, products, cart, checkout, reviews, or your account.";
    }

    private boolean containsAny(String text, String... keywords) {
        for (String k : keywords) {
            if (text.contains(k)) return true;
        }
        return false;
    }
}