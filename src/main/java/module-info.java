module grocery.system {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql; // Essential for the MySQL/JDBC connection in your proposal [cite: 54, 80]

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires javafx.graphics;
    requires javafx.base;
    requires java.desktop;

    // Controllers need to be 'opened' so JavaFX can link them to FXML files
    opens grocery.system.controller to javafx.fxml;
    exports grocery.system.controller;

    // Views and Models need to be exported so the rest of the app can use them
    exports grocery.system.view;
    opens grocery.system.view to javafx.fxml;

    exports grocery.system.model; // Add this so your database can use the Product class [cite: 25]
}