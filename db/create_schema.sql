-- Create the database if it doesn't exist
CREATE DATABASE IF NOT EXISTS grocery_db;
USE grocery_db;

-- Drop tables in reverse order of dependency to avoid Foreign Key errors
DROP TABLE IF EXISTS OrderItem;
DROP TABLE IF EXISTS Orders;
DROP TABLE IF EXISTS Product;
DROP TABLE IF EXISTS Supplier;

-- 1. Supplier Table
CREATE TABLE Supplier (
    supplierID INT PRIMARY KEY AUTO_INCREMENT,
    supplierName VARCHAR(50) NOT NULL,
    address VARCHAR(100),
    phoneNumber VARCHAR(15)
);

-- 2. Product Table
CREATE TABLE Product (
    productID INT PRIMARY KEY AUTO_INCREMENT,
    productName VARCHAR(50) NOT NULL,
    category VARCHAR(30),
    currentStock INT DEFAULT 0,
    minThreshold INT DEFAULT 5,
    aisleNumber INT,
    supplierID INT,
    isPerishable BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (supplierID) REFERENCES Supplier(supplierID) ON DELETE SET NULL
);

-- 3. Orders Table
CREATE TABLE Orders (
    orderID INT PRIMARY KEY AUTO_INCREMENT,
    orderDate DATE NOT NULL,
    supplierID INT,
    totalCost DECIMAL(10, 2),
    FOREIGN KEY (supplierID) REFERENCES Supplier(supplierID)
);

-- 4. OrderItem Table
CREATE TABLE OrderItem (
    orderItemID INT PRIMARY KEY AUTO_INCREMENT,
    orderID INT NOT NULL,
    productID INT NOT NULL,
    quantity INT NOT NULL,
    unitPrice DECIMAL(10, 2),
    subTotal DECIMAL(10, 2),
    FOREIGN KEY (orderID) REFERENCES Orders(orderID) ON DELETE CASCADE,
    FOREIGN KEY (productID) REFERENCES Product(productID)
);