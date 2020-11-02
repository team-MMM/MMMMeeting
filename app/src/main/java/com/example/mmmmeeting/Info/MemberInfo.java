package com.example.mmmmeeting.Info;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

// 사용자에 대한 정보 : 이름, 주소
public class MemberInfo {
    private String name;
    private String address;

    private HashMap<String, Float> rating = new  HashMap<>();

    public MemberInfo(){}

    public MemberInfo(String name,String address){
        this.name = name;
        this.address = address;
    }

    public String getName(){
        return this.name;
    }

    public void setName(String name){
        this.name = name;
    }

    public String getAddress(){
        return this.address;
    }

    public void setAddress(String address){
        this.address = address;
    }

    public Map<String, Float> getRating() {
        return rating;
    }

    public Float getRateValue(String key) {
        return rating.get(key);
    }

    public void setRating(String key, Float rating){
        this.rating.put(key,rating);
    }
}
