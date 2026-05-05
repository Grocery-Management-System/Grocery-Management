package grocery.system.model;

public class Product {
    private int productID;
    private String productName;
    private String category;
    private int currentStock;
    private int minThreshold;
    private int aisleNumber;
    private int supplierID;
    private boolean isPerishable;
    private double unitPrice;

    public Product() {}

    public Product(int productID, String productName, String category, int currentStock, int minThreshold,int aisleNumber,int supplierID,boolean isPerishable) {
        this.productID = productID;
        this.productName = productName;
        this.category = category;
        this.currentStock = currentStock;
        this.minThreshold = minThreshold;
        this.aisleNumber = aisleNumber;
        this.supplierID = supplierID;
        this.isPerishable = isPerishable;
    }
    public int getProductID() {
        return productID;
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

    public int getSupplierID() {
        return supplierID;
    }

    public void setSupplierID(int supplierID) {
        this.supplierID = supplierID;
    }

    public void setIsPerishable(boolean isPerishable) {
        this.isPerishable = isPerishable;
    }

    public boolean isPerishable() {
        return isPerishable;
    }
    public double getUnitPrice() { return unitPrice; }
    public void setUnitPrice(double unitPrice) { this.unitPrice = unitPrice; }
}