package com.pricecomparator.model;

public class Discount {
    private String productId;
    private String name;
    private String brand;
    private String quantity;
    private String unit;
    private String category;
    private String fromDate;
    private String toDate;
    private int discountPercent;
    private String datePosted; // Date when this discount was recorded (from CSV filename)

    public Discount(String productId, String name, String brand, String quantity, String unit,
                    String category, String fromDate, String toDate, int discountPercent) {
        this.productId = productId;
        this.name = name;
        this.brand = brand;
        this.quantity = quantity;
        this.unit = unit;
        this.category = category;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.discountPercent = discountPercent;
        this.datePosted = null; // Will be set based on source file date
    }

    public Discount(String productId, String name, String brand, String quantity, String unit,
                    String category, String fromDate, String toDate, int discountPercent, String datePosted) {
        this.productId = productId;
        this.name = name;
        this.brand = brand;
        this.quantity = quantity;
        this.unit = unit;
        this.category = category;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.discountPercent = discountPercent;
        this.datePosted = datePosted;
    }

    public String getDatePosted() {
        return datePosted;
    }

    public void setDatePosted(String datePosted) {
        this.datePosted = datePosted;
    }

    @Override
    public String toString() {
        return name + " (" + brand + ") " + discountPercent + "% OFF from " + fromDate + " to " + toDate +
               (datePosted != null ? " (posted on " + datePosted + ")" : "");
    }

    public int getDiscountPercent() {
        return discountPercent;
    }

    public String getProductName(){
        return name;
    }

    public String getBrand(){
        return brand;
    }

    public String getProductId() {
        return productId;
    }

    public String getFromDate() {
        return fromDate;
    }

    public String getToDate() {
        return toDate;
    }
}
