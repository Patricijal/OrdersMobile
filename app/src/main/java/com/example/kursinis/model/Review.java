package com.example.kursinis.model;

public class Review {
    private int id;
    private int rating;
    private String text;
    // kaip parasyti komentatoriu?
    private BasicUser commentOwner;
    private int chatId;

    public int getChatId() {
        return chatId;
    }

    public void setChatId(int chatId) {
        this.chatId = chatId;
    }

    @Override
    public String toString() { return text; }
}
