package com.bluemart.bluemart.service;

import com.bluemart.bluemart.dao.UserDAO;
import com.bluemart.bluemart.dao.UserDAOImpl;
import com.bluemart.bluemart.exception.ValidationException;
import com.bluemart.bluemart.model.User;
import org.mindrot.jbcrypt.BCrypt;

import java.sql.SQLException;

public class AuthService {
    private final UserDAO userDAO = new UserDAOImpl();

    public int register(String name, String email, String password, String role) throws SQLException {
        if (name == null || name.isBlank()) throw new ValidationException("Name is required");
        if (email == null || email.isBlank()) throw new ValidationException("Email is required");
        if (password == null || password.length() < 6) throw new ValidationException("Password must be at least 6 characters");
        if (!"BUYER".equals(role) && !"SELLER".equals(role)) throw new ValidationException("Role must be BUYER or SELLER");

        if (userDAO.findByEmail(email) != null) {
            throw new ValidationException("Email already registered");
        }

        String hash = BCrypt.hashpw(password, BCrypt.gensalt());

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPasswordHash(hash);
        user.setRole(role);

        return userDAO.create(user);
    }

    public User login(String email, String password) throws SQLException {
        User user = userDAO.findByEmail(email);
        if (user == null) {
            throw new ValidationException("Invalid email or password");
        }
        if (!BCrypt.checkpw(password, user.getPasswordHash())) {
            throw new ValidationException("Invalid email or password");
        }
        return user;
    }
}