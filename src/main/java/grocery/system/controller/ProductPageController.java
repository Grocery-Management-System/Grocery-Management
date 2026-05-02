package grocery.system.controller;

import grocery.system.model.Product;
import grocery.system.services.DatabaseAccessor;
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

public class ProductPageController {
    //App window size
    private static final double APP_W = 1000;
    private static final double APP_H = 650;
    private static final String R = "/grocery/system/";

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

    private final ObservableList<Product> productList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        productIdColumn.setCellValueFactory(new PropertyValueFactory<>("productID"));
        productNameColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        categoryColumn.setCellValueFactory(new PropertyValueFactory<>("category"));
        supplierColumn.setCellValueFactory(new PropertyValueFactory<>("supplierID"));
        aisleColumn.setCellValueFactory(new PropertyValueFactory<>("aisleNumber"));
        perishableColumn.setCellValueFactory(new PropertyValueFactory<>("perishable"));
        minThresholdColumn.setCellValueFactory(new PropertyValueFactory<>("minThreshold"));

        try {
            DatabaseAccessor db = new DatabaseAccessor();
            productList.addAll(db.getAllProducts());
            db.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }        productTable.setItems(productList);

        categoryFilterComboBox.getItems().addAll(
                "All",
                "Food & Beverage",
                "Home & Garden",
                "Electronics",
                "Fashion",
                "Supplements"
        );
        categoryFilterComboBox.setValue("All");

        categoryFilterComboBox.setOnAction(e -> applyFilters());
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
    public void onClearFilters(ActionEvent actionEvent) {
        searchField.clear();
        categoryFilterComboBox.setValue("All");
        sortComboBox.setValue(null);
        productTable.setItems(productList);
    }

    @FXML
    public void onAddProduct(ActionEvent actionEvent) {
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
                    DatabaseAccessor db = new DatabaseAccessor();
                    db.deleteProduct(selected.getProductID());
                    refreshProductTable();
                } catch (SQLException e) {
                    Alert error = new Alert(Alert.AlertType.ERROR);
                    error.setTitle("Delete Failed");
                    error.setHeaderText(null);
                    error.setContentText("Could not delete product: " + e.getMessage());
                    error.showAndWait();
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

        ObservableList<Product> filteredList = FXCollections.observableArrayList();
        for (Product product : productList) {
            if (selectedCategory == null || selectedCategory.equals("All")) {
                filteredList.add(product);
            } else if (product.getCategory().equals(selectedCategory)) {
                filteredList.add(product);
            }
        }
        productTable.setItems(filteredList);
    }
    private void refreshProductTable() {
        try {
            DatabaseAccessor db = new DatabaseAccessor();
            productList.setAll(db.getAllProducts());
            db.close();
            productTable.setItems(productList);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}
