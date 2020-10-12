package com.example.mmmmeeting.Info;

public class GridItems {
    private String meetingName;
    private String description;

    public GridItems(String name, String description){
        this.meetingName = name;
        this.description =description;
    }

    public String getName(){
        return this.meetingName;
    }

    public void setName(String name){
        this.meetingName = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}