package grocery.system.view;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;

public class HomePage {
    //App window size
    private static final double APP_W = 1000;
    private static final double APP_H = 650;
    private static final String R = "/grocery/system/";
    public HomePage(){




        //above here you implement the buttons and whatever designs you want

        VBox pageLayout = new VBox(); // this will order the page layout
        pageLayout.setSpacing(30);
        BorderPane root = new BorderPane(pageLayout); //the box will be put into the scene
        Scene pageScene = new Scene(root, 1280,960); //which is initialized here

    }
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
                "Grocery Manager |Product");
    }
    //Home -> CreateOrder
    @FXML
    private void onGoToCreateOrderPage(ActionEvent e) throws IOException {
        setScene((Stage)((Node)e.getSource()).getScene().getWindow(),
                R + "CreateOrderPage.fxml",
                "Grocery Manager |Create Order");
    }

    //Home -> LowStockPage
    public void onGoToLowStockPage(ActionEvent actionEvent) {
    }
}
