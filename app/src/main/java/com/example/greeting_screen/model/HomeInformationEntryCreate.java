package com.example.greeting_screen.model;

public class HomeInformationEntryCreate {

    private String title;
    private String description;
    private String information_time;
    private boolean event_today;

    public HomeInformationEntryCreate(String title, String description, String information_time, boolean event_today) {
        this.title = title;
        this.description = description;
        this.information_time = information_time;
        this.event_today = event_today;
    }

}
