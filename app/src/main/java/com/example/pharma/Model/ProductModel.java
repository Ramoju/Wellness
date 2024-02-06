package com.example.pharma.Model;

import java.util.ArrayList;

public class ProductModel {
    String name,description,category, spanishDescription;
    ArrayList<ImageModel> images;
    double price,quantity;

    public String getSpanishDescription() {
        return spanishDescription;
    }

    public void setSpanishDescription(String spanishDescription) {
        this.spanishDescription = spanishDescription;
    }

    public ProductModel(String name, String description, String category, double price, double quantity, ArrayList<ImageModel> imagesFilePath) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.price = price;
        this.quantity = quantity;
        this.images = imagesFilePath;
    }

    public ArrayList<ImageModel> getImagesFilepath() {
        return images;
    }

    public void setImagesFilepath(ArrayList<ImageModel> imagesFilepath) {
        this.images = imagesFilepath;
    }

    public ProductModel() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }
}
