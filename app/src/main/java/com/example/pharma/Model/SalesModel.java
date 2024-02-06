package com.example.pharma.Model;

public class SalesModel {
    private String productName;
    private int quantitysold;

    public SalesModel(String productName, int quantitysold) {
        this.productName = productName;
        this.quantitysold = quantitysold;
    }

    public SalesModel(){

    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getQuantitysold() {
        return quantitysold;
    }

    public void setQuantitysold(int quantitysold) {
        this.quantitysold = quantitysold;
    }
}
