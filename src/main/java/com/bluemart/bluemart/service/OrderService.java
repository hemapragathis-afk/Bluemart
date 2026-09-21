package com.bluemart.bluemart.service;

import com.bluemart.bluemart.dao.CartDAO;
import com.bluemart.bluemart.dao.CartDAOImpl;
import com.bluemart.bluemart.dao.OrderDAO;
import com.bluemart.bluemart.dao.OrderDAOImpl;
import com.bluemart.bluemart.dao.ProductDAO;
import com.bluemart.bluemart.dao.ProductDAOImpl;
import com.bluemart.bluemart.exception.ValidationException;
import com.bluemart.bluemart.listener.DataSourceListener;
import com.bluemart.bluemart.model.CartItem;
import com.bluemart.bluemart.model.Order;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class OrderService {
    private final CartDAO cartDAO = new CartDAOImpl();
    private final ProductDAO productDAO = new ProductDAOImpl();
    private final OrderDAO orderDAO = new OrderDAOImpl();

    /**
     * Places an order from the buyer's current cart contents via mock payment confirmation.
     * Runs as a single transaction: create order, insert items, decrement stock, clear cart.
     */
    public int placeOrder(int buyerId) throws SQLException {
        List<CartItem> cartItems = cartDAO.findByUser(buyerId);
        if (cartItems.isEmpty()) throw new ValidationException("Cart is empty");

        BigDecimal total = BigDecimal.ZERO;
        for (CartItem ci : cartItems) {
            total = total.add(ci.getUnitPrice().multiply(BigDecimal.valueOf(ci.getQuantity())));
        }

        try (Connection c = DataSourceListener.getDataSource().getConnection()) {
            c.setAutoCommit(false);
            try {
                int orderId = orderDAO.createOrder(c, buyerId, total);
                for (CartItem ci : cartItems) {
                    orderDAO.addOrderItem(c, orderId, ci.getProductId(), ci.getQuantity(), ci.getUnitPrice());
                    productDAO.decrementStock(ci.getProductId(), ci.getQuantity());
                }
                cartDAO.clear(buyerId);
                c.commit();
                return orderId;
            } catch (SQLException e) {
                c.rollback();
                throw e;
            } finally {
                c.setAutoCommit(true);
            }
        }
    }

    public List<Order> getBuyerOrders(int buyerId) throws SQLException {
        return orderDAO.findByBuyer(buyerId);
    }

    public List<Order> getSellerOrders(int sellerId) throws SQLException {
        return orderDAO.findBySeller(sellerId);
    }

    public Order getOrderDetail(int orderId) throws SQLException {
        return orderDAO.findById(orderId);
    }
}
