package grocery.system.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;


public class HomePageController {



    public HomePageController() {}

    //App window size
    private static final double APP_W = 1000;
    private static final double APP_H = 650;
    private static final String R = "/grocery/system/";

    private void setScene(Stage stage, String fxml, String title) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(fxml));
        Scene scene = new Scene(loader.load(), APP_W, APP_H);
        stage.setTitle(title);
        stage.setScene(scene);
        stage.setMinWidth(APP_W);
        stage.setMinHeight(APP_H);
        stage.centerOnScreen();
        stage.show();
    }

    // Home -> ProductPage
    @FXML
    private void onGoToProductPage(ActionEvent e) throws IOException {
        setScene((Stage)((Node)e.getSource()).getScene().getWindow(),
                R + "ProductPage.fxml",
                "Grocery Manager | Product");
    }
    //Home -> CreateOrder
    @FXML
    private void onGoToCreateOrderPage(ActionEvent e) throws IOException {
        setScene((Stage)((Node)e.getSource()).getScene().getWindow(),
                R + "CreateOrderPage.fxml",
                "Grocery Manager | Create Order");
    }

    //Home -> LowStockPage
    @FXML
    private void onGoToLowStockPage(ActionEvent e) throws IOException {
        setScene((Stage) ((Node) e.getSource()).getScene().getWindow(),
                R + "LowStockPage.fxml",
                "Grocery Manager | Low Stock");
    }


}
