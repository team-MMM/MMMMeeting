// 장소 검색 화면

package com.example.mmmmeeting.activity;

/*
 * 위치를 직접 검색해서 목적지로 설정한다.
 */


import android.app.AlertDialog;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


import com.example.mmmmeeting.Info.ScheduleInfo;
import com.example.mmmmeeting.Info.VoteInfo;
import com.example.mmmmeeting.R;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.AutocompleteSupportFragment;
import com.google.android.libraries.places.widget.listener.PlaceSelectionListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

import noman.googleplaces.NRPlaces;
import noman.googleplaces.PlaceType;
import noman.googleplaces.PlacesException;
import noman.googleplaces.PlacesListener;


public class SearchPlaceActivity extends AppCompatActivity
        implements View.OnClickListener,OnMapReadyCallback {


    GoogleMap mMap;

    List<Marker> previous_marker = null;

    private static final String TAG = SearchPlaceActivity.class.getSimpleName();

    private String address=null;
    private LatLng arrival;

    Button btn_route,btn_vote;
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

    private ScheduleInfo postInfo;
    private String scheduleId;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String id=null;
    int size;

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
        btn_vote = findViewById(R.id.btn_vote);

        btn_route.setOnClickListener(this);
        btn_vote.setOnClickListener(this);

        postInfo = (ScheduleInfo) getIntent().getSerializableExtra("scheduleInfo");
        scheduleId = postInfo.getId();

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
    public void onClick(View v) {

        switch (v.getId()) {
            //경로 검색 버튼을 눌렀을 때
            case R.id.btn_route:
                try {
                    if (address == null) {
                        final Snackbar snackbar = Snackbar.make(layout_search, "목적지를 입력해주세요^^", Snackbar.LENGTH_INDEFINITE);
                        snackbar.setAction("확인", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                snackbar.dismiss();
                            }
                        });
                        snackbar.show();
                    } else {
                        //DirectionActivity로 넘어간다.
                        Intent i = new Intent(this, DirectionActivity.class);
                        i.putExtra("address", address);
                        startActivity(i);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                break;
            //투표추가 버튼을 눌렀을 때
            case R.id.btn_vote:
                if (address == null) {
                    final Snackbar snackbar = Snackbar.make(layout_search, "목적지를 입력해주세요^^", Snackbar.LENGTH_INDEFINITE);
                    snackbar.setAction("확인", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            snackbar.dismiss();
                        }
                    });
                    snackbar.show();
                } else {
                    db.collection("vote").whereEqualTo("scheduleID", scheduleId).get()
                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                @Override
                                public void onComplete(@NonNull com.google.android.gms.tasks.Task<QuerySnapshot> task) {
                                    if (task.isSuccessful()) {
                                        for (QueryDocumentSnapshot document : task.getResult()) {
                                            id = document.getId(); // document 이름(id)
                                            System.out.println("list 있음");
                                        }
                                        if (id == null) {
                                            VoteInfo info = new VoteInfo(scheduleId);
                                            db.collection("vote").add(info)
                                                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                                                        @Override
                                                        public void onSuccess(DocumentReference documentReference) {
                                                            id = documentReference.getId();
                                                            Log.d("Document Create", "Creating Success");
                                                        }
                                                    })
                                                    .addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Log.d("Document Create", "Error creating documents: ", task.getException());
                                                        }
                                                    });
                                        }
                                    } else {
                                        Log.d("Document Read", "Error getting documents: ", task.getException());
                                    }
                                }
                            });

                    android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(SearchPlaceActivity.this);
                    builder.setTitle("투표 추가")        // 제목
                            .setMessage("투표리스트에 추가하시겠습니까?")        // 메세지
                            // .setCancelable(false)        // 뒤로 버튼 클릭시 취소 가능 설정
                            .setPositiveButton("확인", new DialogInterface.OnClickListener(){
                                // 확인 버튼 클릭시 설정, 오른쪽 버튼입니다.
                                public void onClick(DialogInterface dialog, int whichButton){//투표리스트에 추가
                                    DocumentReference docRef = db.collection("vote").document(id);

                                    docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull com.google.android.gms.tasks.Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                DocumentSnapshot document = task.getResult();
                                                if (document.exists()) {
                                                    // 해당 문서가 존재하는 경우
                                                    List<HashMap<String, Object>> list = (List<HashMap<String, Object>>) document.get("place");
                                                    size = list.size();
                                                } else {
                                                    // 존재하지 않는 문서
                                                    Log.d("Attend", "No Document");
                                                }
                                            } else {
                                                Log.d("Attend", "Task Fail : " + task.getException());
                                            }
                                        }
                                    });

                                    HashMap<String, Object> map = new HashMap<>();
                                    GeoPoint location = new GeoPoint(arrival.latitude, arrival.longitude);
                                    List<String> voter = new ArrayList<>();
                                    map.put("latlng", location);
                                    map.put("vote", 0);
                                    map.put("name", address);
                                    map.put("voter", voter);

                                    if (size >= 5) { // 리스트에 5개 이상 존재할 때
                                        Toast.makeText(SearchPlaceActivity.this, "더이상 투표리스트에 추가할 수 없습니다.", Toast.LENGTH_SHORT).show();
                                    } else {
                                        db.collection("vote").document(id).update("place", FieldValue.arrayUnion(map));
                                        Toast.makeText(SearchPlaceActivity.this, "투표리스트에 추가되었습니다.", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            })
                            .setNegativeButton("취소", new DialogInterface.OnClickListener(){// 취소 버튼 클릭시
                                public void onClick(DialogInterface dialog, int whichButton){//취소 이벤트...
                                }
                            });
                    AlertDialog dialog = builder.create();    // 알림창 객체 생성
                    dialog.show();    // 알림창 띄우기

                }
                break;
        }
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

