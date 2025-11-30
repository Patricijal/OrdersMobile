package com.example.kursinis.model;

public class Cuisine {

    private int id;
    private String title;
    private String description;
    private Double price;
    private Allergens allergens;
    private boolean spicy = false;
    private boolean vegan = false;

    public boolean getSpicy() {
        return spicy;
    }

    public boolean getVegan() {
        return vegan;
    }
}
