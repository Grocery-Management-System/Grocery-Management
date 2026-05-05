package grocery.system.controller;

import grocery.system.model.Supplier;
import grocery.system.services.DatabaseAccessor;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.sql.SQLException;

public class SupplierPageController {
    private static final double APP_W = 1000;
    private static final double APP_H = 650;

    @FXML private TableView<Supplier> supplierTable;
    @FXML private TableColumn<Supplier, Integer> idColumn;
    @FXML private TableColumn<Supplier, String> nameColumn;
    @FXML private TableColumn<Supplier, String> addressColumn;
    @FXML private TableColumn<Supplier, String> phoneColumn;

    private final ObservableList<Supplier> supplierList = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("supplierID"));
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("supplierName"));
        addressColumn.setCellValueFactory(new PropertyValueFactory<>("supplierAddress"));
        phoneColumn.setCellValueFactory(new PropertyValueFactory<>("supplierPhone"));

        refreshTable();
    }

    private void refreshTable() {
        try (DatabaseAccessor db = new DatabaseAccessor()) {
            supplierList.setAll(db.getAllSuppliers());
            supplierTable.setItems(supplierList);
        } catch (SQLException e) {
            showError("Database Error", "Could not load suppliers: " + e.getMessage());
        }
    }

    @FXML
    private void onGoToHomePage(ActionEvent e) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/grocery/system/HomePage.fxml"));
        Scene scene = new Scene(loader.load(), APP_W, APP_H);
        Stage stage = (Stage) ((Node) e.getSource()).getScene().getWindow();
        stage.setScene(scene);
    }

    private void showError(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
    @FXML
    public void onAddSupplier() {
        Dialog<Supplier> dialog = new Dialog<>();
        dialog.setTitle("Add New Supplier");
        dialog.setHeaderText("Enter the details for the new supplier.");

        ButtonType saveButtonType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(saveButtonType, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField name = new TextField();
        name.setPromptText("Supplier Name");
        TextField address = new TextField();
        address.setPromptText("Address");
        TextField phone = new TextField();
        phone.setPromptText("Phone Number");

        grid.add(new Label("Name:"), 0, 0);
        grid.add(name, 1, 0);
        grid.add(new Label("Address:"), 0, 1);
        grid.add(address, 1, 1);
        grid.add(new Label("Phone:"), 0, 2);
        grid.add(phone, 1, 2);

        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == saveButtonType) {
                Supplier s = new Supplier();
                s.setSupplierName(name.getText());
                s.setSupplierAddress(address.getText());
                s.setSupplierPhone(phone.getText());
                return s;
            }
            return null;
        });

        dialog.showAndWait().ifPresent(newSupplier -> {
            try (DatabaseAccessor db = new DatabaseAccessor()) {
                db.addSupplier(newSupplier);
                refreshTable();
            } catch (SQLException e) {
                showAlert(Alert.AlertType.ERROR, "Database Error", "Could not save supplier: " + e.getMessage());
            }
        });
    }

    @FXML
    public void onDeleteSupplier() {
        Supplier selected = supplierTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.ERROR, "No Selection", "Please select a supplier to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete " + selected.getSupplierName() + "?");
        confirm.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try (DatabaseAccessor db = new DatabaseAccessor()) {
                    db.deleteSupplier(selected.getSupplierID());
                    refreshTable();
                } catch (SQLException e) {
                    showAlert(Alert.AlertType.ERROR, "Error", "Could not delete: " + e.getMessage());
                }
            }
        });
    }


    private void showAlert(Alert.AlertType error, String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

}