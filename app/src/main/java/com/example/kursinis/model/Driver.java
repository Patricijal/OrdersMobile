package com.example.kursinis.model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class Driver extends BasicUser{
    private String license;
    private LocalDate bDate;
    private VehicleType vehicleType;

    public Driver(String login, String password, String name, String surname, String phoneNumber, String address, String license, LocalDate bDate, VehicleType vehicleType) {
        super(login, password, name, surname, phoneNumber, address);
        this.license = license;
        this.bDate = bDate;
        this.vehicleType = vehicleType;
    }

    public Driver(int id, String login, String password, String name, String surname, String phoneNumber, LocalDateTime dateCreated, LocalDateTime dateUpdated, boolean isAdmin, String address, String license, LocalDate bDate, VehicleType vehicleType) {
        super(id, login, password, name, surname, phoneNumber, dateCreated, dateUpdated, isAdmin, address);
        this.license = license;
        this.bDate = bDate;
        this.vehicleType = vehicleType;
    }

    public Driver(String license, LocalDate bDate, VehicleType vehicleType) {
        this.license = license;
        this.bDate = bDate;
        this.vehicleType = vehicleType;
    }

    public Driver() {
    }

    public Driver(int id, String login, String password, String name, String surname, String phoneNumber, String address, String license, LocalDate bDate, VehicleType vehicleType) {
        super(id, login, password, name, surname, phoneNumber, address);
        this.license = license;
        this.bDate = bDate;
        this.vehicleType = vehicleType;
    }

    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    public LocalDate getBDate() {
        return bDate;
    }

    public void setBDate(LocalDate bDate) {
        this.bDate = bDate;
    }

    public VehicleType getVehicleType() {
        return vehicleType;
    }

    public void setVehicleType(VehicleType vehicleType) {
        this.vehicleType = vehicleType;
    }
}
