package com.example.greeting_screen.model;

public class HomeInformationEntry {
    private int entry_id;
    private String title;
    private String description;
    private String information_time;
    private boolean event_today;
    private int user_id;

    // Constructors, getters, and setters

    // Example constructor:
    public HomeInformationEntry(int entry_id, String title, String description, String information_time, boolean event_today, int user_id) {
        this.entry_id = entry_id;
        this.title = title;
        this.description = description;
        this.information_time = information_time;
        this.event_today = event_today;
        this.user_id = user_id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getInformation_time() {
        return information_time;
    }

    // Getters and setters for other fields
}