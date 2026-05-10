package com.pricecomparator.model;

public class Product {
    private String id;
    private String name;
    private String category;
    private String brand;
    private double quantity;
    private String unit;
    private double price;
    private String currency;
    private String datePosted; // Date when this product price was recorded

    public Product(String id, String name, String category, String brand, double quantity, String unit, double price, String currency) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.brand = brand;
        this.quantity = quantity;
        this.unit = unit;
        this.price = price;
        this.currency = currency;
        this.datePosted = null; // Will be set based on source file date
    }

    public Product(String id, String name, String category, String brand, double quantity, String unit, double price, String currency, String datePosted) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.brand = brand;
        this.quantity = quantity;
        this.unit = unit;
        this.price = price;
        this.currency = currency;
        this.datePosted = datePosted;
    }

    public String getDatePosted() {
        return datePosted;
    }

    public void setDatePosted(String datePosted) {
        this.datePosted = datePosted;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public double getQuantity() {
        return quantity;
    }

    public void setQuantity(double quantity) {
        this.quantity = quantity;
    }

    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    @Override
    public String toString() {
        return name + " (" + brand + ") - " + quantity + " " + unit + " at " + price + " " + currency +
               (datePosted != null ? " (posted on " + datePosted + ")" : "");
    }

}
