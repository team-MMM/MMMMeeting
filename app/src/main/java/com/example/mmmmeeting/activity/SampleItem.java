package com.example.mmmmeeting.activity;


public class SampleItem {
    String text;
    int img_num;

    SampleItem(String text, int img_num){
        this.text = text;
        this.img_num=img_num;
    }
    public String getText(){
        return text;
    }
    public int getImg_num(){
        return img_num;
    }
}
