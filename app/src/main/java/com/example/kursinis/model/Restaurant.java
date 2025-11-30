package com.example.kursinis.model;

import java.time.LocalDateTime;
import java.util.List;

public class Restaurant extends BasicUser {
    private String workHours;
    private Double rating;

    public Restaurant(String login, String password, String name, String surname, String phoneNumber, String address, String workHours, Double rating) {
        super(login, password, name, surname, phoneNumber, address);
        this.workHours = workHours;
        this.rating = rating;
    }

    public Restaurant(int id, String login, String password, String name, String surname, String phoneNumber, LocalDateTime dateCreated, LocalDateTime dateUpdated, boolean isAdmin, String address, String workHours, Double rating) {
        super(id, login, password, name, surname, phoneNumber, dateCreated, dateUpdated, isAdmin, address);
        this.workHours = workHours;
        this.rating = rating;
    }

    public Restaurant(String workHours, Double rating) {
        this.workHours = workHours;
        this.rating = rating;
    }

    public Restaurant(String login, String password, String name, String surname, String phoneNumber, String address) {
        super(login, password, name, surname, phoneNumber, address);
    }

    public Restaurant() {
    }

    public String getWorkHours() {
        return workHours;
    }

    public void setWorkHours(String workHours) {
        this.workHours = workHours;
    }

    public Double getRating() {
        return rating;
    }

    public void setRating(Double rating) {
        this.rating = rating;
    }
}