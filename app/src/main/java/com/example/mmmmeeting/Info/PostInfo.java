package com.example.mmmmeeting.Info;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class PostInfo implements Serializable {
    private String title;
    private String description;
    private ArrayList<String> contents;
    private String publisher;
    private Date createdAt;
    private String id;
    private String meetingID;

    public PostInfo(String title, String description, String meetingID, ArrayList<String> contents, String publisher, Date createdAt, String id){
        this.title = title;
        this.description = description;
        this.meetingID = meetingID;
        this.contents = contents;
        this.publisher = publisher;
        this.createdAt = createdAt;
        this.id = id;
    }

    public PostInfo(String title, String description, String meetingID, ArrayList<String> contents, ArrayList<String> formats, String publisher, Date createdAt){
        this.title = title;
        this.description = description;
        this.meetingID = meetingID;
        this.contents = contents;
        this.publisher = publisher;
        this.createdAt = createdAt;
    }


    public PostInfo(String title, String description, ArrayList<String> contentsList, String uid, Date date, String meetingcode) {
        this.title = title;
        this.description = description;
        this.contents = contentsList;
        this.publisher = uid;
        this.createdAt = date;
        this.meetingID = meetingcode;

    }

    public Map<String, Object> getPostInfo(){
        Map<String, Object> docData = new HashMap<>();
        docData.put("title",title);
        docData.put("description",description);
        docData.put("meetingID",meetingID);
        docData.put("contents",contents);
        docData.put("publisher",publisher);
        docData.put("createdAt",createdAt);
        return  docData;
    }

    public String getTitle(){
        return this.title;
    }
    public void setTitle(String title){
        this.title = title;
    }
    public ArrayList<String> getContents(){
        return this.contents;
    }
    public void setContents(ArrayList<String> contents){
        this.contents = contents;
    }
    public String getDescription() {return this.description;}
    public void setDescription(String description){
        this.description = description;
    }
    public String getPublisher(){
        return this.publisher;
    }
    public void setPublisher(String publisher){
        this.publisher = publisher;
    }
    public Date getCreatedAt(){
        return this.createdAt;
    }
    public void setCreatedAt(Date createdAt){
        this.createdAt = createdAt;
    }
    public String getId(){
        return this.id;
    }
    public void setId(String id){
        this.id = id;
    }
    public String getMeetingID(){return this.meetingID;}
    public void setMeetingID(){this.meetingID = meetingID;}
}