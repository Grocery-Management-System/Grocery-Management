package grocery.system.controller;

import grocery.system.model.Order;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDate;

public class
CreateOrderController {
    public TableView<Order> orderTable;
    public DatePicker deliveryDatePicker;
    public ComboBox<String> supplierComboBox;
    public TextArea commentTextArea;
    public TableView<Order> orderHistoryTable;

    public TableColumn<Order, Integer> orderIdColumn;
    public TableColumn<Order, String> supplierColumn;
    public TableColumn<Order, LocalDate> deliveryDateColumn; //may need to fix this one
    public TableColumn<Order, String> statusColumn;
    public TableColumn<Order, String> commentColumn;
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
    @FXML
    private void onGoToHomePage(ActionEvent e) throws IOException {
        setScene((Stage)((Node)e.getSource()).getScene().getWindow(),
                R + "HomePage.fxml",
                "Grocery Manager |Home");
    }

    public void onCreateOrder(ActionEvent actionEvent) {

    }

    public void onClearForm(ActionEvent actionEvent) {
    }

    public void onSubmitOrder(ActionEvent actionEvent) {
    }

    public void onSaveOrder(ActionEvent actionEvent) {

    }
}
