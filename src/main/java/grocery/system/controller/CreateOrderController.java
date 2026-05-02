package grocery.system.controller;

import grocery.system.model.Order;
import grocery.system.model.Supplier;
import grocery.system.services.DatabaseAccessor;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

public class CreateOrderController {

    @FXML public TableView<Order>            orderHistoryTable;
    @FXML public DatePicker                  deliveryDatePicker;
    @FXML public ComboBox<String>            supplierComboBox;
    @FXML public TextArea                    commentTextArea;
    @FXML public TableColumn<Order, Integer> orderIdColumn;
    @FXML public TableColumn<Order, String>  supplierColumn;
    @FXML public TableColumn<Order, String>  deliveryDateColumn;
    @FXML public TableColumn<Order, String>  statusColumn;
    @FXML public TableColumn<Order, String>  commentColumn;

    private static final double APP_W = 1000;
    private static final double APP_H = 650;
    private static final String R = "/grocery/system/";
    private DatabaseAccessor db;
    private List<Supplier>   supplierList;
    private final ObservableList<Order> orderList = FXCollections.observableArrayList();

    @FXML
    public void initialize() throws SQLException {
        try {
            db = new DatabaseAccessor();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "DB Error", "Could not connect to database: " + e.getMessage());
            return;
        }
        setupTable();
        loadSuppliers();
        loadOrderHistory();
    }

    private void setupTable() {
        orderIdColumn.setCellValueFactory(
                cell -> new javafx.beans.property.SimpleIntegerProperty(
                        cell.getValue().getOrderID()).asObject());

        supplierColumn.setCellValueFactory(cell -> {
            int sid = cell.getValue().getSupplierID();
            String name = supplierList == null ? String.valueOf(sid) :
                    supplierList.stream()
                            .filter(s -> s.getSupplierID() == sid)
                            .map(Supplier::getSupplierName)
                            .findFirst()
                            .orElse(String.valueOf(sid));
            return new SimpleStringProperty(name);
        });

        deliveryDateColumn.setCellValueFactory(cell -> {
            Date d = cell.getValue().getOrderDate();
            String text = d == null ? "—" :
                    d.toInstant().atZone(ZoneId.systemDefault())
                            .toLocalDate().toString();
            return new SimpleStringProperty(text);
        });

        statusColumn.setCellValueFactory(
                cell -> new SimpleStringProperty(cell.getValue().getOrderStatus()));

        commentColumn.setCellValueFactory(
                cell -> new SimpleStringProperty(cell.getValue().getComment()));

        orderHistoryTable.setItems(orderList);
    }

    private void loadSuppliers() {
        try {
            supplierList = db.getAllSuppliers();
            List<String> names = supplierList.stream()
                    .map(Supplier::getSupplierName)
                    .toList();
            supplierComboBox.setItems(FXCollections.observableArrayList(names));
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Could not load suppliers: " + e.getMessage());
        }
    }

    private void loadOrderHistory() throws SQLException {
        List<Order> all = db.getAllOrders();
        System.out.println("Total orders fetched: " + all.size());
        List<Order> filtered = all.stream()
                .filter(o -> o.getOrderStatus().equals("SUBMITTED")
                        || o.getOrderStatus().equals("DELIVERED"))
                .toList();
        System.out.println("Filtered orders: " + filtered.size());
        orderList.setAll(filtered);
    }


    @FXML
    public void onSubmitOrder(ActionEvent actionEvent) throws SQLException {
        String supplierName    = supplierComboBox.getValue();
        LocalDate deliveryDate = deliveryDatePicker.getValue();

        if (supplierName == null) {
            showAlert(Alert.AlertType.WARNING, "Missing Field", "Please select a supplier.");
            return;
        }
        if (deliveryDate == null) {
            showAlert(Alert.AlertType.WARNING, "Missing Field", "Please select a delivery date.");
            return;
        }
        if (deliveryDate.isBefore(LocalDate.now())) {
            showAlert(Alert.AlertType.WARNING, "Invalid Date", "Delivery date cannot be in the past.");
            return;
        }

        int supplierID = supplierList.stream()
                .filter(s -> s.getSupplierName().equals(supplierName))
                .findFirst()
                .map(Supplier::getSupplierID)
                .orElse(-1);

        if (supplierID == -1) {
            showAlert(Alert.AlertType.ERROR, "Error", "Invalid supplier selected.");
            return;
        }

        Order order = new Order();
        order.setSupplierID(supplierID);
        order.setOrderDate(Date.from(deliveryDate.atStartOfDay(ZoneId.systemDefault()).toInstant()));
        order.setComment(commentTextArea.getText().trim());
        order.setOrderStatus("DRAFT");

        int newOrderID = db.addOrder(order);
        if (newOrderID == -1) {
            showAlert(Alert.AlertType.ERROR, "Error", "Failed to create order.");
            return;
        }
        order.setOrderID(newOrderID);
        openOrderDetailPage(actionEvent, order);
    }


    @FXML
    public void onClearForm(ActionEvent actionEvent) {
        supplierComboBox.setValue(null);
        deliveryDatePicker.setValue(null);
        commentTextArea.clear();
    }

    @FXML
    public void onSaveOrder(ActionEvent actionEvent) {
    }


    private void openOrderDetailPage(ActionEvent actionEvent, Order order) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(R + "OrderDetailPage.fxml"));
            Scene scene = new Scene(loader.load(), APP_W, APP_H);

            OrderDetailPageController controller = loader.getController();
            controller.setOrder(order);
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            stage.setTitle("Grocery Manager | Order #" + order.getOrderID());
            stage.setScene(scene);
            stage.centerOnScreen();
            stage.show();
        } catch (IOException e) {
            showAlert(Alert.AlertType.ERROR, "Navigation Error", "Could not open Order Detail page: " + e.getMessage());
        }
    }

    @FXML
    private void onGoToHomePage(ActionEvent e) {
        try {
            if (db != null) db.close();
            FXMLLoader loader = new FXMLLoader(getClass().getResource(R + "HomePage.fxml"));
            Scene scene = new Scene(loader.load(), APP_W, APP_H);
            Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
            stage.setTitle("Grocery Manager | Home");
            stage.setScene(scene);
            stage.setMinWidth(APP_W);
            stage.setMinHeight(APP_H);
            stage.centerOnScreen();
            stage.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}