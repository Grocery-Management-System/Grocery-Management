package grocery.system.services;

import grocery.system.model.Order;
import grocery.system.model.OrderItem;
import grocery.system.model.Product;
import grocery.system.model.Supplier;
import java.sql.Date;
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

    public void addProduct(Product product) throws SQLException{
        String sql =
                "INSERT INTO Products (productID,productName, category, currentStock, minThreshold, aisleNumber,supplierID,isPerishable) " +
                "VALUES (?,?,?,?,?,?,?,?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, product.getProductID());
            ps.setString(2, product.getProductName());
            ps.setString(3, product.getCategory());
            ps.setInt(4, product.getCurrentStock());
            ps.setInt(5, product.getMinThreshold());
            ps.setInt(6, product.getAisleNumber());
            ps.setInt(7, product.getSupplierID());
            ps.setBoolean(8, product.isPerishable());

            ps.executeUpdate();
        }
    }
    public void addSupplier(Supplier supplier) throws SQLException{
        String sql =
                "INSERT INTO Suppliers (supplierID,supplierName,address,phoneNumber) " +
                        "VALUES (?,?,?,?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, supplier.getSupplierID());
            ps.setString(2, supplier.getSupplierName());
            ps.setString(3, supplier.getSupplierAddress());
            ps.setString(4, supplier.getSupplierPhone());

            ps.executeUpdate();
        }
    }
    public void addOrderItem(OrderItem orderItem) throws SQLException{
        String sql =
                "INSERT INTO OrderItems (orderItemID,orderID,productID,quantity,unitPrice) " +
                        "VALUES (?,?,?,?,?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, orderItem.getOrderItemID());
            ps.setInt(2, orderItem.getOrderID());
            ps.setInt(3, orderItem.getProductID());
            ps.setInt(4,orderItem.getQuantity());
            ps.setDouble(5, orderItem.getUnitPrice());

            ps.executeUpdate();
        }
    }
    public void addOrder(Order order) throws SQLException{
        String sql =
                "INSERT INTO Orders (orderID,supplierID,orderStatus,totalCost,orderDate,comment) " +
                        "VALUES (?,?,?,?,?,?)";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, order.getOrderID());
            ps.setInt(2, order.getSupplierID());
            ps.setString(3, order.getOrderStatus());
            ps.setDouble(4,order.getTotalCost());
            ps.setDate(5, (Date) order.getOrderDate());
            ps.setString(6, order.getComment());

            ps.executeUpdate();
        }
    }

}
