package com.bluemart.bluemart.dao;

import com.bluemart.bluemart.model.User;
import java.sql.SQLException;
import java.util.List;

public interface UserDAO {
    User findByEmail(String email) throws SQLException;
    User findById(int id) throws SQLException;
    int create(User user) throws SQLException;
    List<User> findAll() throws SQLException;
}