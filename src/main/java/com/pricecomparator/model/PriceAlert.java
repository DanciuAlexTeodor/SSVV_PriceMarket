package com.pricecomparator.model;

public class PriceAlert {
    private String productId;
    private String productName;
    private double targetPrice;
    private String userId;
    private boolean isActive;

    public PriceAlert(String productId, String productName, double targetPrice, String userId) {
        this.productId = productId;
        this.productName = productName;
        this.targetPrice = targetPrice;
        this.userId = userId;
        this.isActive = true;
    }

    public String getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public double getTargetPrice() {
        return targetPrice;
    }

    public String getUserId() {
        return userId;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public void setTargetPrice(double targetPrice) {
        this.targetPrice = targetPrice;
    }

    @Override
    public String toString() {
        return productId + ',' + productName + ',' + targetPrice + ',' + userId;
    }

}
