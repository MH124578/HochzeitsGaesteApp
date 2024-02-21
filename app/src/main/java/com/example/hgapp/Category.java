package com.example.hgapp;

import androidx.annotation.NonNull;

public class Category {
    private final int id;
    private final String name;

    // Konstruktor, Getter und Setter
    public Category(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    @Override
    @NonNull
    public String toString() {
        return name;
    }
}
