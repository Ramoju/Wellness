package com.example.pharma.Model;

public class CartModel {
    private int id;

    private String productName;
    private String productImage;
    private String productDescription;
    private double price;
    private String userid;

    private int quantity;
    private double totalItemPrice;

    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    public CartModel(){

    }

    public CartModel(int id, String productName, String productImage, double price, String userid, int quantity, double totalItemPrice, String productDescription) {
        this.id = id;
        this.productName = productName;
        this.productImage = productImage;
        this.price = price;
        this.userid = userid;
        this.quantity = quantity;
        this.productDescription = productDescription;
        this.totalItemPrice = totalItemPrice;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductImage() {
        return productImage;
    }

    public void setProductImage(String productImage) {
        this.productImage = productImage;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getTotalItemPrice() {
        return totalItemPrice;
    }

    public void setTotalItemPrice(double totalItemPrice) {
        this.totalItemPrice = totalItemPrice;
    }
}
