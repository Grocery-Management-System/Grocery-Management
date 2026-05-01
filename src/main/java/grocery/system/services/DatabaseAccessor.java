package grocery.system.services;

import grocery.system.model.Order;
import grocery.system.model.OrderItem;
import grocery.system.model.Product;
import grocery.system.model.Supplier;

import java.nio.file.Path;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseAccessor implements AutoCloseable {
    private final Path path = Path.of("localdata", "database.db");
    //i did path.of instead of path.get, which was not working
    //this may break the code so we'll resolve this later
    private Connection conn;
    private static final String DB_NAME = "grocery_db";
    private final String DB_user = System.getenv("DB_USER");
    private final String DB_password = System.getenv("DB_PASSWORD");

    public DatabaseAccessor() throws SQLException {
        if (DB_user == null || DB_password == null) {
            throw new RuntimeException("DB_USER or DB_PASSWORD not set in environment");
        }

        this.conn = DriverManager.getConnection(
                "jdbc:mysql://localhost:3306/" + DB_NAME,
                DB_user,
                DB_password
        );
    }

    public void initDatabase() throws Exception{

        if(DB_user == null || DB_password == null) {
            throw new RuntimeException("Please set DB_USER and DB_PASSWORD environment variables");
        }

        try (Connection rootConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/", DB_user, DB_password);
             Statement st = rootConn.createStatement()) { //connects to localhost port mysql will use
            System.out.println("Creating database...");
            st.execute("CREATE DATABASE IF NOT EXISTS " + DB_NAME); //creates database
            System.out.println("Database creation attempted");
            System.out.println("USER: " + DB_user);
            System.out.println("PASS: " + DB_password);
        }


        conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + DB_NAME,
                DB_user,
                DB_password); //NOW connect to port + database

        try (Statement st = conn.createStatement()){
            st.execute("SET innodb_lock_wait_timeout = 5"); // waits if database is locked for 5 seconds, then fails
            st.execute("SET FOREIGN_KEY_CHECKS = 1"); //by default foreign key checks are already set to 1,
            //so may remove this later
            // we should see if checks are enabled but may delete this too
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }


        try (Statement st = conn.createStatement()){

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
            """); //creates table Orders as Order is a reserved keyword



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
                            productName VARCHAR(20),
                            subTotal Decimal(10, 2),
                            FOREIGN KEY (orderID) REFERENCES Orders(orderID),
                            FOREIGN KEY (productID) REFERENCES Product(productID)
                        );
            """);
            /*creates table OrderItem. it seems it doesn't detect order and product tables yet
            i will find a way to make sure those tables are actually included next time
            */

        } //end try
    } //end initDatabase

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

    /** Update all editable fields of an existing product by productID. */
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

    /** Delete a product by ID. Will fail if OrderItems reference it (by design). */
    public void deleteProduct(int productID) throws SQLException {
        String sql = "DELETE FROM Product WHERE productID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productID);
            ps.executeUpdate();
        }
    }

    /** Returns every product — used to populate the ProductPage TableView. */
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

    public List<Product> searchProducts(String keyword) throws SQLException {
        List<Product> list = new ArrayList<>();
        String sql = """
            SELECT * FROM Product
            WHERE LOWER(productName) LIKE ?
               OR CAST(productID AS CHAR) LIKE ?
        """;
        String pattern = "%" + keyword.toLowerCase() + "%";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, pattern);
            ps.setString(2, pattern);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapProduct(rs));
            }
        }
        return list;
    }

    /** Filter products by category. Pass "All" or null to skip filtering. */
    public List<Product> getProductsByCategory(String category) throws SQLException {
        if (category == null || category.equals("All")) return getAllProducts();
        List<Product> list = new ArrayList<>();
        String sql = "SELECT * FROM Product WHERE category = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, category);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapProduct(rs));
            }
        }
        return list;
    }

    /** Returns all products where currentStock < minThreshold (for the Low Stock page). */
    public List<Product> getLowStockProducts() throws SQLException {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT * FROM Product WHERE currentStock < minThreshold";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapProduct(rs));
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

    public void updateSupplier(Supplier supplier) throws SQLException {
        String sql = """
            UPDATE Supplier
            SET supplierName = ?, address = ?, phoneNumber = ?
            WHERE supplierID = ?
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, supplier.getSupplierName());
            ps.setString(2, supplier.getSupplierAddress());
            ps.setString(3, supplier.getSupplierPhone());
            ps.setInt(4,    supplier.getSupplierID());
            ps.executeUpdate();
        }
    }

    public void deleteSupplier(int supplierID) throws SQLException {
        String sql = "DELETE FROM Supplier WHERE supplierID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, supplierID);
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


    public int addOrder(Order order) throws SQLException {
        String sql = """
            INSERT INTO Orders (supplierID, orderStatus, totalCost, orderDate, comment)
            VALUES (?, ?, ?, ?, ?)
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1,    order.getSupplierID());
            ps.setString(2, order.getOrderStatus());
            ps.setDouble(3, order.getTotalCost());
            // FIX: java.util.Date → java.sql.Date conversion (was a cast bug before)
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

    public void updateOrderStatus(int orderID, String status) throws SQLException {
        String sql = "UPDATE Orders SET orderStatus = ? WHERE orderID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2,    orderID);
            ps.executeUpdate();
        }
    }

    public void deleteOrder(int orderID) throws SQLException {
        // OrderItems are deleted automatically via ON DELETE CASCADE
        String sql = "DELETE FROM Orders WHERE orderID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderID);
            ps.executeUpdate();
        }
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

    public void addOrderItem(OrderItem item) throws SQLException {
        item.setSubTotal();   // recalculate before saving
        String sql = """
            INSERT INTO OrderItem (orderID, productID, quantity, unitPrice, subTotal)
            VALUES (?, ?, ?, ?, ?)
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1,    item.getOrderID());
            ps.setInt(2,    item.getProductID());
            ps.setInt(3,    item.getQuantity());
            ps.setDouble(4, item.getUnitPrice());
            ps.setDouble(5, item.getSubTotal());
            ps.executeUpdate();
        }
    }

    public void deleteOrderItem(int orderItemID) throws SQLException {
        String sql = "DELETE FROM OrderItem WHERE orderItemID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderItemID);
            ps.executeUpdate();
        }
    }

    /** Returns all items for a given order — used by OrderDetailPage. */
    public List<OrderItem> getItemsByOrder(int orderID) throws SQLException {
        List<OrderItem> list = new ArrayList<>();
        String sql = "SELECT * FROM OrderItem WHERE orderID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderItem item = new OrderItem();
                    item.setOrderItemID(rs.getInt("orderItemID"));
                    item.setOrderID(rs.getInt("orderID"));
                    item.setProductID(rs.getInt("productID"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setUnitPrice(rs.getDouble("unitPrice"));
                    list.add(item);
                }
            }
        }
        return list;
    } //close getItemsByOrder


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
    } //close mapProduct





} //end of class