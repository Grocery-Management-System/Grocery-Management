package grocery.system.services;

import grocery.system.model.Product;

import java.nio.file.Path;
import java.sql.*;

public class DatabaseAccessor {

    private final Path path = Path.of("localdata", "database.db");
    //i did path.of instead of path.get, which was not working
    //this may break the code so we'll resolve this later
    private final String url = "jdbc:mysql" + path;
    private final Connection conn;

    public DatabaseAccessor() throws SQLException {
        this.conn = DriverManager.getConnection(url);
        try (Statement st = conn.createStatement()){
            st.execute("SET innodb_lock_wait_timeout = 5"); // waits if database is locked for 5 seconds, then fails
            st.execute("SET FOREIGN_KEY_CHECKS = 1"); //by default foreign key checks are already set to 1,
            //so may remove this later
            st.execute("SELECT @@FOREIGN_KEY_CHECKS"); // we should see if checks are enabled but may delete this too
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void initDatabase() throws Exception{
        try (Statement st = conn.createStatement()){
            st.execute("""
                        CREATE TABLE IF NOT EXISTS Order(
                            orderID INT PRIMARY KEY AUTO_INCREMENT NOT NULL,
                            orderDate DATE,
                            supplierID INT,
                            totalCOST DOUBLE
                        );
            """); //creates table Order

            st.execute("""
                        CREATE TABLE IF NOT EXISTS OrderItem(
                            orderItemID INT PRIMARY KEY AUTO_INCREMENT NOT NULL,
                            orderID INT NOT NULL,
                            productID INT NOT NULL,
                            quantity INT,
                            unitPrice DOUBLE,
                            productName VARCHAR(20),
                            subTotal DOUBLE,
                            FOREIGN KEY (orderID) REFERENCES Order(orderID),
                            FOREIGN KEY (productID) REFERENCES Product(productID)
                        );
            """);
            /*creates table OrderItem. it seems it doesn't detect order and product tables yet
            i will find a way to make sure those tables are actually included next time
            */

            st.execute("""
                        CREATE TABLE IF NOT EXISTS PRODUCT(
                        productID INT PRIMARY KEY AUTO_INCREMENT,
                        productName VARCHAR(20),
                        category VARCHAR(20), 
                        currentStock INT,
                        minThreshold INT,
                        aisleNumber VARCHAR(2),
                        supplierID INT,
                        FOREIGN KEY (supplierID) REFERENCES Supplier(supplierID)
                        );
            
            """);

            st.execute("""
                        CREATE TABLE IF NOT EXISTS Supplier(
                        supplierID INT PRIMARY KEY AUTO_INCREMENT,
                        supplierName VARCHAR(20),
                        address VARCHAR(100),
                        phoneNumber VARCHAR(10)
                        );
            """);

        }
    }

    public Product getProduct(String productName) throws SQLException {
        if (productName == null || productName.trim().isEmpty()) return null;
        String sqlQuery = """
                SELECT productID, productName, category, currentStock, minThreshold, aisleNumber, supplierID, isPerishable
                FROM Product
                WHERE productName = ?
                LIMIT 1
                """;

        try (PreparedStatement ps = conn.prepareStatement(sqlQuery)) {
            ps.setString(1, productName.trim());
            try (ResultSet set = ps.executeQuery()) {
                if(!set.next()) {
                    return null;
                }
                Product res = new Product();
                res.setProductID(set.getInt("productID"));
                res.setProductName(set.getString("productName"));
                res.setCategory(set.getString("category"));
                res.setCurrentStock(set.getInt("currentStock"));
                res.setMinThreshold(set.getInt("minThreshold"));
                res.setAisleNumber(set.getInt("aisleNumber"));
                res.setSupplierID(set.getInt("supplierID"));
                res.setIsPerishable(set.getBoolean("isPerishable"));

                return res;
            }
        }

    }

    public void setProduct(Product product) throws SQLException {
        if (product.getProductName().isBlank()) return;
        String sqlQuery = """
                INSERT INTO Product(productID, productName, category, currentStock, minThreshold, aisleNumber, supplierID)
                VALUES(?, ?, ?, ?, ?, ?, ?);
                """;
        try (PreparedStatement ps = conn.prepareStatement(sqlQuery)) {
            ps.setInt(1, product.getProductID());
            ps.setString(2, product.getProductName());
            ps.setString(3, product.getCategory());
            ps.setInt(4, product.getCurrentStock());
            ps.setInt(5, product.getMinThreshold());
            ps.setInt(6, product.getAisleNumber());
            ps.setInt(7, product.getSupplierID());
            ps.executeUpdate();

        }
    }



}
