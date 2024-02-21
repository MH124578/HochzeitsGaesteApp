package com.example.hgapp;

public class ImageData {
    private final int entryId;
    private final String imageUrl;
    private final String text;
    private boolean isSelected;

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

    public String getImageUrl() {
        return imageUrl;
    }

    public String getText() {
        return text;
    }
    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
