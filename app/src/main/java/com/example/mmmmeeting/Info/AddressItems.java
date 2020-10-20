package com.example.mmmmeeting.Info;

public class AddressItems {
    private String road;
    private String jibun;
    private String post;

    public AddressItems(String road, String jubun,String post){
        this.road = road;
        this.jibun =jubun;
        this.post =post;
    }

    public String getRoad(){
        return this.road;
    }


    public String getJibun() { return jibun;}


    public String getPost() { return post;}

}
