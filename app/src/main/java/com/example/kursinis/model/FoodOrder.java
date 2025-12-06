package com.example.kursinis.model;

import java.time.LocalDate;

public class FoodOrder {
    private int id;
    private String name;
    private Double price;
    private OrderStatus orderStatus;
    private LocalDate dateCreated;
    private LocalDate dateUpdated;

    private int buyerId;
    private int driverId;


    public FoodOrder(int id, String name, Double price, OrderStatus orderStatus, LocalDate dateCreated, LocalDate dateUpdated) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.orderStatus = orderStatus;
        this.dateCreated = dateCreated;
        this.dateUpdated = dateUpdated;
    }

    public FoodOrder() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public LocalDate getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(LocalDate dateCreated) {
        this.dateCreated = dateCreated;
    }

    public LocalDate getDateUpdated() {
        return dateUpdated;
    }

    public void setDateUpdated(LocalDate dateUpdated) {
        this.dateUpdated = dateUpdated;
    }

    public int getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(int buyerId) {
        this.buyerId = buyerId;
    }

    public int getDriverId() {
        return driverId;
    }

    public void setDriverId(int driverId) {
        this.driverId = driverId;
    }

    @Override
    public String toString() { return "Order: " + name + ", " + price + "€, " + orderStatus + ", " + dateCreated; }
}
