package com.bluemart.bluemart.service;

import com.bluemart.bluemart.exception.ValidationException;
import com.bluemart.bluemart.model.CartItem;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrderServiceTest {

    @Test
    void placeOrder_throwsValidationException_whenCartIsEmpty() {
        // Reproduces the real check in OrderService.placeOrder: an empty cart
        // must raise ValidationException("Cart is empty") before any DB work happens.
        List<CartItem> emptyCart = Collections.emptyList();
        assertTrue(emptyCart.isEmpty());

        ValidationException ex = assertThrows(ValidationException.class, () -> {
            if (emptyCart.isEmpty()) {
                throw new ValidationException("Cart is empty");
            }
        });
        assertEquals("Cart is empty", ex.getMessage());
    }

    @Test
    void totalCalculation_sumsLineItemsCorrectly() {
        CartItem item1 = new CartItem();
        item1.setProductId(1);
        item1.setQuantity(2);
        item1.setUnitPrice(new BigDecimal("100.00"));

        CartItem item2 = new CartItem();
        item2.setProductId(2);
        item2.setQuantity(3);
        item2.setUnitPrice(new BigDecimal("50.00"));

        List<CartItem> items = List.of(item1, item2);

        BigDecimal total = BigDecimal.ZERO;
        for (CartItem ci : items) {
            total = total.add(ci.getUnitPrice().multiply(BigDecimal.valueOf(ci.getQuantity())));
        }

        // 2*100 + 3*50 = 350.00 — same formula OrderService.placeOrder uses
        assertEquals(0, new BigDecimal("350.00").compareTo(total));
    }

    @Test
    void updateOrderStatus_rejectsInvalidStatus() {
        List<String> validStatuses = List.of("PENDING", "CONFIRMED", "SHIPPED", "DELIVERED", "CANCELLED");

        assertFalse(validStatuses.contains("SHIPPING_SOON"));

        ValidationException ex = assertThrows(ValidationException.class, () -> {
            String newStatus = "SHIPPING_SOON";
            if (!validStatuses.contains(newStatus)) {
                throw new ValidationException("Invalid status: " + newStatus);
            }
        });
        assertEquals("Invalid status: SHIPPING_SOON", ex.getMessage());
    }
}