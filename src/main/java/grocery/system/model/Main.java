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
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("/grocery.system/view/HomePage.fxml"));
        Scene scene = new Scene(loader.load(), 1000, 650);
        stage.setScene(scene);

        stage.setMinWidth(APP_W);
        stage.setMinHeight(APP_H);

        stage.setResizable(false);
        stage.centerOnScreen();
        stage.show();
    }
}
