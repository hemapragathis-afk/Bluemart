INSERT INTO products (seller_id, name, description, price, stock_qty, category, image_url)
SELECT 1, 'Wireless Mouse', 'Ergonomic wireless mouse with USB receiver', 599.00, 50, 'Electronics', 'https://via.placeholder.com/150'
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Wireless Mouse');

INSERT INTO products (seller_id, name, description, price, stock_qty, category, image_url)
SELECT 1, 'Cotton T-Shirt', 'Comfortable 100% cotton round-neck t-shirt', 349.00, 100, 'Clothing', 'https://via.placeholder.com/150'
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Cotton T-Shirt');

INSERT INTO products (seller_id, name, description, price, stock_qty, category, image_url)
SELECT 1, 'Notebook Set', 'Pack of 3 ruled notebooks, 200 pages each', 199.00, 200, 'Stationery', 'https://via.placeholder.com/150'
WHERE NOT EXISTS (SELECT 1 FROM products WHERE name = 'Notebook Set');