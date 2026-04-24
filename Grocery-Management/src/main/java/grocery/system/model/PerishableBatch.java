package grocery.system.model;

import java.util.Date;

public class PerishableBatch {
    private int batchID;
    private int productID;
    private Date expirationDate;
    private int quantity;

    public PerishableBatch() {
    }

    public void setBatchID(int batchID) {
        this.batchID = batchID;
    }

    public int getBatchID() {
        return batchID;
    }
    public void setProductID(int productID) {this.productID = productID;}
    public int getProductID() {
        return productID;
    }
    public void setExpirationDate(Date expirationDate) {this.expirationDate = expirationDate;}
    public Date getExpirationDate() {return expirationDate;}
    public void setQuantity(int quantity) {this.quantity = quantity;}
    public int getQuantity() {return quantity;}
}

