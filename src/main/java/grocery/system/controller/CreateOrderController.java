package grocery.system.controller;

import grocery.system.model.Order;
import grocery.system.services.DatabaseAccessor;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.controlsfx.control.action.Action;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public class CreateOrderController {
    public DatePicker deliveryDatePicker;
    public ComboBox<String> supplierComboBox;
    public TextArea commentTextArea;
    public TableView<Order> orderHistoryTable;

    public TableColumn<Order, Integer> orderIdColumn;
    public TableColumn<Order, String> supplierColumn;
    public TableColumn<Order, LocalDate> deliveryDateColumn;
    public TableColumn<Order, String> statusColumn;
    public TableColumn<Order, String> commentColumn;
    private static final double APP_W = 1000;
    private static final double APP_H = 650;
    private static final String R = "/grocery/system/";
    @FXML
    public void onClearForm(ActionEvent actionEvent){
        deliveryDatePicker.setValue(null);
        supplierComboBox.getSelectionModel().clearSelection();
        commentTextArea.clear();
        System.out.println("Form cleared.");
    }

    @FXML
    public void onSaveOrder(ActionEvent actionEvent){
        processOrder("DRAFT");
    }

    @FXML
    public void onSubmitOrder(ActionEvent actionEvent){
        processOrder("SUBMITTED");
    }

    private void processOrder(String status){
        Order newOrder = new Order();

        newOrder.setSupplierID(1);
        newOrder.setOrderStatus(status);
        newOrder.setComment(commentTextArea.getText());
        newOrder.setTotalCost(0.0);
        newOrder.setOrderDate(new Date());

        try {
            DatabaseAccessor db = new DatabaseAccessor();
            db.saveOrder(newOrder);

            Alert alert = new Alert(Alert.AlertType.INFORMATION, "Order " + status + " successfully!");
            alert.showAndWait();
            //would create a clear form after the success
            onClearForm(null);
        } catch(SQLException e){
            new Alert(Alert.AlertType.ERROR, "Database Error: " + e.getMessage()).show();
            e.printStackTrace();
        }
    }
    @FXML
    private void onGoToHomePage (ActionEvent e) throws IOException {
        setScene((Stage) ((Node)e.getSource()).getScene().getWindow(), R + "HomePage.fxml", "Grocery Manager | Home");
    }
    @FXML
    public void initialize() {
        orderIdColumn.setCellValueFactory(new PropertyValueFactory<>("orderID"));
        statusColumn.setCellValueFactory(new PropertyValueFactory<>("orderStatus"));
        commentColumn.setCellValueFactory(new PropertyValueFactory<>("comment"));
        //supplierColumn and deliveryDateColumn need specific mapping or custom cells

        refreshData();
    }

    private void refreshData() {
        try {
            DatabaseAccessor db = new DatabaseAccessor();

            List<String> suppliers = db.getAllSuppliers();
            supplierComboBox.setItems(FXCollections.observableArrayList(suppliers));

            List<Order> history = db.getAllOrders();
            orderHistoryTable.setItems(FXCollections.observableArrayList(history));

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void onDeleteOrder() {
        Order selected = orderHistoryTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            try {
                new DatabaseAccessor().deleteOrder(selected.getOrderID());
                refreshData(); // Update the table after deletion
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
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
