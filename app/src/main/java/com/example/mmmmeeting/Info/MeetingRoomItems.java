package com.example.mmmmeeting.Info;

// 그리드 뷰에 사용하는 그리드 아이템 : 모임 이름과 모임 설명을 하나의 클래스로
public class MeetingRoomItems {
    private String meetingName;
    private String description;

    public MeetingRoomItems(String name, String description){
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