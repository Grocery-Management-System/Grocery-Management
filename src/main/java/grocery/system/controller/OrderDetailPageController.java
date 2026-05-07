package grocery.system.controller;

import grocery.system.model.Order;
import grocery.system.model.OrderItem;
import grocery.system.model.Product;
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
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.List;

public class OrderDetailPageController {

    @FXML private TextField searchField;
    @FXML private CheckBox lowStockCheckBox;
    @FXML
    private ComboBox<String>   categoryComboBox;
    @FXML private ComboBox<String> perishableComboBox;
    @FXML private Spinner<Integer> quantitySpinner;

    // ── Available products table ───────────────────────────────────────────
    @FXML private TableView<Product> availableProductsTable;
    @FXML private TableColumn<Product, Integer> productIdColumn;
    @FXML private TableColumn<Product, String> productNameColumn;
    @FXML private TableColumn<Product, String>  categoryColumn;
    @FXML private TableColumn<Product, Integer> stockColumn;
    @FXML private TableColumn<Product, String>  unitPriceColumn;
    @FXML private TableColumn<Product, Integer> aisleColumn;
    @FXML private TableColumn<Product, String>  perishableColumn;

    // ── Order items table ──────────────────────────────────────────────────
    @FXML private TableView<OrderItem>           orderItemsTable;
    @FXML private TableColumn<OrderItem, Integer> submittedProductIdColumn;
    @FXML private TableColumn<OrderItem, String>  submittedProductNameColumn;
    @FXML private TableColumn<OrderItem, Integer> submittedQuantityColumn;
    @FXML private TableColumn<OrderItem, Double>  submittedUnitPriceColumn;
    @FXML private TableColumn<OrderItem, Double>  submittedSubTotalColumn;

    private DatabaseAccessor db;
    private Order currentOrder;
    private List<Product> allProducts;
    private final ObservableList<Product>   productList   = FXCollections.observableArrayList();
    private final ObservableList<OrderItem> orderItemList = FXCollections.observableArrayList();
    private static final double APP_W = 1000;
    private static final double APP_H = 650;
    private static final String R = "/grocery/system/";

    @FXML
    public void initialize() {
        try {
            db = DatabaseAccessor.getInstance();
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "DB Error", "Could not connect: " + e.getMessage());
            return;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        setupAvailableProductsTable();
        setupOrderItemsTable();
        setupFilters();
    }
    public void setOrder(Order order) {
        this.currentOrder = order;
        loadAllProducts();
    }

    @FXML
    public void onSearchProduct(ActionEvent actionEvent) {
        String keyword      = searchField.getText().trim().toLowerCase();
        String category     = categoryComboBox.getValue();
        String perishable   = perishableComboBox.getValue();

        List<Product> filtered = allProducts.stream()
                .filter(p -> {
                    if (!keyword.isEmpty()) {
                        boolean matchesId   = String.valueOf(p.getProductID()).contains(keyword);
                        boolean matchesName = p.getProductName().toLowerCase().contains(keyword);
                        if (!matchesId && !matchesName) return false;
                    }
                    if (category != null && !category.equals("All")) {
                        if (!p.getCategory().equals(category)) return false;
                    }
                    if (perishable != null && !perishable.equals("All")) {
                        boolean wantsPerishable = perishable.equals("Yes");
                        if (p.isPerishable() != wantsPerishable) return false;
                    }
                    if (lowStockCheckBox.isSelected()) {
                        return p.getCurrentStock() < p.getMinThreshold();
                    }
                    return true;
                })
                .toList();

        if (filtered.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "No Results",
                    "No products found matching the search criteria.");
            productList.setAll(allProducts);
        } else {
            productList.setAll(filtered);
        }
    }

    @FXML
    public void onClearFilters(ActionEvent actionEvent) {
        searchField.clear();
        categoryComboBox.setValue("All");
        perishableComboBox.setValue("All");
        lowStockCheckBox.setSelected(false);
        quantitySpinner.getValueFactory().setValue(1);
        productList.setAll(allProducts);
    }
    @FXML
    public void onAddToOrder(ActionEvent actionEvent) {
        Product selected = availableProductsTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "No Selection",
                    "Please select a product from the table first.");
            return;
        }

        int quantity = quantitySpinner.getValue();

        // Check if this product is already in the order — if so, update quantity
        for (OrderItem existing : orderItemList) {
            if (existing.getProductID() == selected.getProductID()) {
                existing.setQuantity(existing.getQuantity() + quantity);
                existing.setSubTotal();
                orderItemsTable.refresh();
                return;
            }
        }

        // New order item
        OrderItem item = new OrderItem();
        item.setProductID(selected.getProductID());
        item.setQuantity(quantity);

        //specifically for unit price: make it look nice by using decimal format
        DecimalFormat df = new DecimalFormat("#.00");
        double unitPrice = selected.getUnitPrice();
        unitPrice = Double.parseDouble(df.format(unitPrice));
        item.setUnitPrice(unitPrice);

        item.setSubTotal();
        if (currentOrder != null) {
            item.setOrderID(currentOrder.getOrderID());
        }

        orderItemList.add(item);
    }
    @FXML
    public void onClearOrder(ActionEvent actionEvent) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Clear Order");
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to clear all items from this order?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                orderItemList.clear();
            }
        });
    }

    private void setupFilters() {
        categoryComboBox.setItems(FXCollections.observableArrayList(
                "All", "Food & Beverage", "Home & Garden",
                "Electronics", "Fashion", "Supplements"
        ));
        categoryComboBox.setValue("All");

        // Perishable dropdown
        perishableComboBox.setItems(FXCollections.observableArrayList(
                "All", "Yes", "No"
        ));
        perishableComboBox.setValue("All");
        quantitySpinner.setValueFactory(
                new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 999, 1));
    }

    private void loadAllProducts() {
        try {
            allProducts = db.getProductsBySupplier(currentOrder.getSupplierID());
            productList.setAll(allProducts);
        } catch (SQLException e) {
            showAlert(Alert.AlertType.ERROR, "Error", "Could not load products: " + e.getMessage());
        }
    }
    private void setupAvailableProductsTable() {
        productIdColumn.setCellValueFactory(new PropertyValueFactory<>("productID"));
        productNameColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        stockColumn.setCellValueFactory(new PropertyValueFactory<>("currentStock"));
        aisleColumn.setCellValueFactory(new PropertyValueFactory<>("aisleNumber"));
        unitPriceColumn.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        perishableColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().isPerishable() ? "Yes" : "No"));

        availableProductsTable.setItems(productList);
    }
    private void setupOrderItemsTable() {
        submittedProductIdColumn.setCellValueFactory(new PropertyValueFactory<>("productID"));
        submittedQuantityColumn.setCellValueFactory(new PropertyValueFactory<>("quantity"));
        submittedUnitPriceColumn.setCellValueFactory(new PropertyValueFactory<>("unitPrice"));
        submittedSubTotalColumn.setCellValueFactory(new PropertyValueFactory<>("subTotal"));

        // Show product name by looking up productID in allProducts
        submittedProductNameColumn.setCellValueFactory(cell -> {
            int pid = cell.getValue().getProductID();
            String name = allProducts == null ? String.valueOf(pid) :
                    allProducts.stream()
                            .filter(p -> p.getProductID() == pid)
                            .map(Product::getProductName)
                            .findFirst()
                            .orElse(String.valueOf(pid));
            return new SimpleStringProperty(name);
        });

        orderItemsTable.setItems(orderItemList);
    }
    @FXML
    public void onSubmitOrder(ActionEvent actionEvent) {
        if (orderItemList.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Empty Order",
                    "Please add at least one product before submitting.");
            return;
        }

        try {
            db = DatabaseAccessor.getInstance();
            for (OrderItem item : orderItemList) {
                db.addOrderItem(item);
            }
            db.updateOrderStatus(currentOrder.getOrderID(), "SUBMITTED");
            showAlert(Alert.AlertType.INFORMATION, "Order Submitted",
                    "Order #" + currentOrder.getOrderID() + " has been submitted successfully!");
            onBack(actionEvent);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    public void onBack(ActionEvent e) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                getClass().getResource(R + "CreateOrderPage.fxml"));
        Scene scene = new Scene(loader.load(), APP_W, APP_H);
        Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
        stage.setTitle("Grocery Manager | Create Order");
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.show();
    }
    private void showAlert(Alert.AlertType type, String title, String message) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
