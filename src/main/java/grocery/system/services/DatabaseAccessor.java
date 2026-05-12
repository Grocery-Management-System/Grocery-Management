package grocery.system.services;

import grocery.system.model.Order;
import grocery.system.model.OrderItem;
import grocery.system.model.Product;
import grocery.system.model.Supplier;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
/**
 * DatabaseAccessor: A Singleton service class that manages the JDBC connection
 * and handles all CRUD (Create, Read, Update, Delete) operations for the
 * Grocery Management System.
 */
public class DatabaseAccessor implements AutoCloseable {

    private Connection conn;
    private static final String DB_NAME = "grocery_db";
    // Environment variables for secure credential management
    private final String DB_user = System.getenv("DB_USER");
    private final String DB_password = System.getenv("DB_PASSWORD");

    private static DatabaseAccessor instance;

    /**
     * Private constructor to enforce Singleton pattern and initialize the DB.
     */
    public DatabaseAccessor() throws Exception {
        initDatabase();
    }

    /**
     * Singleton accessor that ensures a single database connection exists throughout the app.
     */
    public static DatabaseAccessor getInstance() throws Exception {
        if (instance == null) {
            instance = new DatabaseAccessor();
        } else if (instance.conn == null || instance.conn.isClosed()) {
            instance.initDatabase();
        }
        return instance;
    }

    /**
     * Configures the MySQL connection, creates the database if missing,
     * and initializes the required table schemas.
     */
    public void initDatabase() throws Exception{

        if(DB_user == null || DB_password == null) {
            throw new RuntimeException("Please set DB_USER and DB_PASSWORD environment variables");
        }
        // Phase 1: Connect to the MySQL server and ensure the database exists. add default values if it is a fresh database

        boolean freshDatabase;
        try (Connection rootConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/", DB_user, DB_password);
             Statement st = rootConn.createStatement()) {

            try (ResultSet rs = st.executeQuery("SHOW DATABASES LIKE '" + DB_NAME + "'")) {
                freshDatabase = !rs.next();
            }
            System.out.println("Creating database...");
            st.execute("CREATE DATABASE IF NOT EXISTS " + DB_NAME);
            System.out.println("Database creation attempted");
            System.out.println("USER: " + DB_user);
            System.out.println("PASS: " + DB_password);
        }
        // Phase 2: Connect directly to the specific grocery database
        conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/" + DB_NAME,
                DB_user,
                DB_password); //NOW connect to port + database

        try (Statement st = conn.createStatement()){
            st.execute("SET innodb_lock_wait_timeout = 5");
            st.execute("SET FOREIGN_KEY_CHECKS = 1");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // Phase 3: Setup Table Schemas
        try (Statement st = conn.createStatement()){
            // Supplier Table: Stores vendor contact details
            st.execute("""
                        CREATE TABLE IF NOT EXISTS Supplier(
                        supplierID INT PRIMARY KEY AUTO_INCREMENT,
                        supplierName VARCHAR(50) NOT NULL,
                        address VARCHAR(100),
                        phoneNumber VARCHAR(15)
                    );
            """);

            // Orders Table: Stores the header information for restocking orders
            st.execute("""
                        CREATE TABLE IF NOT EXISTS Orders(
                        orderID INT PRIMARY KEY AUTO_INCREMENT,
                        orderDate DATE NOT NULL,
                        orderStatus VARCHAR(20) DEFAULT 'DRAFT',
                        supplierID INT,
                        totalCost   DOUBLE      DEFAULT 0.0,
                        comment     TEXT,
                        FOREIGN KEY (supplierID) REFERENCES Supplier(supplierID)
                    );
            """);

            // Product Table: Stores inventory items and their stock thresholds
            st.execute("""
                CREATE TABLE IF NOT EXISTS Product(
                productID INT PRIMARY KEY AUTO_INCREMENT,
                productName VARCHAR(50) NOT NULL,
                category VARCHAR(30),
                currentStock INT DEFAULT 0,
                minThreshold INT DEFAULT 5,
                aisleNumber INT,
                supplierID INT,
                isPerishable BOOLEAN DEFAULT FALSE,
                unitPrice    DOUBLE       DEFAULT 0.0,
                FOREIGN KEY (supplierID) REFERENCES Supplier(supplierID) ON DELETE SET NULL
            );
            """);

            // OrderItem Table: Junction table linking Products to Orders (BCNF Compliant)
            st.execute("""
                        CREATE TABLE IF NOT EXISTS OrderItem(
                        orderItemID INT PRIMARY KEY AUTO_INCREMENT,
                        orderID INT NOT NULL,
                        productID INT NOT NULL,
                        quantity INT NOT NULL,
                        unitPrice DECIMAL(10, 2),
                        subTotal DECIMAL(10, 2),
                        FOREIGN KEY (orderID) REFERENCES Orders(orderID) ON DELETE CASCADE,
                        FOREIGN KEY (productID) REFERENCES Product(productID));
            """);
        }
        if (freshDatabase) {
            System.out.println("Fresh database has been made! Initializing values...");
            addSupplierDefault();
            addProductDefault();
            addOrderDefault();
            addOrderItemDefault();
        }
    }


    // --- PRODUCT CRUD OPERATIONS ---

    /**
     * CREATE: Adds a new Product to the database.
     * SQL: INSERT INTO Product (...) VALUES (...)
     */
    public void addProduct(Product product) throws SQLException {
        String sql = """
            INSERT INTO Product (productName, category, currentStock,
                                 minThreshold, aisleNumber, supplierID, isPerishable, unitPrice)
            VALUES (?, ?, ?, ?, ?, ?, ?,?)
        """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, product.getProductName());
            ps.setString(2, product.getCategory());
            ps.setInt(3,    product.getCurrentStock());
            ps.setInt(4,    product.getMinThreshold());
            ps.setInt(5,    product.getAisleNumber());
            ps.setInt(6,    product.getSupplierID());
            ps.setBoolean(7, product.isPerishable());
            ps.setDouble(8, product.getUnitPrice());
            ps.executeUpdate();
        }
    }
    /**
     * READ: Retrieves products that are currently under their minimum stock threshold.
     * SQL: SELECT * FROM Product WHERE currentStock < minThreshold
     */
    public List<Product> getLowStockProducts() throws SQLException {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT * FROM Product WHERE currentStock < minThreshold";
        try (Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(mapProduct(rs));
        }
        return list;
    }

    /**
     * UPDATE: Modifies an existing product's stock levels.
     * SQL: UPDATE Product SET currentStock = currentStock + ? WHERE productID = ?
     */
    public void updateProductQuantity(int productID, int num) throws SQLException {
        String sql = "UPDATE Product SET currentStock = currentStock + ? WHERE productID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, num);
            ps.setInt(2, productID);
            ps.executeUpdate();
        }
    }

    /**
     * DELETE: Removes a product from inventory.
     * SQL: DELETE FROM Product WHERE productID = ?
     */
    public void deleteProduct(int productID) throws SQLException {
        String sql = "DELETE FROM Product WHERE productID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productID);
            ps.executeUpdate();
        }
    }

    // --- HELPER METHODS ---

    /**
     * Maps a single row from the ResultSet into a Product object.
     */
    private Product mapProduct(ResultSet rs) throws SQLException {
        Product p = new Product(
                rs.getInt("productID"),
                rs.getString("productName"),
                rs.getString("category"),
                rs.getInt("currentStock"),
                rs.getInt("minThreshold"),
                rs.getInt("aisleNumber"),
                rs.getInt("supplierID"),
                rs.getBoolean("isPerishable")
        );
        p.setUnitPrice(rs.getDouble("unitPrice"));
        return p;
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
                isPerishable = ?,
                unitPrice = ?
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
            ps.setDouble(8, product.getUnitPrice());
            ps.setInt(9,    product.getProductID());
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

    public void deleteOrder(int orderID) throws SQLException {
        // OrderItems are deleted automatically via ON DELETE CASCADE
        String sql = "DELETE FROM Orders WHERE orderID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderID);
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

    public void close() {
        try {
            if (conn != null && !conn.isClosed()) conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
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

    public void addOrderItem(OrderItem item) throws SQLException {
        String sql = """
        INSERT INTO OrderItem (orderID, productID, quantity, unitPrice)
        VALUES (?, ?, ?, ?)
    """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1,    item.getOrderID());
            ps.setInt(2,    item.getProductID());
            ps.setInt(3,    item.getQuantity());
            ps.setDouble(4, item.getUnitPrice());
            ps.executeUpdate();
        }
    }

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
    }

    public void updateOrderStatus(int orderID, String status) throws SQLException {
        String sql = "UPDATE Orders SET orderStatus = ? WHERE orderID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, status);
            ps.setInt(2,    orderID);
            ps.executeUpdate();
        }
    }

    public List<Product> getProductsBySupplier(int supplierID) throws SQLException {
        List<Product> list = new ArrayList<>();
        String sql = "SELECT * FROM Product WHERE supplierID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, supplierID);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(mapProduct(rs));
            }
        }
        return list;
    }
    public void addProductDefault() throws SQLException{
        String sql = """
                INSERT INTO Product (productID, productName, category, currentStock, minThreshold, aisleNumber, supplierID, isPerishable, unitPrice) VALUES
                (1,  'Whole Milk',           'Food & Beverage',  25,  10,  1,  1,  TRUE,  3.50),
                (2,  'Orange Juice',         'Food & Beverage',   4,  10,  1,  1,  TRUE,  4.00),
                (3,  'Potato Chips',         'Food & Beverage',  40,  15,  2,  1,  FALSE, 2.50),
                (4,  'Sparkling Water',      'Food & Beverage',   6,  20,  3,  1,  FALSE, 1.50),
                (5,  'Peanut Butter',        'Food & Beverage',  35,  10,  2,  1,  FALSE, 5.00),
                (6,  'Greek Yogurt',         'Food & Beverage',   3,  10,  1,  4,  TRUE,  3.00),
                (7,  'Organic Eggs',         'Food & Beverage',  50,  20,  1,  4,  TRUE,  4.30),
                (8,  'Sourdough Bread',      'Food & Beverage',   5,   8,  2,  4,  TRUE,  4.00),
                (9,  'Organic Butter',       'Food & Beverage',   2,  10,  1,  4,  TRUE,  6.50),
                (10, 'Almond Milk',          'Food & Beverage',  18,  10,  1,  4,  TRUE,  3.80),
                (11, 'Laundry Detergent',    'Home & Garden',    12,   5,  7,  2,  FALSE, 12.00),
                (12, 'Garden Hose',          'Home & Garden',     2,   5,  8,  2,  FALSE, 35.00),
                (13, 'Dish Soap',            'Home & Garden',    20,   8,  7,  2,  FALSE,  5.00),
                (14, 'Potting Soil',         'Home & Garden',     3,   8,  9,  2,  FALSE, 15.00),
                (15, 'Trash Bags 50pk',      'Home & Garden',     1,  10,  7,  2,  FALSE,  8.00),
                (16, 'Wireless Mouse',       'Electronics',      20,   8, 10,  3,  FALSE, 25.00),
                (17, 'USB-C Cable',          'Electronics',      45,  15, 10,  3,  FALSE,  3.50),
                (18, 'Bluetooth Speaker',    'Electronics',       4,  10, 11,  3,  FALSE, 45.00),
                (19, 'Phone Stand',          'Electronics',       6,   8, 11,  3,  FALSE,  3.00),
                (20, 'Wireless Charger',     'Electronics',       2,  10, 10,  3,  FALSE, 29.00),
                (21, 'White T-Shirt',        'Fashion',          30,  10, 12,  5,  FALSE,  6.00),
                (22, 'Blue Jeans',           'Fashion',           3,  10, 12,  5,  FALSE, 25.00),
                (23, 'Running Socks',        'Fashion',          50,  20, 13,  5,  FALSE,  2.50),
                (24, 'Baseball Cap',         'Fashion',           4,  15, 12,  5,  FALSE,  9.00),
                (25, 'Hoodie Sweater',       'Fashion',           2,  10, 12,  5,  FALSE, 35.00),
                (26, 'Vitamin C 1000mg',     'Supplements',      40,  15, 14,  9,  FALSE,  2.00),
                (27, 'Whey Protein',         'Supplements',      20,   8, 14,  9,  FALSE, 35.00),
                (28, 'Fish Oil Capsules',    'Supplements',       3,  10, 14,  9,  FALSE, 15.00),
                (29, 'Multivitamin 100ct',   'Supplements',       2,  10, 14,  9,  FALSE, 12.00),
                (30, 'Magnesium 200mg',      'Supplements',      25,  10, 14,  9,  FALSE,  8.00),
                (31, 'Paper Towels 6pk',     'Home & Garden',     5,  15,  7,  6,  FALSE,  9.00),
                (32, 'Instant Noodles 12pk', 'Food & Beverage',  60,  20,  2,  6,  FALSE,  5.50),
                (33, 'AA Batteries 20pk',    'Electronics',       3,  10, 10,  6,  FALSE,  8.00),
                (34, 'Basic White Socks',    'Fashion',          45,  20, 13,  6,  FALSE,  4.00),
                (35, 'Hand Soap 3pk',        'Home & Garden',     4,  12,  7,  6,  FALSE,  7.00),
                (36, 'Cereal Variety Pack',  'Food & Beverage',  30,  15,  2,  6,  FALSE,  6.50),
                (37, 'Ibuprofen 200ct',      'Supplements',       2,  10, 14,  6,  FALSE, 10.00),
                (38, 'Shampoo 2-in-1',       'Home & Garden',    15,   8,  7,  7,  FALSE,  7.50),
                (39, 'Frozen Pizza',         'Food & Beverage',   3,  10,  3,  7,  TRUE,   6.00),
                (40, 'Bluetooth Earbuds',    'Electronics',       2,  10, 11,  7,  FALSE, 39.00),
                (41, 'Yoga Pants',           'Fashion',          20,  10, 12,  7,  FALSE, 22.00),
                (42, 'All-Purpose Cleaner',  'Home & Garden',     4,  10,  7,  7,  FALSE,  5.00),
                (43, 'Granola Bars 12pk',    'Food & Beverage',  25,  15,  2,  7,  FALSE,  5.00),
                (44, 'Protein Bars 8pk',     'Supplements',       3,  10, 14,  7,  FALSE, 14.00),
                (45, 'Olive Oil 2L',         'Food & Beverage',  10,   5,  2,  8,  FALSE, 18.00),
                (46, 'Kirkland Coffee 2kg',  'Food & Beverage',   3,   8,  2,  8,  FALSE, 22.00),
                (47, 'Toilet Paper 30pk',    'Home & Garden',    12,  10,  7,  8,  FALSE, 25.00),
                (48, 'Mixed Nuts 1kg',       'Food & Beverage',   2,  10,  2,  8,  FALSE, 15.00),
                (49, 'Laundry Pods 120ct',   'Home & Garden',     5,   8,  7,  8,  FALSE, 20.00),
                (50, 'Vitamin D3 360ct',     'Supplements',      30,  15, 14,  8,  FALSE, 12.00),
                (51, 'Organic Spinach',      'Food & Beverage',   4,  10,  1, 10,  TRUE,   3.50),
                (52, 'Organic Avocados 4pk', 'Food & Beverage',   6,  10,  1, 10,  TRUE,   5.00),
                (53, 'Organic Apples 3lb',   'Food & Beverage',   2,  10,  1, 10,  TRUE,   4.50),
                (54, 'Organic Carrots 2lb',  'Food & Beverage',  15,  10,  1, 10,  TRUE,   2.80),
                (55, 'Organic Kombucha',     'Food & Beverage',   3,  10,  1, 10,  TRUE,   4.00);
                
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
        }
    }

    public void addSupplierDefault() throws SQLException {
        String sql = """
                INSERT INTO Supplier (supplierID, supplierName, address, phoneNumber) VALUES
                (1,  'FreshFarm Co.',       '123 Farm Rd, San Jose CA',          '4081110001'),
                (2,  'HomeGoods Ltd.',      '456 Market St, San Jose CA',        '4081110002'),
                (3,  'TechSupply Inc.',     '789 Silicon Ave, Santa Clara CA',   '4081110003'),
                (4,  'OrganicWorld',        '321 Green Blvd, Sunnyvale CA',      '4081110004'),
                (5,  'FashionDist Co.',     '654 Style Lane, San Francisco CA',  '4151110005'),
                (6,  'Walmart Wholesale',   '1000 Walmart Way, Bentonville AR',  '8001112222'),
                (7,  'Target Distribution', '1000 Nicollet Mall, Minneapolis MN','6121113333'),
                (8,  'Costco Wholesale',    '999 Lake Dr, Issaquah WA',          '4251114444'),
                (9,  'NutriPlus Supplies',  '500 Health Blvd, Austin TX',        '5121115555'),
                (10, 'GreenLeaf Organics',  '200 Eco Lane, Portland OR',         '5031116666');
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
        }
    }

    public void addOrderDefault() throws SQLException {
        String sql = """
                INSERT INTO Orders (orderID, supplierID, orderStatus, totalCost, orderDate, comment) VALUES
                (1,  1,  'DELIVERED', 237.50, '2026-03-01', 'Monthly dairy and beverage restock'),
                (2,  2,  'DELIVERED', 415.00, '2026-03-10', 'Home and garden monthly order'),
                (3,  3,  'SUBMITTED', 320.00, '2026-03-28', 'Electronics restock for Q2'),
                (4,  4,  'DELIVERED', 180.00, '2026-04-01', 'Organic produce and supplements'),
                (5,  5,  'SUBMITTED', 275.00, '2026-04-10', 'Spring fashion collection'),
                (6,  1,  'DELIVERED',  95.00, '2026-04-15', 'Urgent milk and eggs reorder'),
                (7,  3,  'SUBMITTED', 150.00, '2026-04-20', 'USB cables and accessories'),
                (8,  2,  'DRAFT',       0.00, '2026-04-28', 'Pending approval'),
                (9,  6,  'SUBMITTED', 520.00, '2026-04-22', 'Walmart bulk restock'),
                (10, 7,  'DELIVERED', 310.00, '2026-04-05', 'Target spring inventory'),
                (11, 8,  'SUBMITTED', 680.00, '2026-04-25', 'Costco bulk order'),
                (12, 10, 'DELIVERED', 195.00, '2026-04-12', 'GreenLeaf organic restock');
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();

        }
    }

    public void addOrderItemDefault() throws SQLException {
        String sql = """
                INSERT INTO OrderItem (orderID, productID, quantity, unitPrice, subTotal) VALUES
                (1,  1,  20,  3.50,  70.00),
                (1,  2,  15,  4.00,  60.00),
                (1,  7,  25,  4.30, 107.50),
                (2,  11, 10, 12.00, 120.00),
                (2,  12,  5, 35.00, 175.00),
                (2,  13, 12,  5.00,  60.00),
                (2,  14,  4, 15.00,  60.00),
                (3,  16, 10, 25.00, 250.00),
                (3,  17, 20,  3.50,  70.00),
                (4,   6, 20,  3.00,  60.00),
                (4,   8, 15,  4.00,  60.00),
                (4,  26, 30,  2.00,  60.00),
                (5,  21, 25,  6.00, 150.00),
                (5,  23, 50,  2.50, 125.00),
                (6,   1, 15,  3.50,  52.50),
                (6,   7, 10,  4.25,  42.50),
                (7,  17, 30,  3.50, 105.00),
                (7,  19, 15,  3.00,  45.00),
                (9,  31, 20,  9.00, 180.00),
                (9,  32, 30,  5.50, 165.00),
                (9,  33, 20,  8.00, 160.00),
                (10, 38, 20,  7.50, 150.00),
                (10, 41, 10, 22.00, 220.00),
                (11, 45, 15, 18.00, 270.00),
                (11, 47, 10, 25.00, 250.00),
                (11, 50, 20, 12.00, 240.00),
                (12, 51, 20,  3.50,  70.00),
                (12, 52, 25,  5.00, 125.00);
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
        }
    }

}
