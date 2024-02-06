package com.example.pharma.Model;

import java.util.List;

public class OrderModel {
    double orderAmount;
    String productsList;
    String ordernum;
    String paymentID;
    String userName;

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPaymentID() {
        return paymentID;
    }

    public void setPaymentID(String paymentID) {
        this.paymentID = paymentID;
    }

    public OrderModel(double orderAmount, String productsList, String ordernum, String paymentid, String userName) {
        this.orderAmount = orderAmount;
        this.productsList = productsList;
        this.ordernum = ordernum;
        this.paymentID = paymentid;
        this.userName = userName;
    }

    public OrderModel(){

    }

    public double getOrderAmount() {
        return orderAmount;
    }

    public void setOrderAmount(double orderAmount) {
        this.orderAmount = orderAmount;
    }

    public String getProductsList() {
        return productsList;
    }

    public void setProductsList(String productsList) {
        this.productsList = productsList;
    }

    public String getOrdernum() {
        return ordernum;
    }

    public void setOrdernum(String ordernum) {
        this.ordernum = ordernum;
    }
}
