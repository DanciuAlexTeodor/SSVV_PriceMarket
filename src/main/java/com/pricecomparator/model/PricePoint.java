package com.pricecomparator.model;
import java.util.*;

public class PricePoint {
    
    private String id;
    private String name;
    private String category;
    private String brand;
    private double price;
    private String store;
    private String date;

    public PricePoint(String id, String name, String category, String brand, double price, String store, String date)
    {
        this.id=id;
        this.name=name;
        this.category=category;
        this.brand=brand;
        this.price=price;
        this.store=store;
        this.date=date;
    }

    public String getId()
    {
        return this.id;
    }

    public String getName()
    {
        return this.name;
    }

    public String getCategory()
    {
        return this.category;
    }

    public String getBrand()
    {
        return this.brand;
    }

    public double getPrice()
    {
        return this.price;  
    }

    public String getStore()
    {
        return this.store;
    }

    public String getDate()
    {
        return this.date;
    }

}
