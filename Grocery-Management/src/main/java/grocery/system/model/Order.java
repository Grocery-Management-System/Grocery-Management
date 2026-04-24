package grocery.system.model;

import java.util.Date;

public class Order {
    private int orderID;
    private int supplierID;
    private String orderStatus;
    private double totalCost;
    private Date orderDate;
    private String comment;
    public Order() {
        this.orderStatus = "DRAFT";
        this.totalCost = 0.0;
        this.orderDate = new Date();
        this.comment = "";
    }
    public void setOrderID(int orderID) {
        this.orderID = orderID;
    }
    public int getOrderID() {
        return orderID;
    }

    public void setSupplierID(int supplierID) {
        this.supplierID = supplierID;
    }
    public int getSupplierID() {return supplierID;}

    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }
    public double getTotalCost() {return totalCost;}
    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }
    public Date getOrderDate() {return orderDate;}
    public void setComment(String comment) {
        this.comment = comment;
    }
    public String getComment() {return comment;}

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }
    public String getOrderStatus() {return orderStatus;}

    public void submitOrder() {
        this.orderStatus = "SUBMITTED";
    }

    public void markDelivered() {
        this.orderStatus = "DELIVERED";
    }

    public void cancelOrder() {
        this.orderStatus = "CANCELLED";
    }
}
