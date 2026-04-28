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

    public Product() {}

    public int getProductID() {
        return productID;
    }

    public void setProductID(int id) {
        this.productID = id;
    }


    public int getSupplierID() {
        return supplierID;
    }

    public void setSupplierID(int id) {
        this.supplierID = id;
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

    public void setIsPerishable(boolean isPerishable) {this.isPerishable = isPerishable;}
    public boolean isPerishable() {return isPerishable;}
}
