package grocery.system.controller;

import grocery.system.view.HomePage;
import grocery.system.view.ProductPage;
import grocery.system.view.DefineProductPage;
import grocery.system.view.StoragePage;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;


public class HomePageController {
    //App window size
    private static final double APP_W = 1000;
    private static final double APP_H = 650;
    private static final String R = "/grocery/system/view/";


    public HomePageController() {}

    public void storageBtnAct(){
        StoragePage storagePage = new StoragePage();
        /*
        storagePage.show();
         */
    }

    public void exitBtnAct(){

    }


    public void onGoToProductPage(ActionEvent actionEvent) throws IOException {
        setScene((Stage)((Node)actionEvent.getSource()).getScene().getWindow(),
                R + "ProductPage.fxml","Product" );

    }

    public void onGoToCreateOrderPage(ActionEvent actionEvent) {
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
}
