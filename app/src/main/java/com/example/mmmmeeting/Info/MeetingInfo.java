package com.example.mmmmeeting.Info;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

// 모임 정보 : 모임이름, 모임 설명, 모임원에 대한 정보 저장, 가져오기 가능
public class MeetingInfo {
    private String meetingName;
    private String description;
    private String leader;
    private ArrayList<String> userID = new ArrayList<>();
    private HashMap<String,Integer> best = new  HashMap<>();
    private String resetDate=new SimpleDateFormat("yyyyMMdd").format(new Date());

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

    public String getLeader() {
        return leader;
    }

    public void setLeader(String leader){
        this.leader=leader;
    }

    public HashMap<String, Integer> getBest() {
        return best;
    }

    public void setBest(HashMap<String, Integer> best){
        this.best=best;
    }

    public String getResetDate() { return resetDate; }

    public void setResetDate(String resetDate) { this.resetDate = resetDate; }
}
