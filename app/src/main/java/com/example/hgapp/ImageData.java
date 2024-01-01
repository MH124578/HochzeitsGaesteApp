package com.example.hgapp;

public class ImageData {
    private int entryId;
    private String imageUrl;
    private String text;

    // Konstruktor
    public ImageData(int entryId, String imageUrl, String text) {
        this.entryId = entryId;
        this.imageUrl = imageUrl;
        this.text = text;
    }

    // Getter und Setter
    public int getEntryId() {
        return entryId;
    }

    public void setEntryId(int entryId) {
        this.entryId = entryId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
