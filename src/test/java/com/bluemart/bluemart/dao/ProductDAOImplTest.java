package com.bluemart.bluemart.dao;

import com.bluemart.bluemart.listener.DataSourceListener;
import com.bluemart.bluemart.model.Product;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProductDAOImplTest {

    private static HikariDataSource testDataSource;
    private final ProductDAO productDAO = new ProductDAOImpl();

    @BeforeAll
    static void setUpDatabase() throws Exception {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1");
        config.setDriverClassName("org.h2.Driver");
        config.setUsername("sa");
        config.setPassword("");
        testDataSource = new HikariDataSource(config);

        // Inject this test DataSource into DataSourceListener's static field,
        // since all DAOs read from DataSourceListener.getDataSource()
        Field field = DataSourceListener.class.getDeclaredField("dataSource");
        field.setAccessible(true);
        field.set(null, testDataSource);

        try (Connection c = testDataSource.getConnection();
             Statement stmt = c.createStatement()) {
            stmt.execute("CREATE TABLE users (id INT AUTO_INCREMENT PRIMARY KEY, name VARCHAR(100), " +
                    "email VARCHAR(150) UNIQUE, password_hash VARCHAR(255), role VARCHAR(10), created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            stmt.execute("CREATE TABLE products (id INT AUTO_INCREMENT PRIMARY KEY, seller_id INT, name VARCHAR(150), " +
                    "description VARCHAR(1000), price DECIMAL(10,2), stock_qty INT, category VARCHAR(100), " +
                    "image_url VARCHAR(500), created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
            stmt.execute("INSERT INTO users (name, email, password_hash, role) VALUES ('Test Seller', 'seller@test.com', 'x', 'SELLER')");
        }
    }

    @BeforeEach
    void cleanProducts() throws Exception {
        try (Connection c = testDataSource.getConnection();
             Statement stmt = c.createStatement()) {
            stmt.execute("DELETE FROM products");
        }
    }

    @AfterAll
    static void tearDown() {
        if (testDataSource != null) testDataSource.close();
    }

    @Test
    void create_and_findById_returnsCorrectProduct() throws Exception {
        Product p = new Product();
        p.setSellerId(1);
        p.setName("Test Widget");
        p.setDescription("A widget for testing");
        p.setPrice(new BigDecimal("99.99"));
        p.setStockQty(10);
        p.setCategory("Testing");
        p.setImageUrl("http://example.com/img.png");

        int id = productDAO.create(p);
        Product found = productDAO.findById(id);

        assertNotNull(found);
        assertEquals("Test Widget", found.getName());
        assertEquals(0, new BigDecimal("99.99").compareTo(found.getPrice()));
        assertEquals(10, found.getStockQty());
    }

    @Test
    void search_byKeyword_findsMatchingProduct() throws Exception {
        Product p = new Product();
        p.setSellerId(1);
        p.setName("Searchable Gadget");
        p.setDescription("desc");
        p.setPrice(new BigDecimal("50.00"));
        p.setStockQty(5);
        p.setCategory("Gadgets");
        p.setImageUrl("x");
        productDAO.create(p);

        List<Product> results = productDAO.search("gadget", null);

        assertEquals(1, results.size());
        assertEquals("Searchable Gadget", results.get(0).getName());
    }

    @Test
    void decrementStock_reducesQuantityCorrectly() throws Exception {
        Product p = new Product();
        p.setSellerId(1);
        p.setName("Stocked Item");
        p.setDescription("desc");
        p.setPrice(new BigDecimal("10.00"));
        p.setStockQty(20);
        p.setCategory("Misc");
        p.setImageUrl("x");
        int id = productDAO.create(p);

        productDAO.decrementStock(id, 5);

        Product updated = productDAO.findById(id);
        assertEquals(15, updated.getStockQty());
    }

    @Test
    void decrementStock_throwsWhenInsufficientStock() throws Exception {
        Product p = new Product();
        p.setSellerId(1);
        p.setName("Low Stock Item");
        p.setDescription("desc");
        p.setPrice(new BigDecimal("10.00"));
        p.setStockQty(2);
        p.setCategory("Misc");
        p.setImageUrl("x");
        int id = productDAO.create(p);

        assertThrows(java.sql.SQLException.class, () -> productDAO.decrementStock(id, 100));
    }
}