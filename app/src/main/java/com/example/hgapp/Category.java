package com.example.hgapp;

public class Category {
    private int id;
    private String name;

    // Konstruktor, Getter und Setter
    public Category(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    @Override
    public String toString() {
        return name; // Stelle sicher, dass 'name' der Name der Kategorie ist
    }
}