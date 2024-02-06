package com.example.pharma.Model;

public class ImageModel {
    private String imageURI;
    private String productName;

    public ImageModel(){

    }
    public ImageModel(String uri, String productName){
        this.imageURI = uri;
        this.productName = productName;
    }

    public String getImageURI() {
        return imageURI;
    }

    public void setImageURI(String imageURI) {
        this.imageURI = imageURI;
    }

    public String getProductName() {
        return productName;
    }

    public void setProduct(String productName) {
        this.productName = productName;
    }
}
