package grocery.system.model;

public class OrderItem {
    int orderItemID;
    int orderID;
    int productID;
    int quantity;
    double unitPrice;
    double subTotal;
    public OrderItem() {
    }

    public void setOrderID(int orderID) {
        this.orderID = orderID;
    }

    public int getOrderID() {
        return orderID;
    }
    public void setOrderItemID(int orderItemID) {
        this.orderItemID = orderItemID;
    }

    public void setSubTotal() {this.subTotal = unitPrice * quantity;}
    public double getSubTotal() {return subTotal;}
    public int getProductID() {return productID;}
    public void setProductID(int productID) {this.productID = productID;}
    public int getQuantity() {return quantity;}
    public void setQuantity(int quantity) {this.quantity = quantity;}
    public double getUnitPrice() {return unitPrice;}
    public void setUnitPrice(double unitPrice) {this.unitPrice = unitPrice;}

}