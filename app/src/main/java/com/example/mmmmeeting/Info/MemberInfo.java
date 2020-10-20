package com.example.mmmmeeting.Info;

// 사용자에 대한 정보 : 이름, 주소
public class MemberInfo {
    private String name;
    private String address;

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

}
