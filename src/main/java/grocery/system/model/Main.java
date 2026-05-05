package grocery.system.model;

import grocery.system.services.DatabaseAccessor;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;


public class Main extends Application {
    private static final double APP_W = 1000;
    private static final double APP_H = 650;

    @Override
    public void start(Stage stage) throws Exception {
        try {
            DatabaseAccessor db = new DatabaseAccessor();
            db.initDatabase();
        } catch (Exception e) {
            System.err.println("Database setup failed: " + e.getMessage());
            return;
        }

        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("HomePage.fxml"));

        var url = Main.class.getResource("/grocery/system/HomePage.fxml");
        System.out.println("URL = " + url);

        FXMLLoader loader = new FXMLLoader(url);
        Scene scene = new Scene(loader.load(), APP_W, APP_H);
        stage.setScene(scene);

        stage.setMinWidth(APP_W);
        stage.setMinHeight(APP_H);
        stage.setResizable(false);
        stage.centerOnScreen();
        stage.show();
    }

    @Override
    public void stop() {
        try {
            DatabaseAccessor.getInstance().close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
