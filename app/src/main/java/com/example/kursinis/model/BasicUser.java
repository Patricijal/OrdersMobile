package com.example.kursinis.model;

import java.time.LocalDateTime;

public class BasicUser extends User{
    protected String address;
    protected Integer bonusPoints = 0;

    public BasicUser(String login, String password, String name, String surname, String phoneNumber, String address) {
        super(login, password, name, surname, phoneNumber);
        this.address = address;
        this.bonusPoints = 0;
    }

    public BasicUser(int id, String login, String password, String name, String surname, String phoneNumber, LocalDateTime dateCreated, LocalDateTime dateUpdated, boolean isAdmin, String address) {
        super(id, login, password, name, surname, phoneNumber, dateCreated, dateUpdated, isAdmin);
        this.address = address;
        this.bonusPoints = 0;
    }

    public BasicUser() {
        this.bonusPoints = 0;
    }

    public BasicUser(int id, String login, String password, String name, String surname, String phoneNumber, String address) {
        super(id, login, password, name, surname, phoneNumber);
        this.address = address;
        this.bonusPoints = 0;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getBonusPoints() {
        return bonusPoints;
    }

    public void setBonusPoints(Integer bonusPoints) {
        this.bonusPoints = bonusPoints;
    }
}
