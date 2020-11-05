package com.example.mmmmeeting.activity;

import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.mmmmeeting.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;

import noman.googleplaces.NRPlaces;
import noman.googleplaces.Place;
import noman.googleplaces.PlacesException;
import noman.googleplaces.PlacesListener;

public class PlaceListActivity extends AppCompatActivity implements OnMapReadyCallback, PlacesListener {

    GoogleMap mMap;
    //임의지정
    LatLng midpoint = new LatLng(37.584114826538716, 127.05876976018965);


    LinearLayout fl_place_list,place_list_view;

    List<Marker> previous_marker=null;

    private String str_url = null;
    private String placeInfo;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //이전 엑티비티에서 중간지점 받아오기(일단 주석처리)
        Intent i = getIntent();
        midpoint = i.getParcelableExtra("midpoint");

        setContentView(R.layout.activity_place_list);

        String apiKey = getString(R.string.api_key);

        previous_marker = new ArrayList<Marker>();

        place_list_view = findViewById(R.id.place_list_view);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.place_map);
        mapFragment.getMapAsync(this);

        showPlaceInformation("restaurant");

        place_list_view.setVisibility(View.VISIBLE);
    }

    public void showPlaceInformation(String type)
    {
        //mMap.clear();//지도 클리어
        String apiKey = getString(R.string.api_key);

        if (previous_marker != null)
            previous_marker.clear();//지역정보 마커 클리어

        new NRPlaces.Builder()
                .listener(PlaceListActivity.this)
                .key(apiKey)
                .latlng(midpoint.latitude, midpoint.longitude)//현재 위치
                .radius(500) //500 미터 내에서 검색
                .type(type) //음식점
                .build()
                .execute();

    }


    @Override
    public void onPlacesFailure(PlacesException e) {

    }

    @Override
    public void onPlacesStart() {

    }

    @Override
    public void onPlacesSuccess(final List<Place> places) {
        runOnUiThread(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void run() {
                int i=0;
                for (noman.googleplaces.Place place : places) {

                    //place.getPlaceId();
                    String placeName=null;
                    float rating=0;
                    placeInfo=getPlaceJson(place.getLatitude(),place.getLongitude(),place.getPlaceId());

                    try {
                        System.out.println("try들어옴");
                        JSONObject jsonObject = new JSONObject(placeInfo);
                        System.out.println("장소정보 JSON : "+placeInfo);

                        String sresult;

                        sresult = jsonObject.getString("result");
                        JSONObject resultObject = new JSONObject(sresult);
                        rating = Float.parseFloat(resultObject.getString("rating"));
                        placeName = resultObject.getString("name");

                        System.out.println("rating값 : "+rating);
                        System.out.println("장소 명 : "+placeName);

                        LatLng latLng
                                = new LatLng(place.getLatitude()
                                , place.getLongitude());

                        //주소
                        String placeAddress = getCurrentAddress(latLng);


                        LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT);

                        LinearLayout.LayoutParams fl_param = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT);

                        fl_place_list = new LinearLayout(PlaceListActivity.this);
                        fl_place_list.setOrientation(LinearLayout.VERTICAL);
                        //param.bottomMargin = 100;
                        fl_place_list.setLayoutParams(fl_param);
                        fl_place_list.setBackgroundColor(Color.WHITE);
                        fl_place_list.setPadding(0,10,0,30);


                        RelativeLayout.LayoutParams rl_param = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT);

                        RelativeLayout pl_name = new RelativeLayout(PlaceListActivity.this);
                        pl_name.setLayoutParams(rl_param);
                        //pl_name.setOrientation(LinearLayout.HORIZONTAL);


                        //장소 이름, 주소 출력부분
                        TextView pInfo = new TextView(PlaceListActivity.this);
                        SpannableString s = new SpannableString(placeName+"\n"+placeAddress);
                        s.setSpan(new RelativeSizeSpan(1.8f),0,placeName.length(),0);
                        s.setSpan(new ForegroundColorSpan(Color.parseColor("#62ABD9")),0,placeName.length(),0);
                        pInfo.setText(s);
                        pInfo.setLayoutParams(rl_param);
                        pl_name.addView(pInfo);

                        //좋아요버튼
                        Button favorite = new Button(PlaceListActivity.this);
                        RelativeLayout.LayoutParams btn_param = new RelativeLayout.LayoutParams(90,90);
                        btn_param.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,RelativeLayout.TRUE);
                        favorite.setLayoutParams(btn_param);
                        favorite.setPadding(0,20,5,0);
                        favorite.setId(i+1);
                        favorite.setBackground(ContextCompat.getDrawable(PlaceListActivity.this,R.drawable.heart));
                        pl_name.addView(favorite);

                        //좋아요 count
                        TextView favorite_count = new TextView(PlaceListActivity.this);
                        RelativeLayout.LayoutParams fc_param = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                                ViewGroup.LayoutParams.WRAP_CONTENT);
                        //fc_param.addRule(RelativeLayout.CENTER_HORIZONTAL,RelativeLayout.TRUE);
                        fc_param.addRule(RelativeLayout.LEFT_OF,favorite.getId());
                        favorite_count.setLayoutParams(fc_param);

                        favorite_count.setText("0");
                        pl_name.addView(favorite_count);

                        fl_place_list.addView(pl_name);

                        //LinearLayout 생성
                        LinearLayout ly = new LinearLayout(PlaceListActivity.this);
                        //LinearLayout.LayoutParams lyparams = param;
                        ly.setLayoutParams(param);
                        ly.setOrientation(LinearLayout.HORIZONTAL);

                        TextView rate_tv = new TextView(PlaceListActivity.this);
                        rate_tv.setText("별점 : "+rating+" | ");
                        rate_tv.setLayoutParams(param);
                        ly.addView(rate_tv);

                        RatingBar rb = new RatingBar(PlaceListActivity.this,null,android.R.attr.ratingBarStyleSmall);
                        rb.setNumStars(5);
                        rb.setRating(rating);
                        rb.setStepSize((float)0.1);
                        rb.setPadding(0,5,0,0);
                        rb.setLayoutParams(param);
                        ly.addView(rb);


                        fl_place_list.addView(ly);

                        place_list_view.addView(fl_place_list);


                        favorite.setOnClickListener(new View.OnClickListener(){

                            @Override
                            public void onClick(View v) {
                                int selected_count=0;

                                v.setSelected(!v.isSelected());//선택여부 반전

                                if(v.isSelected()){//현재 좋아요 누른 상태

                                    selected_count++;
                                    favorite_count.setText(String.valueOf(selected_count));
                                }
                                else{
                                    if(selected_count>0)
                                        selected_count--;
                                    favorite_count.setText(String.valueOf(selected_count));
                                }


                            }
                        });

                        MarkerOptions markerOptions = new MarkerOptions();
                        markerOptions.position(latLng);
                        markerOptions.title(place.getName());
                        markerOptions.snippet(placeAddress);
                        Marker item = mMap.addMarker(markerOptions);
                        previous_marker.add(item);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    i++;
                }

                //중복 마커 제거
//                HashSet<Marker> hashSet = new HashSet<Marker>();
//                hashSet.addAll(previous_marker);
                //previous_marker.clear();
                //previous_marker.addAll(hashSet);

            }
        });
    }

    @Override
    public void onPlacesFinished() {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap=googleMap;

        MarkerOptions marker = new MarkerOptions();
        marker.position(midpoint).title("중간지점")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        mMap.addMarker(marker);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(midpoint, 15));
        //mMap.clear();
    }

    public String getCurrentAddress(LatLng latlng) {

        //지오코더... GPS를 주소로 변환
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());

        List<Address> addresses;

        try {

            addresses = geocoder.getFromLocation(
                    latlng.latitude,
                    latlng.longitude,
                    1);
        } catch (IOException ioException) {
            //네트워크 문제
            Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
            return "지오코더 서비스 사용불가";
        } catch (IllegalArgumentException illegalArgumentException) {
            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
            return "잘못된 GPS 좌표";

        }


        if (addresses == null || addresses.size() == 0) {
            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
            return "주소 미발견";

        } else {
            Address address = addresses.get(0);
            return address.getAddressLine(0).toString();
        }
    }


    ////////////////////////
    //URL연결, JSON 받아오기///
    ////////////////////////
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

    public String getPlaceJson(double latitude, double longtitude, String placeId) {

        String apiKey = getString(R.string.api_key);
        Intent a = getIntent();

        String str_origin = latitude + "," + longtitude;
        //String str_origin = currentPosition.latitude+","+currentPosition.longitude;
        System.out.println("현재위치는 : " + str_origin);


        str_url = "https://maps.googleapis.com/maps/api/place/details/json?"+
                "place_id="+placeId+"&fields=name,rating,formatted_phone_number"
                +"&key="+apiKey+"&language=ko";

        String resultText = null;

        try {
            resultText = new Task().execute().get();

        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return resultText;
    }


}
