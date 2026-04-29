package grocery.system.model;

import grocery.system.services.DatabaseAccessor;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class Main extends Application {
    private static final double APP_W = 1000;
    private static final double APP_H = 650;

    @Override
    public void start(Stage stage) throws IOException {
        try {
            DatabaseAccessor db = new DatabaseAccessor();
            System.out.println("Connected successfully!");
            db.close();
        } catch (Exception e) {
            System.out.println("Connection failed: " + e.getMessage());
        }
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
}
