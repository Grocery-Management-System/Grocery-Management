package grocery.system.services;

import grocery.system.model.Order;
import grocery.system.model.OrderItem;
import grocery.system.model.Product;
import grocery.system.model.Supplier;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseAccessor implements AutoCloseable {
    private Connection conn;
    private static final String DB_NAME = "grocery_db";

    public DatabaseAccessor() throws SQLException {
        // 1. Prioritize Cloud settings from Environment Variables
        String dbUrl = System.getenv("DB_URL");
        String user  = System.getenv("DB_USER");
        String pass  = System.getenv("DB_PASSWORD");

        // 2. Fallback to local if Cloud settings are missing
        if (dbUrl == null) {
            dbUrl = "jdbc:mysql://localhost:3306/" + DB_NAME;
        }
        if (user == null || pass == null) {
            user = System.getenv("DB_USER"); // Fallback to local variables if applicable
            pass = System.getenv("DB_PASSWORD");
        }

        if (user == null || pass == null) {
            throw new RuntimeException("DB_USER or DB_PASSWORD not set in environment");
        }

        this.conn = DriverManager.getConnection(dbUrl, user, pass);
    }

    public void initDatabase() throws Exception {
        String dbUrl = System.getenv("DB_URL");
        String user  = System.getenv("DB_USER");
        String pass  = System.getenv("DB_PASSWORD");

        // Use Cloud URL if available, otherwise use root connection for local DB creation
        if (dbUrl == null) {
            // LOCAL MODE: Ensure database exists
            try (Connection rootConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/", user, pass);
                 Statement st = rootConn.createStatement()) {
                st.execute("CREATE DATABASE IF NOT EXISTS " + DB_NAME);
            }
            dbUrl = "jdbc:mysql://localhost:3306/" + DB_NAME;
        }

        // Re-establish connection to the specific database (Local or Cloud)
        if (conn == null || conn.isClosed()) {
            conn = DriverManager.getConnection(dbUrl, user, pass);
        }

        try (Statement st = conn.createStatement()){
            st.execute("SET innodb_lock_wait_timeout = 5");
            st.execute("SET FOREIGN_KEY_CHECKS = 1");

            // Attempt to fix existing local tables by adding the column if it's missing
            try {
                st.execute("ALTER TABLE Product ADD COLUMN isPerishable BOOLEAN");
            } catch (SQLException e) {
                // Ignore if column already exists
            }

            // Create Tables
            st.execute("""
                CREATE TABLE IF NOT EXISTS Supplier(
                    supplierID INT PRIMARY KEY AUTO_INCREMENT,
                    supplierName VARCHAR(20),
                    address VARCHAR(100),
                    phoneNumber VARCHAR(10)
                );
            """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS Orders(
                    orderID INT PRIMARY KEY AUTO_INCREMENT,
                    orderDate DATE,
                    supplierID INT,
                    totalCost Decimal(10, 2)
                );
            """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS Product(
                    productID INT PRIMARY KEY AUTO_INCREMENT,
                    productName VARCHAR(20),
                    category VARCHAR(20), 
                    currentStock INT,
                    minThreshold INT,
                    aisleNumber VARCHAR(2),
                    supplierID INT,
                    isPerishable BOOLEAN,
                    FOREIGN KEY (supplierID) REFERENCES Supplier(supplierID)
                );
            """);

            st.execute("""
                CREATE TABLE IF NOT EXISTS OrderItem(
                    orderItemID INT PRIMARY KEY AUTO_INCREMENT,
                    orderID INT NOT NULL,
                    productID INT NOT NULL,
                    quantity INT,
                    unitPrice Decimal(10, 2),
                    subTotal Decimal(10, 2),
                    FOREIGN KEY (orderID) REFERENCES Orders(orderID),
                    FOREIGN KEY (productID) REFERENCES Product(productID)
                );
            """);
        }
    }

    public void addProduct(Product product) throws SQLException {
        String sql = """
            INSERT INTO Product (productName, category, currentStock,
                                 minThreshold, aisleNumber, supplierID, isPerishable)
            VALUES (?, ?, ?, ?, ?, ?, ?)
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, product.getProductName());
            ps.setString(2, product.getCategory());
            ps.setInt(3,    product.getCurrentStock());
            ps.setInt(4,    product.getMinThreshold());
            ps.setInt(5,    product.getAisleNumber());
            ps.setInt(6,    product.getSupplierID());
            ps.setBoolean(7, product.isPerishable());
            ps.executeUpdate();
        }
    }

    public void updateProduct(Product product) throws SQLException {
        String sql = """
            UPDATE Product
            SET productName  = ?,
                category     = ?,
                currentStock = ?,
                minThreshold = ?,
                aisleNumber  = ?,
                supplierID   = ?,
                isPerishable = ?
            WHERE productID = ?
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, product.getProductName());
            ps.setString(2, product.getCategory());
            ps.setInt(3,    product.getCurrentStock());
            ps.setInt(4,    product.getMinThreshold());
            ps.setInt(5,    product.getAisleNumber());
            ps.setInt(6,    product.getSupplierID());
            ps.setBoolean(7, product.isPerishable());
            ps.setInt(8,    product.getProductID());
            ps.executeUpdate();
        }
    }

    public void deleteProduct(int productID) throws SQLException {
        String sql = "DELETE FROM Product WHERE productID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productID);
            ps.executeUpdate();
        }
    }

    public List<Product> getAllProducts() throws SQLException {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT * FROM Product";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapProduct(rs));
            }
        }
        return list;
    }

    public void addSupplier(Supplier supplier) throws SQLException {
        String sql = "INSERT INTO Supplier (supplierName, address, phoneNumber) VALUES (?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, supplier.getSupplierName());
            ps.setString(2, supplier.getSupplierAddress());
            ps.setString(3, supplier.getSupplierPhone());
            ps.executeUpdate();
        }
    }

    public List<Supplier> getAllSuppliers() throws SQLException {
        List<Supplier> list = new ArrayList<>();
        String sql = "SELECT * FROM Supplier";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Supplier s = new Supplier();
                s.setSupplierID(rs.getInt("supplierID"));
                s.setSupplierName(rs.getString("supplierName"));
                s.setSupplierAddress(rs.getString("address"));
                s.setSupplierPhone(rs.getString("phoneNumber"));
                list.add(s);
            }
        }
        return list;
    }
    /** Delete a supplier by ID. */
    public void deleteSupplier(int supplierID) throws SQLException {
        String sql = "DELETE FROM Supplier WHERE supplierID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, supplierID);
            ps.executeUpdate();
        }
    }
    public void close() {
        try {
            if (conn != null && !conn.isClosed()) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private Product mapProduct(ResultSet rs) throws SQLException {
        return new Product(
                rs.getInt("productID"),
                rs.getString("productName"),
                rs.getString("category"),
                rs.getInt("currentStock"),
                rs.getInt("minThreshold"),
                rs.getInt("aisleNumber"),
                rs.getInt("supplierID"),
                rs.getBoolean("isPerishable")
        );
    }

    public List<Order> getAllOrders() throws SQLException {
        List<Order> list = new ArrayList<>();
        String sql = "SELECT * FROM Orders ORDER BY orderDate DESC";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Order o = new Order();
                o.setOrderID(rs.getInt("orderID"));
                o.setSupplierID(rs.getInt("supplierID"));
                o.setOrderStatus(rs.getString("orderStatus"));
                o.setTotalCost(rs.getDouble("totalCost"));
                Date d = rs.getDate("orderDate");
                if (d != null) o.setOrderDate(new java.util.Date(d.getTime()));
                o.setComment(rs.getString("comment"));
                list.add(o);
            }
        }
        return list;
    }

    public int addOrder(Order order) throws SQLException {
        String sql = """
        INSERT INTO Orders (supplierID, orderStatus, totalCost, orderDate, comment)
        VALUES (?, ?, ?, ?, ?)
    """;
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1,    order.getSupplierID());
            ps.setString(2, order.getOrderStatus());
            ps.setDouble(3, order.getTotalCost());
            ps.setDate(4, order.getOrderDate() != null
                    ? new Date(order.getOrderDate().getTime()) : null);
            ps.setString(5, order.getComment());
            ps.executeUpdate();

            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) return keys.getInt(1);
            }
        }
        return -1;
    }

    public void addOrderItem(OrderItem item) {
    }

    public void updateOrderStatus(int orderID, String submitted) {
    }
}
//revert