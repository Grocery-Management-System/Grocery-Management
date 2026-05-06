package grocery.system.controller;

import grocery.system.model.Product;
import grocery.system.model.Supplier;
import grocery.system.services.DatabaseAccessor;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Comparator;
import java.util.List;

public class ProductPageController {
    //App window size
    private static final double APP_W = 1000;
    private static final double APP_H = 650;
    private static final String R = "/grocery/system/";

    private DatabaseAccessor db;

    @FXML
    private TextField searchField;

    @FXML
    private ComboBox<String> categoryFilterComboBox;

    @FXML
    private ComboBox<String> sortComboBox;

    @FXML
    private TableView<Product> productTable;

    @FXML
    private TableColumn<Product, Integer> productIdColumn;

    @FXML
    private TableColumn<Product, String> productNameColumn;
    @FXML
    public TableColumn<Product,String> categoryColumn;
    @FXML
    private TableColumn<Product, String> supplierColumn;

    @FXML
    private TableColumn<Product, Integer> aisleColumn;

    @FXML
    private TableColumn<Product, String> perishableColumn;

    @FXML
    private TableColumn<Product, Integer> minThresholdColumn;
    @FXML
    public TableColumn<Product, Integer>  inStock;


    private final ObservableList<Product> productList = FXCollections.observableArrayList();
    private List<Supplier> supplierList;

    @FXML
    public void initialize() throws Exception {
        DatabaseAccessor db = DatabaseAccessor.getInstance();
        productIdColumn.setCellValueFactory(new PropertyValueFactory<>("productID"));
        productNameColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
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
        aisleColumn.setCellValueFactory(new PropertyValueFactory<>("aisleNumber"));
        perishableColumn.setCellValueFactory(new PropertyValueFactory<>("perishable"));
        inStock.setCellValueFactory(new PropertyValueFactory<>("currentStock"));
        minThresholdColumn.setCellValueFactory(new PropertyValueFactory<>("minThreshold"));

        try {
            db = DatabaseAccessor.getInstance();
            productList.addAll(db.getAllProducts());
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        productTable.setItems(productList);

        categoryFilterComboBox.getItems().addAll(
                "All",
                "Food & Beverage",
                "Home & Garden",
                "Electronics",
                "Fashion",
                "Supplements"
        );
        categoryFilterComboBox.setValue("All");

        sortComboBox.getItems().addAll(
                "Name A→Z",
                "Name Z→A",
                "Stock Low→High",
                "Stock High→Low",
                "Price Low→High",
                "Price High→Low",
                "Aisle Number"
        );
        sortComboBox.setOnAction(event-> applyFilters());

        categoryFilterComboBox.setOnAction(e -> applyFilters());
        try {
            supplierList = db.getAllSuppliers();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        refreshProductTable();
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
    @FXML
    private void onGoToHomePage(ActionEvent e) throws IOException {
        setScene((Stage) ((Node) e.getSource()).getScene().getWindow(),
                R + "HomePage.fxml",
                "Grocery Manager |Home");
    }

    @FXML
    public void onClearFilters() {
        searchField.clear();
        categoryFilterComboBox.setValue("All");
        sortComboBox.setValue(null);
        productTable.setItems(productList);
    }

    @FXML
    public void onAddProduct() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/grocery/system/DefineProductPage.fxml"));
            Parent root = loader.load();

            DefineProductPageController controller = loader.getController();
            controller.setOnProductSaved(this::refreshProductTable);

            Stage dialog = new Stage();
            dialog.setScene(new Scene(root));
            dialog.setResizable(false);
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.centerOnScreen();
            dialog.showAndWait();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @FXML
    public void onUpdateProduct(ActionEvent actionEvent) {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText(null);
            alert.setContentText("No product selected! Please select a product from the table first.");
            alert.showAndWait();
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/grocery/system/UpdateProductPage.fxml"));
            Parent root = loader.load();

            DefineProductPageController controller = loader.getController();
            controller.setOnProductSaved(this::refreshProductTable);
            controller.setProduct(selected);

            Stage dialog = new Stage();
            dialog.setScene(new Scene(root));
            dialog.setResizable(false);
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.centerOnScreen();
            dialog.showAndWait();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void onDeleteProduct(ActionEvent actionEvent) {
        Product selected = productTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setTitle("No Selection");
            alert.setHeaderText(null);
            alert.setContentText("No product selected! Please select a product from the table first.");
            alert.showAndWait();
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Delete Product");
        confirm.setHeaderText(null);
        confirm.setContentText("Are you sure you want to delete \"" + selected.getProductName() + "\"? This action cannot be undone.");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    db = DatabaseAccessor.getInstance();
                    db.deleteProduct(selected.getProductID());
                    refreshProductTable();
                } catch (SQLException e) {
                    Alert error = new Alert(Alert.AlertType.ERROR);
                    error.setTitle("Delete Failed");
                    error.setHeaderText(null);
                    error.setContentText("Could not delete product: " + e.getMessage());
                    error.showAndWait();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public void onSearch(ActionEvent actionEvent) {
        String keyword = searchField.getText().trim().toLowerCase();

        if (keyword.isEmpty()) {
            productTable.setItems(productList);
            return;
        }
        ObservableList<Product> filteredList = FXCollections.observableArrayList();
        for (Product product : productList) {
            if (String.valueOf(product.getProductID()).contains(keyword)
                    || product.getProductName().toLowerCase().contains(keyword)) {
                filteredList.add(product);
            }
        }
        productTable.setItems(filteredList);

    }
    private void applyFilters() {
        String selectedCategory = categoryFilterComboBox.getValue();
        String selectedSort     = sortComboBox.getValue();

        ObservableList<Product> filtered = FXCollections.observableArrayList();
        for (Product product : productList) {
            if (selectedCategory == null || selectedCategory.equals("All")) {
                filtered.add(product);
            } else if (product.getCategory().equals(selectedCategory)) {
                filtered.add(product);
            }
        }
        if (selectedSort != null) {
            switch (selectedSort) {
                case "Name A→Z"        -> filtered.sort((a, b) -> a.getProductName().compareToIgnoreCase(b.getProductName()));
                case "Name Z→A"        -> filtered.sort((a, b) -> b.getProductName().compareToIgnoreCase(a.getProductName()));
                case "Stock Low→High"  -> filtered.sort(Comparator.comparingInt(Product::getCurrentStock));
                case "Stock High→Low"  -> filtered.sort((a, b) -> Integer.compare(b.getCurrentStock(), a.getCurrentStock()));
                case "Price Low→High"  -> filtered.sort(Comparator.comparingDouble(Product::getUnitPrice));
                case "Price High→Low"  -> filtered.sort((a, b) -> Double.compare(b.getUnitPrice(), a.getUnitPrice()));
                case "Aisle Number"    -> filtered.sort(Comparator.comparingInt(Product::getAisleNumber));
            }
        }
        productTable.setItems(filtered);
    }
    private void refreshProductTable() {
        try {
            db = DatabaseAccessor.getInstance();
            productList.setAll(db.getAllProducts());
            productTable.setItems(productList);
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


}
