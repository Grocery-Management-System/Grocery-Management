package grocery.system.model;

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
        grocery.system.services.DatabaseAccessor db = new grocery.system.services.DatabaseAccessor();
        try {
            db.initDatabase();
        } catch (Exception e) {
            System.err.println("DB Init Failed: " + e.getMessage());
            e.printStackTrace();
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
