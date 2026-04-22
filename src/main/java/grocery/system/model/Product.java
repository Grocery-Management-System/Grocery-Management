package grocery.system.model;

public class Product {
    private int productID;
    private String productName;
    private String category;
    private int currentStock;
    private int minThreshold;
    private int aisleNumber;
    private int supplierID;

    public Product() {}

    public int getProductID() {
        return productID;
    }

    public int getSupplierID() {
        return supplierID;
    }



    public String getProductName() {
        return productName;
    }

    public void setProductName(String name) {
        this.productName = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String name) {
        this.category = name;
    }

    public int getCurrentStock() {
        return currentStock;
    }

    public void setCurrentStock(int current) {
        this.currentStock = current;
    }

    public int getMinThreshold() {
        return minThreshold;
    }

    public void setMinThreshold(int threshold) {
        this.minThreshold = threshold;
    }

    public int getAisleNumber() {
        return aisleNumber;
    }

    public void setAisleNumber(int number) {
        this.aisleNumber = number;
    }
}
