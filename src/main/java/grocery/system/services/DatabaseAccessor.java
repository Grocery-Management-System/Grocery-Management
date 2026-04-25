package grocery.system.services;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

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
                                                        
                        )
                        
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
                            
                        )
            
            """);
            /*creates table OrderItem. it seems it doesn't detect order and product tables yet
            i will find a way to make sure those tables are actually included next time
            */
        }
    }

}
