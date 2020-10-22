// 장소 검색 화면
package com.example.mmmmeeting.activity;

/*
 * 위치를 직접 검색해서 목적지로 설정한다.
 */


import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;


import com.example.mmmmeeting.R;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import noman.googleplaces.NRPlaces;
import noman.googleplaces.PlaceType;
import noman.googleplaces.PlacesException;
import noman.googleplaces.PlacesListener;


public class SearchPlaceActivity extends AppCompatActivity
        implements View.OnClickListener,OnMapReadyCallback, PlacesListener {


    GoogleMap mMap;

    List<Marker> previous_marker = null;

    private static final String TAG = SearchPlaceActivity.class.getSimpleName();

    private String address=null;
    private LatLng arrival;

    Button btn_route,btn_category,btn_loading;
    private View layout_search;

    int category; //선택한 카테고리 넘버

    // 구글 서버로 부터 받아온 데이터를 저장할 리스트
    ArrayList<Double> lat_list;
    ArrayList<Double> lng_list;
    ArrayList<String> name_list;
    ArrayList<String> vicinity_list;
    // 지도의 표시한 마커(주변장소표시)를 관리하는 객체를 담을 리스트
    ArrayList<Marker> markers_list;
    // 다이얼로그를 구성하기 위한 배열
    String[] category_name_array={
            "모두","ATM","은행","미용실","카페","교회","주유소","식당"
    };
    // types 값 배열
    String[] category_value_array={
            "all","atm","bank","beauty_salon","cafe","church","gas_station","restaurant"
    };


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_place_search);


        String apiKey = getString(R.string.api_key);

        previous_marker = new ArrayList<Marker>();

        layout_search=findViewById(R.id.layout_search);
        btn_route = findViewById(R.id.btn_route);
        btn_category = findViewById(R.id.btn_category);

        btn_route.setOnClickListener(this);
        btn_category.setOnClickListener(this);


        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), apiKey);
        }

        AutocompleteSupportFragment autocompleteFragment = (AutocompleteSupportFragment)
                getSupportFragmentManager().findFragmentById(R.id.search_map);

        autocompleteFragment.setPlaceFields(Arrays.asList(Place.Field.ID, Place.Field.NAME));

        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                // TODO: Get info about the selected place.
                Log.i(TAG, "Place: " + place.getName() + ", " + place.getId());
                address=place.getName();
                System.out.println("address: "+address);

                mMap.clear();
                SearchPlaceActivity.Point p = getPointFromGeoCoder(address);
                arrival = new LatLng(p.X_value(),p.Y_value());

                MarkerOptions markerOptions = new MarkerOptions();
                markerOptions.position(arrival);
                markerOptions.title("목적지");
                markerOptions.snippet(address);
                mMap.addMarker(markerOptions);

                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(arrival, 12));
            }

            @Override
            public void onError(Status status) {
                // TODO: Handle the error.
                Log.i(TAG, "An error occurred: " + status);
            }
        });


        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.show_map);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onClick(View v){

        switch(v.getId()){
            //경로 검색 버튼을 눌렀을 때
            case R.id.btn_route:
                try {
                    if(address==null){
                        final Snackbar snackbar = Snackbar.make(layout_search, "목적지를 입력해주세요^^", Snackbar.LENGTH_INDEFINITE);
                        snackbar.setAction("확인", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                snackbar.dismiss();
                            }
                        });
                        snackbar.show();
                    }else{
                        //DirectionActivity로 넘어간다.
                        Intent i = new Intent(this, DirectionActivity.class);
                        i.putExtra("address", address);
                        startActivity(i);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            //카테고리 버튼을 눌렀을 때
            case R.id.btn_category:
                if (address == null) {
                    final Snackbar snackbar = Snackbar.make(layout_search, "목적지를 입력해주세요^^", Snackbar.LENGTH_INDEFINITE);
                    snackbar.setAction("확인", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            snackbar.dismiss();
                        }
                    });
                    snackbar.show();
                }else{
                    mMap.clear();
                    if (previous_marker != null)
                        previous_marker.clear();//지역정보 마커 클리어
                    show();
                    break;
                }
        }
    }


    void show() //카테고리 보여주기
    {
        // 카테고리를 선택 할 수 있는 리스트를 띄운다.
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
        builder.setTitle("장소 타입 선택");
        ArrayAdapter<String> adapter= new ArrayAdapter<String>(
                this,android.R.layout.simple_list_item_1,category_name_array
        );
        DialogListener listener=new DialogListener();
        builder.setAdapter(adapter,listener);
        builder.setNegativeButton("취소",null);
        builder.show();
    }
    // 다이얼로그의 리스너
    class DialogListener implements DialogInterface.OnClickListener{

        @Override
        public void onClick(DialogInterface dialogInterface, int i) {
            // 사용자가 선택한 항목 인덱스번째의 type 값을 가져온다.
            String type=category_value_array[i];
            // 주변 정보를 가져온다
            showPlaceInformation(arrival,type);
        }
    }

    public void showPlaceInformation(LatLng location,String type)
    {
        mMap.clear();//지도 클리어
        String apiKey = getString(R.string.api_key);

        if (previous_marker != null)
            previous_marker.clear();//지역정보 마커 클리어

        new NRPlaces.Builder()
                .listener(SearchPlaceActivity.this)
                .key(apiKey)
                .latlng(location.latitude, location.longitude)//현재 위치
                .radius(500) //500 미터 내에서 검색
                .type(type) //음식점
                .build()
                .execute();

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap=googleMap;

        LatLng SEOUL = new LatLng(37.56, 126.97);
        MarkerOptions marker = new MarkerOptions();
        marker.position(SEOUL);
        marker.visible(false);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(SEOUL, 8));
        mMap.clear();
    }

    @Override
    public void onPlacesFailure(PlacesException e) {

    }

    @Override
    public void onPlacesStart() {

    }

    @Override
    public void onPlacesSuccess(final List<noman.googleplaces.Place> places) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                for (noman.googleplaces.Place place : places) {

                    LatLng latLng
                            = new LatLng(place.getLatitude()
                            , place.getLongitude());

                    //주소
                    String markerSnippet = getCurrentAddress(latLng);

                    MarkerOptions markerOptions = new MarkerOptions();
                    markerOptions.position(latLng);
                    markerOptions.title(place.getName());
                    markerOptions.snippet(markerSnippet);
                    Marker item = mMap.addMarker(markerOptions);
                    previous_marker.add(item);

                }

                //중복 마커 제거
                HashSet<Marker> hashSet = new HashSet<Marker>();
                hashSet.addAll(previous_marker);
                previous_marker.clear();
                previous_marker.addAll(hashSet);

            }
        });
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

    @Override
    public void onPlacesFinished() {

    }


    class Point {
        // 위도
        public double x;
        // 경도
        public double y;
        public String addr;
        // 포인트를 받았는지 여부
        public boolean havePoint;

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append("x : ");
            builder.append(x);
            builder.append(" y : ");
            builder.append(y);
            builder.append(" addr : ");
            builder.append(addr);

            return builder.toString();
        }
        public double X_value(){
            return x;
        }
        public double Y_value(){
            return y;
        }
    }

    private SearchPlaceActivity.Point getPointFromGeoCoder(String addr){
        SearchPlaceActivity.Point point = new SearchPlaceActivity.Point();
        point.addr = addr;

        Geocoder geocoder = new Geocoder(this);
        List<Address> listAddress;

        try{
            listAddress = geocoder.getFromLocationName(addr,1);
        } catch (IOException e) {
            e.printStackTrace();
            point.havePoint=false;
            return point;
        }

        if(listAddress.isEmpty()){
            point.havePoint=false;
            return point;
        }

        point.havePoint=true;
        point.y=listAddress.get(0).getLongitude();
        point.x=listAddress.get(0).getLatitude();
        return point;
    }
}
