package com.example.hgapp;

public class ImageData {
    private int entryId;
    private String imageUrl;
    private String text;
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

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }
}
