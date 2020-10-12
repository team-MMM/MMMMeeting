package com.example.mmmmeeting.activity;


import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.mmmmeeting.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutionException;

public class MapMainActivity extends AppCompatActivity
        implements OnMapReadyCallback {

    private static final String API_KEY="AIzaSyB0_yYCWRjfJoPOhDAPnZ4l2-mu6IbLbv0";
    private GoogleMap map;

    private String str_url=null;
    private String str_info=null;

    private LinearLayout container;
    private static final float FONT_SIZE =15;

    private LatLng depart=new LatLng(37.5728359,126.9746922);;
    private LatLng arrival= new LatLng(37.5129907,127.1005382);

    private DataParser parser= new DataParser();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map_main);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        container =(LinearLayout) findViewById(R.id.layout_1);

        textview(showInfo(depart,arrival));
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;

        MarkerOptions marker1 = new MarkerOptions();
        marker1.position(depart).title("출발").snippet("종로구")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        map.addMarker(marker1);

        MarkerOptions marker2 = new MarkerOptions();
        marker2.position(arrival).title("도착").snippet("잠실")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED));
        map.addMarker(marker2);


        map.moveCamera(CameraUpdateFactory.newLatLngZoom(arrival, 10));

    }



    ////////////////////////
    //URL연결, JSON 받아오기///
    ////////////////////////
    public class Task extends AsyncTask<String,Void,String>{
        private String str,receiveMsg;

        @Override
        protected String doInBackground(String... parms) {
            URL url = null;

            try{
                url = new URL(str_url);

                HttpURLConnection conn = (HttpURLConnection)url.openConnection();

                if(conn.getResponseCode()==conn.HTTP_OK){
                    InputStreamReader tmp = new InputStreamReader(conn.getInputStream(), "UTF-8");
                    BufferedReader reader = new BufferedReader(tmp);

                    StringBuffer buffer = new StringBuffer();
                    while((str=reader.readLine())!=null){
                        buffer.append(str);
                    }
                    receiveMsg = buffer.toString();
                    reader.close();
                }
                else{
                    Log.i("통신 결과",conn.getResponseCode()+"에러");
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return receiveMsg;
        }
    }

    public String showInfo(LatLng depart, LatLng arrival){

        String str_origin = depart.latitude+","+depart.longitude;
        String str_dest = arrival.latitude+","+arrival.longitude;

        str_url="https://maps.googleapis.com/maps/api/directions/json?"+
                "origin="+str_origin+"&destination="+str_dest+"&mode=transit"+
                "&alternatives=true&language=Korean&key="+API_KEY;

        String resultText = "값이 없음";

        try{
            resultText = new Task().execute().get();

            System.out.println("str_url 출력하기 : "+str_url);
            System.out.println("resultText : "+resultText);
            str_info=parser.parse(resultText);
            System.out.println("str_info : "+str_info);
            //textview(str_info);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        System.out.println("str_info : "+str_info);
        return str_info;
    }
    public void textview(String a){
        //TextView 생성
        TextView view1 = new TextView(this);
        view1.setText(a);
        view1.setTextSize(FONT_SIZE);
        view1.setTextColor(Color.BLACK);

        //layout_width, layout_height, gravity 설정
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams
                (ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        lp.gravity= Gravity.CENTER;
        view1.setLayoutParams(lp);

        container.addView(view1);


    }
}