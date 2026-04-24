package grocery.system.model;

public class Supplier {
    private int supplierID;
    private String supplierName;
    private String supplierAddress;
    private String supplierPhone;
    public Supplier(){}

    public int getSupplierID() {
        return supplierID;
    }
    public void setSupplierID(int supplierID) {this.supplierID = supplierID;}
    public String getSupplierName() {return supplierName;}
    public void setSupplierName(String supplierName) {this.supplierName = supplierName;}
    public String getSupplierAddress() {return supplierAddress;}
    public void setSupplierAddress(String supplierAddress) {this.supplierAddress = supplierAddress;}
    public String getSupplierPhone() {return supplierPhone;}
    public void setSupplierPhone(String supplierPhone) {this.supplierPhone = supplierPhone;}
}


