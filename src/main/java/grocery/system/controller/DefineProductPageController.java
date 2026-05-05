package grocery.system.controller;

import grocery.system.model.Product;
import grocery.system.model.Supplier;
import grocery.system.services.DatabaseAccessor;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.util.List;

public class DefineProductPageController {

    @FXML private TextField    productNameField;
    @FXML private ComboBox<String> categoryComboBox;
    @FXML private ComboBox<String> supplierComboBox;
    @FXML private TextField    currentStockField;
    @FXML private TextField    minThresholdField;
    @FXML private TextField    aisleNumberField;
    @FXML private CheckBox     perishableCheckBox;
    @FXML private Label        errorLabel;
    @FXML private Label        titleLabel;
    @FXML private Button       saveButton;

    private DatabaseAccessor db;
    private Product productToUpdate = null;
    private List<Supplier> supplierList;
    private Runnable onProductSaved;
    public void setOnProductSaved(Runnable callback) {
        this.onProductSaved = callback;
    }


    @FXML
    public void initialize() {
        try {
            db = new DatabaseAccessor();
        } catch (SQLException e) {
            showError("Could not connect to database: " + e.getMessage());
            return;
        }

        categoryComboBox.setItems(FXCollections.observableArrayList(
                "Food & Beverage",
                "Home & Garden",
                "Electronics",
                "Fashion",
                "Supplements"
        ));

        loadSuppliers();
    }

    private void loadSuppliers() {
        try {
            supplierList = db.getAllSuppliers();
            List<String> names = supplierList.stream()
                    .map(Supplier::getSupplierName)
                    .toList();
            supplierComboBox.setItems(FXCollections.observableArrayList(names));
        } catch (SQLException e) {
            showError("Could not load suppliers: " + e.getMessage());
        }
    }


    public void setProduct(Product product) {
        // Store the product — this also serves as the "update mode" flag
        this.productToUpdate = product;

        // Change header and button text
        if (titleLabel != null) titleLabel.setText("Update Product");
        if (saveButton  != null) saveButton.setText("Save Changes");

        // Pre-fill text fields
        productNameField.setText(product.getProductName());
        currentStockField.setText(String.valueOf(product.getCurrentStock()));
        minThresholdField.setText(String.valueOf(product.getMinThreshold()));
        aisleNumberField.setText(String.valueOf(product.getAisleNumber()));
        perishableCheckBox.setSelected(product.isPerishable());

        // Pre-fill category — value must match one of the ComboBox items exactly
        categoryComboBox.setValue(product.getCategory());

        // Pre-fill supplier — find the supplier name that matches this product's supplierID
        if (supplierList != null) {
            supplierList.stream()
                    .filter(s -> s.getSupplierID() == product.getSupplierID())
                    .findFirst()
                    .ifPresent(s -> supplierComboBox.setValue(s.getSupplierName()));
        }
    }


    @FXML
    private void onSaveProduct() {
        // ── Read and validate fields ───────────────────────────────────
        String name         = productNameField.getText().trim();
        String category     = categoryComboBox.getValue();
        String supplierName = supplierComboBox.getValue();
        String stockText     = currentStockField.getText().trim();
        String thresholdText = minThresholdField.getText().trim();
        String aisleText     = aisleNumberField.getText().trim();

        if (name.isEmpty() || category == null || supplierName == null
                || stockText.isEmpty() || thresholdText.isEmpty() || aisleText.isEmpty()) {
            showError("Please fill in all required fields.");
            return;
        }

        int currentStock, minThreshold, aisleNumber;
        try {
            currentStock = Integer.parseInt(stockText);
            minThreshold = Integer.parseInt(thresholdText);
            aisleNumber  = Integer.parseInt(aisleText);
        } catch (NumberFormatException e) {
            showError("Stock, threshold, and aisle must be whole numbers.");
            return;
        }

        if (currentStock < 0 || minThreshold < 0 || aisleNumber < 0) {
            showError("Stock, threshold, and aisle cannot be negative.");
            return;
        }

        int supplierID = supplierList.stream()
                .filter(s -> s.getSupplierName().equals(supplierName))
                .findFirst()
                .map(Supplier::getSupplierID)
                .orElse(-1);

        if (supplierID == -1) {
            showError("Invalid supplier selected.");
            return;
        }

        try {
            if (productToUpdate != null) {
                // UPDATE MODE — reuse the existing product (keeps its productID intact)
                productToUpdate.setProductName(name);
                productToUpdate.setCategory(category);
                productToUpdate.setCurrentStock(currentStock);
                productToUpdate.setMinThreshold(minThreshold);
                productToUpdate.setAisleNumber(aisleNumber);
                productToUpdate.setSupplierID(supplierID);
                productToUpdate.setIsPerishable(perishableCheckBox.isSelected());
                db.updateProduct(productToUpdate);  // UPDATE WHERE productID = productToUpdate.getProductID()

            } else {
                // ADD MODE — create a fresh product (productID assigned by DB)
                Product newProduct = new Product();
                newProduct.setProductName(name);
                newProduct.setCategory(category);
                newProduct.setCurrentStock(currentStock);
                newProduct.setMinThreshold(minThreshold);
                newProduct.setAisleNumber(aisleNumber);
                newProduct.setSupplierID(supplierID);
                newProduct.setIsPerishable(perishableCheckBox.isSelected());
                db.addProduct(newProduct);
            }

            db.close();
            if (onProductSaved != null) onProductSaved.run();
            closeDialog();

        } catch (SQLException e) {
            showError("Failed to save: " + e.getMessage());
        }
    }


    @FXML
    private void onCancel() {
        closeDialog();
    }
    private void showError(String message) {
        errorLabel.setText(message);
        errorLabel.setVisible(true);
        errorLabel.setManaged(true);
    }

    private void closeDialog() {
        Stage stage = (Stage) productNameField.getScene().getWindow();
        stage.close();
    }
}