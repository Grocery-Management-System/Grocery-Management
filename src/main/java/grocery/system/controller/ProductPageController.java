package grocery.system.controller;

import grocery.system.model.Product;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.scene.control.TextField;
import java.io.IOException;

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

        productList.addAll(
                new Product(101, "Whole Milk", "Food & Beverage", 25, 10, 1, 1, true),
                new Product(102, "Potato Chips", "Food & Beverage", 40, 15, 2, 1, false),
                new Product(103, "Laundry Detergent", "Home & Garden", 12, 5, 7, 2, false),
                new Product(104, "Garden Hose", "Home & Garden", 8, 3, 8, 2, false),
                new Product(105, "Wireless Mouse", "Electronics", 20, 8, 10, 3, false)
        );

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

        categoryFilterComboBox.setOnAction(e -> applyFilters());    }


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

    public void onAddProduct(ActionEvent actionEvent) {
    }

    public void onUpdateProduct(ActionEvent actionEvent) {
    }

    public void onDeleteProduct(ActionEvent actionEvent) {
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




}
