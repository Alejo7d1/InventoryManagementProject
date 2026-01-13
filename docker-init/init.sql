CREATE DATABASE IF NOT EXISTS dcava_db
CHARACTER SET utf8mb4
COLLATE utf8mb4_general_ci;

USE dcava_db;

-- ======================
-- USER ADMIN
-- ======================
CREATE TABLE user_admin (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(100) NOT NULL,
  email VARCHAR(150) NOT NULL UNIQUE,
  uid_firebase VARCHAR(255) NOT NULL UNIQUE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB;

-- ======================
-- PRODUCT
-- ======================
CREATE TABLE product (
  id INT AUTO_INCREMENT PRIMARY KEY,
  name_product VARCHAR(255) NOT NULL,
  description_product TEXT NOT NULL,
  price DOUBLE NOT NULL,
  cost DOUBLE NOT NULL,
  category VARCHAR(64),
  status_product VARCHAR(12) DEFAULT 'active',
  stock INT NOT NULL,
  compatible_tags TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

  FULLTEXT KEY ft_product_name (name_product),
  FULLTEXT KEY ft_product_tags (compatible_tags),

  INDEX idx_product_category (category),
  INDEX idx_product_status (status_product),
  INDEX idx_product_stock (stock),
  INDEX idx_product_status_category (status_product, category)
) ENGINE=InnoDB;

-- ======================
-- PRODUCT IMAGE
-- ======================
CREATE TABLE product_image (
  id INT AUTO_INCREMENT PRIMARY KEY,
  product_id INT NOT NULL,
  file_name VARCHAR(255) NOT NULL,
  file_path VARCHAR(255) NOT NULL,

  INDEX idx_product_image_product (product_id),
  FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- ======================
-- SALE
-- ======================
CREATE TABLE sale (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT,
  sale_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  subtotal DOUBLE NOT NULL,
  discount DOUBLE NOT NULL DEFAULT 0,
  total DOUBLE NOT NULL,
  notes TEXT,

  INDEX idx_sale_date (sale_date),
  INDEX idx_sale_user (user_id),
  INDEX idx_sale_user_date (user_id, sale_date),

  FOREIGN KEY (user_id) REFERENCES user_admin(id)
) ENGINE=InnoDB;

-- ======================
-- SALE ITEMS
-- ======================
CREATE TABLE sale_items (
  id INT AUTO_INCREMENT PRIMARY KEY,
  sale_id INT NOT NULL,
  product_id INT NULL,
  item_name VARCHAR(255) NOT NULL,
  item_description TEXT,
  quantity INT NOT NULL,
  unit_price DOUBLE NOT NULL,
  unit_cost DOUBLE NOT NULL,
  is_external BOOLEAN NOT NULL DEFAULT FALSE,

  INDEX idx_sale_items_sale (sale_id),
  INDEX idx_sale_items_product (product_id),
  INDEX idx_sale_items_external (is_external),
  INDEX idx_sale_items_product_sale (product_id, sale_id),

  FOREIGN KEY (sale_id) REFERENCES sale(id) ON DELETE CASCADE,
  FOREIGN KEY (product_id) REFERENCES product(id)
) ENGINE=InnoDB;

-- ======================
-- ADVERTISEMENT
-- ======================
CREATE TABLE advertisement (
  id INT AUTO_INCREMENT PRIMARY KEY,
  file_path VARCHAR(255) NOT NULL,
  title VARCHAR(150),
  ad_type VARCHAR(12),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

  INDEX idx_advertisement_type (ad_type)
) ENGINE=InnoDB;

-- ======================
-- STOCK LOG
-- ======================
CREATE TABLE stock_log (
  id INT AUTO_INCREMENT PRIMARY KEY,
  product_id INT NOT NULL,
  change_amount INT NOT NULL,
  reason VARCHAR(100),
  timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,

  INDEX idx_stock_log_product (product_id),
  INDEX idx_stock_log_product_time (product_id, timestamp),

  FOREIGN KEY (product_id) REFERENCES product(id)
) ENGINE=InnoDB;

