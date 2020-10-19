package com.example.mmmmeeting.Info;

import java.util.ArrayList;

// 모임 정보 : 모임이름, 모임 설명, 모임원에 대한 정보 저장, 가져오기 가능
public class MeetingInfo {
    private String meetingName;
    private String description;
    private ArrayList<String> userID = new ArrayList<>();

    public MeetingInfo(String name, String description){
        this.meetingName = name;
        this.description=description;
    }

    public String getName(){
        return this.meetingName;
    }

    public void setName(String name){
        this.meetingName = name;
    }

    public String getDescription(){
        return this.description;
    }

    public void setDescription(String description){
        this.description = description;
    }

    public void setUserID(String userID) {
        this.userID.add(userID);
    }

    public ArrayList<String> getUserID() {
        return userID;
    }
}
