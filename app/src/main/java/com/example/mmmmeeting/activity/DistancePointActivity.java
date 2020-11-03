package com.example.mmmmeeting.activity;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import com.example.mmmmeeting.R;
import com.example.mmmmeeting.activity.Point;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.SphericalUtil;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class DistancePointActivity extends AppCompatActivity{
    /*
    1. 중간지점 가져오기
    2. 선호도 기반으로 한 장소 주소 array 생성
    3. 장소랑 중간지점과의 이동시간 계산
     */

    // 임시로 시립대 후문을 중간 지점으로 함
    private LatLng midP = new LatLng(37.585007,127.059992);

    // 코드 합칠 땐 좌표값을 바로 받아올듯.. 테스트 용으로 임의 생성!
    private String[] addrName = {"반지하돈부리","리얼수타돈까스","삶은고기","꽃갈피","여기가좋겠네","왕십리역"};
    private String[] addr = {"서울특별시 동대문구 휘경2동 망우로18다길 44",
                "서울 동대문구 망우로18다길 27-8","서울 동대문구 망우로18다길 13-1",
                "서울 동대문구 망우로18가길 36", "서울특별시 동대문구 회기동 회기로21길 25",
                "서울특별시 성동구 왕십리로 지하 300"};

    // 주소값 addr을 위도, 경도로 바꾸는 변수
    private Point[] points;

    private int[] disTime;
    private double[] distance;
    private double[] euclidean;
    private String str_url;
    JSONArray routesArray;
    JSONArray legsArray;

    int j = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        disTime = new int[addr.length];
        distance = new double[addr.length];
        euclidean = new double[addr.length];


        // 주소 -> 위도, 경도로 변환
        points = new Point[addr.length];
        for (String str : addr) {
            getPointFromGeoCoder(str, points);
        }

        for (int i = 0; i < addr.length; i++) {
            // 각 장소~중간지점까지의 시간을 저장
            int time = getPathTime(points[i].getposition());
            System.out.println(addrName[i]+"에서 서울시립대 후문까지 걸리는 시간: "+time+"분");
            disTime[i] = time;

            // 각 장소~중간지점까지의 거리를 저장
            distance[i] = SphericalUtil.computeDistanceBetween(midP,points[i].getposition());
            System.out.println(addrName[i]+"에서 서울시립대 후문까지의 거리 : " + String.format("%.2f", distance[i]) +'m');

            // 유클리디안 거리값 계산
            double latitude = points[i].getX()-midP.latitude;
            double longitude = points[i].getY()-midP.longitude;
            // 0~1사이에 분포, 0에 가까울 수록 중간 지점과 가깝다
            euclidean[i] = Math.sqrt(Math.pow(latitude,2.0)+Math.pow(longitude,2.0));
            // 0~5점 사이에 분포한 선호도 값과 비슷하게 분포하도록 역수+로그를 이용해서 변환
            euclidean[i] = Math.log10(1.0 / euclidean[i]);
            System.out.println(addrName[i]+"의 거리 점수 : " + String.format("%.5f", euclidean[i]));
        }
    }

    // 주소 -> 위도, 경도로 변환
    private void getPointFromGeoCoder(String addr, Point[] points) {
        Geocoder geocoder = new Geocoder(this);
        List<Address> listAddress = null;
        try {
            listAddress = geocoder.getFromLocationName(addr, 10);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        if (listAddress.isEmpty()) {
            System.out.println("주소가 없습니다.");
            return;
        }

        points[j++] = new Point(listAddress.get(0).getLatitude(), listAddress.get(0).getLongitude());
        System.out.println(addrName[j-1]+"의 경도:"+Double.toString(points[j-1].getX())+
                " 위도:"+Double.toString(points[j-1].getY()));
    }



    // 대중교통 이용한 이동 시간 -> 단점: 너무 가까운 곳은 값이 이상하게 나옴
    public int getPathTime(LatLng start) {
        // 시작 위치 ~ 중간 지점까지
        String getJS = getJSON(start);

        String Dur = null;

        try {
            JSONObject jsonObject = new JSONObject(getJS);

            // routesArray = {"legs":[legsArray]}
            // legsArray = [{"arrival_time":{}}]
            //legJsonObject = {"arrival_time":{}}
            routesArray = jsonObject.getJSONArray("routes");

            int i = 0;
            do {

                legsArray = ((JSONObject) routesArray.get(i)).getJSONArray("legs");
                //JSONObject legJsonObject = legsArray.getJSONObject(i);
                JSONObject legJsonObject = legsArray.getJSONObject(0);

                //총 이동시간 => 이건 leg마다 다르니까 step에 같이 출력하기
                String duration = legJsonObject.getString("duration");
                //Object에서 키 값이 duration인 변수를 찾아서 저장
                JSONObject durJsonObject = new JSONObject(duration);
                //duration에도 Object가 존재하므로 Object를 변수에 저장
                //getDuration[j] = durJsonObject.getString("text");
                Dur = durJsonObject.getString("text");
                i++;
            } while (i < routesArray.length());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        int totalT = 0;
        if (Dur == null) {
            System.out.println("이동시간 정보가 없습니다. " + "시작 : " + start);
            totalT = 0;
        } else {
            String[] t = Dur.split(" ");
            if (t[0].contains("시간")) {
                String[] hour = t[0].split("시간");
                int h = Integer.parseInt(hour[0]);
                String[] min = t[1].split("분");
                int m = Integer.parseInt(min[0]);
                totalT = h * 60 + m;

            } else {
                String[] min = t[0].split("분");
                int m = Integer.parseInt(min[0]);
                totalT = m;
            }
        }

        return totalT;
    }

    // depart: 카테고리 장소 | arrival: 중간 지점
    public String getJSON(LatLng depart) {

        String apiKey = getString(R.string.api_key);

        String str_origin = depart.latitude + "," + depart.longitude;
        String str_dest = midP.latitude + "," + midP.longitude;

        str_url = "https://maps.googleapis.com/maps/api/directions/json?" +
                "origin=" + str_origin + "&destination=" + str_dest + "&mode=transit" +
                "&alternatives=true&language=Ko&key=" + apiKey;

        String resultText = "값이 없음";

        try {
            resultText = new Task().execute().get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return resultText;
    }

    public class Task extends AsyncTask<String, Void, String> {
        private String str, receiveMsg;

        @Override
        protected String doInBackground(String... parms) {
            URL url = null;

            try {
                url = new URL(str_url);

                HttpURLConnection conn = (HttpURLConnection) url.openConnection();

                if (conn.getResponseCode() == conn.HTTP_OK) {
                    InputStreamReader tmp = new InputStreamReader(conn.getInputStream(), "UTF-8");
                    BufferedReader reader = new BufferedReader(tmp);

                    StringBuffer buffer = new StringBuffer();
                    while ((str = reader.readLine()) != null) {
                        buffer.append(str);
                    }
                    receiveMsg = buffer.toString();
                    reader.close();
                } else {
                    Log.i("통신 결과", conn.getResponseCode() + "에러");
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

}
