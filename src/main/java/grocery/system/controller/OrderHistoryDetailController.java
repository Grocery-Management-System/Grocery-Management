package grocery.system.controller;

import grocery.system.model.Order;
import grocery.system.model.OrderItem;
import grocery.system.model.Product;
import grocery.system.model.Supplier;
import grocery.system.services.DatabaseAccessor;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.ZoneId;
import java.util.List;

public class OrderHistoryDetailController {

    @FXML private Label orderIdLabel;
    @FXML private Label orderDateLabel;
    @FXML private Label orderStatusLabel;
    @FXML private Label supplierLabel;
    @FXML private Label totalLabel;

    @FXML private TableView<OrderItem>           orderItemsTable;
    @FXML private TableColumn<OrderItem, Integer> productIdColumn;
    @FXML private TableColumn<OrderItem, String>  productNameColumn;
    @FXML private TableColumn<OrderItem, Integer> quantityColumn;
    @FXML private TableColumn<OrderItem, Double>  unitPriceColumn;
    @FXML private TableColumn<OrderItem, Double>  subTotalColumn;

    @FXML private Button deliveredButton;
    @FXML private Button cancelOrderButton;

    private DatabaseAccessor db;
    private Order currentOrder;
    private List<Product> allProducts;
    private Runnable onStatusChanged;

    private final ObservableList<OrderItem> itemList = FXCollections.observableArrayList();

    public void setOnStatusChanged(Runnable callback) {
        this.onStatusChanged = callback;
    }

    @FXML
    public void initialize() {
        try {
            db = new DatabaseAccessor();
            allProducts = db.getAllProducts();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "DB Error", "Could not connect: " + e.getMessage());
            return;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        setupTable();
    }

    private void setupTable() {
        productIdColumn.setCellValueFactory(
                cell -> new SimpleIntegerProperty(cell.getValue().getProductID()).asObject());
        productNameColumn.setCellValueFactory(cell -> {
            int pid = cell.getValue().getProductID();
            String name = allProducts == null ? String.valueOf(pid) :
                    allProducts.stream()
                            .filter(p -> p.getProductID() == pid)
                            .map(Product::getProductName)
                            .findFirst()
                            .orElse(String.valueOf(pid));
            return new SimpleStringProperty(name);
        });

        quantityColumn.setCellValueFactory(
                cell -> new SimpleIntegerProperty(cell.getValue().getQuantity()).asObject());

        unitPriceColumn.setCellValueFactory(
                cell -> new SimpleDoubleProperty(cell.getValue().getUnitPrice()).asObject());

        subTotalColumn.setCellValueFactory(
                cell -> new SimpleDoubleProperty(cell.getValue().getSubTotal()).asObject());

        orderItemsTable.setItems(itemList);
    }

    public void setOrder(Order order, List<Supplier> supplierList) {
        this.currentOrder = order;
        orderIdLabel.setText("Order #" + order.getOrderID());

        String dateText = order.getOrderDate() == null ? "—" :
                order.getOrderDate().toInstant()
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate().toString();
        orderDateLabel.setText("Date: " + dateText);
        orderStatusLabel.setText("Status: " + order.getOrderStatus());

        String supplierName = supplierList == null ? String.valueOf(order.getSupplierID()) :
                supplierList.stream()
                        .filter(s -> s.getSupplierID() == order.getSupplierID())
                        .map(Supplier::getSupplierName)
                        .findFirst()
                        .orElse(String.valueOf(order.getSupplierID()));
        supplierLabel.setText("Supplier: " + supplierName);
        String status = order.getOrderStatus();
        if (status.equals("DELIVERED") || status.equals("CANCELLED")) {
            deliveredButton.setDisable(true);
            cancelOrderButton.setDisable(true);
        }
        loadOrderItems();
    }

    private void loadOrderItems() {
        try {
            List<OrderItem> items = db.getItemsByOrder(currentOrder.getOrderID());
            itemList.setAll(items);
            double total = items.stream()
                    .mapToDouble(OrderItem::getSubTotal)
                    .sum();
            totalLabel.setText(String.format("Total: $%.2f", total));

        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Could not load order items: " + e.getMessage());
        }
    }

    @FXML
    private void onMarkDelivered() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Mark Delivered");
        confirm.setHeaderText(null);
        confirm.setContentText("Mark Order #" + currentOrder.getOrderID() + " as DELIVERED?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    db.updateOrderStatus(currentOrder.getOrderID(), "DELIVERED");
                    if (onStatusChanged != null) onStatusChanged.run();
                    showAlert(Alert.AlertType.INFORMATION, "Success",
                            "Order #" + currentOrder.getOrderID() + " marked as Delivered!");
                    closeDialog();
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Could not update order: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void onCancelOrder() {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Cancel Order");
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to cancel Order #" + currentOrder.getOrderID() + "?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    db.updateOrderStatus(currentOrder.getOrderID(), "CANCELLED");
                    if (onStatusChanged != null) onStatusChanged.run();
                    showAlert(Alert.AlertType.INFORMATION, "Cancelled",
                            "Order #" + currentOrder.getOrderID() + " has been cancelled.");
                    closeDialog();
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Could not cancel order: " + e.getMessage());
                }
            }
        });
    }

    @FXML
    private void onClose() {
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) orderItemsTable.getScene().getWindow();
        stage.close();
    }

    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}