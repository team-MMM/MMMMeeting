package com.example.mmmmeeting.Info;

import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class VoteInfo {
    private String scheduleID;
    private ArrayList<HashMap<String,Object>> places = new ArrayList<>();

    public VoteInfo(String scheduleID){
        this.scheduleID = scheduleID;
    }

    public String getScheduleID(){
        return this.scheduleID;
    }

    public void setScheduleID(String Id){
        this.scheduleID = Id;
    }

    public void setPlace(GeoPoint location,Integer vote) {
        HashMap<String,Object> place= new HashMap<>();
        place.put("latlng",location);
        place.put("vote", vote);
        this.places.add(place);
    }

    public ArrayList<HashMap<String,Object>> getPlace() {
        return places;
    }

}
