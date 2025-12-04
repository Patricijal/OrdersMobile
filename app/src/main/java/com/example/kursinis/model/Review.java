package com.example.kursinis.model;

public class Review {
    private int id;
    private int rating;
    private String text;
    // kaip parasyti komentatoriu?
    private BasicUser commentOwner;

    @Override
    public String toString() { return text; }
}
