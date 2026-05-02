package grocery.system.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

import grocery.system.model.Product;
import grocery.system.services.DatabaseAccessor;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

public class LowStockReportController {

    @FXML private TableView<Product> lowStockTable;
    @FXML private TableColumn<Product, String> nameColumn;
    @FXML private TableColumn<Product, Integer> stockColumn;
    @FXML private TableColumn<Product, Integer> thresholdColumn;
    @FXML private TableColumn<Product, Integer> aisleColumn;

    private final DatabaseAccessor db = new DatabaseAccessor();

    public LowStockReportController() throws SQLException {
    }

    @FXML
    public void initialize() throws SQLException {
        nameColumn.setCellValueFactory(new PropertyValueFactory<>("productName"));
        stockColumn.setCellValueFactory(new PropertyValueFactory<>("currentStock"));
        thresholdColumn.setCellValueFactory(new PropertyValueFactory<>("minThreshold"));
        aisleColumn.setCellValueFactory(new PropertyValueFactory<>("aisleNumber"));

        loadLowStockData();
    }

    private void loadLowStockData() throws SQLException {
        List<Product> allProducts = db.getAllProducts();

        ObservableList<Product> lowStockList = FXCollections.observableArrayList(
                allProducts.stream()
                        .filter(p -> p.getCurrentStock() <= p.getMinThreshold())
                        .collect(Collectors.toList())
        );

        lowStockTable.setItems(lowStockList);
    }
    @FXML
    private void onGoToHomePage(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/grocery/system/HomePage.fxml"));
            Scene scene = new Scene(loader.load());

            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}