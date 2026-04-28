package grocery.system.services;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import grocery.system.model.Order;
import grocery.system.model.Product;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;




public class DatabaseAccessor {

    private final String url = "jdbc:mysql://localhost:3306/grocery_db";
    private final String user = "root";
    // This tells Java: "Look for a variable named DB_PASSWORD on this Mac"
    private final String password = System.getenv("DB_PASSWORD");
    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    public void initDatabase() throws Exception {
        try (Connection conn = getConnection();
             Statement st = conn.createStatement()) {

            // Create Supplier first (independent)
            st.execute("""
                CREATE TABLE IF NOT EXISTS Supplier(
                    supplierID INT PRIMARY KEY AUTO_INCREMENT,
                    supplierName VARCHAR(50),
                    address VARCHAR(100),
                    phoneNumber VARCHAR(15)
                );
            """);

            // Create Product (depends on Supplier)
            st.execute("""
                CREATE TABLE IF NOT EXISTS PRODUCT(
                    productID INT PRIMARY KEY AUTO_INCREMENT,
                    productName VARCHAR(50),
                    category VARCHAR(50),
                    currentStock INT,
                    minThreshold INT,
                    aisleNumber VARCHAR(10),
                    supplierID INT,
                    FOREIGN KEY (supplierID) REFERENCES Supplier(supplierID)
                );
            """);

            // Create Order (depends on Supplier)
            st.execute("""
                CREATE TABLE IF NOT EXISTS `Order`(
                    orderID INT PRIMARY KEY AUTO_INCREMENT NOT NULL,
                    orderDate DATE,
                    supplierID INT,
                    totalCOST DOUBLE,
                    orderStatus VARCHAR(20),
                    comment TEXT,
                    FOREIGN KEY (supplierID) REFERENCES Supplier(supplierID)
                );
            """);

            // OrderItem (depends on Order and Product)
            st.execute("""
                CREATE TABLE IF NOT EXISTS OrderItem(
                    orderItemID INT PRIMARY KEY AUTO_INCREMENT NOT NULL,
                    orderID INT NOT NULL,
                    productID INT NOT NULL,
                    quantity INT,
                    unitPrice DOUBLE,
                    subTotal DOUBLE,
                    FOREIGN KEY (orderID) REFERENCES `Order`(orderID),
                    FOREIGN KEY (productID) REFERENCES Product(productID)
                );
            """);
        }
    }
    //Save / Submit Order
    public void saveOrder(Order order) throws SQLException {
        String sql = "INSERT INTO `Order` (orderDate, supplierID, totalCost, comment, orderStatus) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)){
            pstmt.setDate(1, new java.sql.Date(order.getOrderDate().getTime()));
            pstmt.setInt(2, order.getSupplierID());
            pstmt.setDouble(3, order.getTotalCost());
            pstmt.setString(4, order.getComment());
            pstmt.setString(5, order.getOrderStatus()); //draft or submitted

            pstmt.executeUpdate();

            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()){
                order.setOrderID(rs.getInt(1));
            }
        }
    }

    //delete order task
    public void deleteOrder(int orderID) throws SQLException{
        String sql = "DELETE FROM `Order` WHERE orderID = ?";
        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)){
            pstmt.setInt(1, orderID);
            pstmt.executeUpdate();
        }
    }

    public List<String> getAllSuppliers() throws SQLException {
        List<String> suppliers = new ArrayList<>();
        String sql = "SELECT supplierName FROM Supplier";
        try (Connection conn = getConnection(); Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                suppliers.add(rs.getString("supplierName"));
            }
        }
        return suppliers;
    }

    public List<Order> getAllOrders() throws SQLException {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT * FROM `Order` ORDER BY orderDate DESC";
        try (Connection conn = getConnection(); Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Order o = new Order();
                o.setOrderID(rs.getInt("orderID"));
                o.setSupplierID(rs.getInt("supplierID"));
                o.setOrderDate(rs.getDate("orderDate"));
                o.setOrderStatus(rs.getString("orderStatus"));
                o.setComment(rs.getString("comment"));
                orders.add(o);
            }
        }
        return orders;
    }
    public List<Product> getLowStockProducts() throws SQLException {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT * FROM PRODUCT WHERE currentStock <= minThreshold";

        try (Connection conn = getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                Product p = new Product();
                p.setProductName(rs.getString("productName"));
                p.setCurrentStock(rs.getInt("currentStock"));
                p.setMinThreshold(rs.getInt("minThreshold"));
                p.setAisleNumber(rs.getInt("aisleNumber"));
                products.add(p);
            }
        }
        return products;
    }

}
