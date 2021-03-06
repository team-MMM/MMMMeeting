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
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
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
import java.util.List;
import java.util.Locale;


public class SearchPlaceActivity extends AppCompatActivity
        implements View.OnClickListener,OnMapReadyCallback {


    GoogleMap mMap;

    List<Marker> previous_marker = null;

    private static final String TAG = SearchPlaceActivity.class.getSimpleName();

    private String address=null;
    private LatLng arrival;

    Button btn_vote,btn_delete;
    private View layout_search;


    private ScheduleInfo scheduleInfo;
    private String scheduleId;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String id=null;
    int size;
    String state="valid";
    boolean result = false; //투표리스트에 없음

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_place_search);


        String apiKey = getString(R.string.api_key);

        previous_marker = new ArrayList<Marker>();

        layout_search=findViewById(R.id.layout_search);
        btn_vote = findViewById(R.id.btn_vote);
        btn_delete = findViewById(R.id.btn_delete);

        btn_vote.setOnClickListener(this);
        btn_delete.setOnClickListener(this);

        scheduleInfo = (ScheduleInfo) getIntent().getSerializableExtra("scheduleInfo");
        scheduleId = scheduleInfo.getId();

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
                                            info.setState("valid");
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
                            .setPositiveButton("추가", new DialogInterface.OnClickListener(){
                                // 확인 버튼 클릭시 설정, 오른쪽 버튼입니다.
                                public void onClick(DialogInterface dialog, int whichButton){//투표리스트에 추가
                                    checkList(arrival); // 투표리스트에 존재하는지 확인
                                    DocumentReference docRef = db.collection("vote").document(id);

                                    Handler delayHandler = new Handler();
                                    Runnable r = new Runnable() {
                                        @Override
                                        public void run() {
                                            if(state.equals("valid")){ // 투표 시작 전 상태
                                                if(result){
                                                    result = false;
                                                    Toast.makeText(SearchPlaceActivity.this, "이미 추가된 장소입니다.", Toast.LENGTH_SHORT).show();
                                                }else {
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
                                            }
                                            else{
                                                Toast.makeText(SearchPlaceActivity.this, "이미 투표가 시작되었습니다.", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    };
                                    delayHandler.postDelayed(r, 1500); // 1.5초후

                                    docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull com.google.android.gms.tasks.Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                DocumentSnapshot document = task.getResult();
                                                if (document.exists()) {
                                                    // 해당 문서가 존재하는 경우
                                                    List<HashMap<String, Object>> list = (List<HashMap<String, Object>>) document.get("place");
                                                    size = list.size();
                                                    state = document.getData().get("state").toString(); // 투표 상태
                                                } else {
                                                    // 존재하지 않는 문서
                                                    Log.d("Attend", "No Document");
                                                }
                                            } else {
                                                Log.d("Attend", "Task Fail : " + task.getException());
                                            }
                                        }
                                    });
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
            //투표삭제 버튼을 눌렀을 때
            case R.id.btn_delete:
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
                                            info.setState("valid");
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
                    builder.setTitle("투표 삭제")        // 제목
                            .setMessage("투표리스트에서 삭제하시겠습니까?")        // 메세지
                            // .setCancelable(false)        // 뒤로 버튼 클릭시 취소 가능 설정
                            .setPositiveButton("삭제", new DialogInterface.OnClickListener(){
                                // 확인 버튼 클릭시 설정, 오른쪽 버튼입니다.
                                public void onClick(DialogInterface dialog, int whichButton){//투표리스트에 추가
                                    checkList(arrival); // 투표리스트에 존재하는지 확인
                                    DocumentReference docRef = db.collection("vote").document(id);

                                    Handler delayHandler = new Handler();
                                    Runnable r = new Runnable() {
                                        @Override
                                        public void run() {
                                            if(state.equals("valid")) { // 투표 시작 전 상태
                                                if (result) {
                                                    result = false;
                                                    HashMap<String, Object> map = new HashMap<>();
                                                    GeoPoint location = new GeoPoint(arrival.latitude, arrival.longitude);
                                                    List<String> voter = new ArrayList<>();
                                                    map.put("latlng", location);
                                                    map.put("vote", 0);
                                                    map.put("name", address);
                                                    map.put("voter", voter);

                                                    docRef.update("place", FieldValue.arrayRemove(map));
                                                    Toast.makeText(SearchPlaceActivity.this, "투표리스트에서 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                                                }
                                                else{
                                                    Toast.makeText(SearchPlaceActivity.this, "투표리스트에 존재하지 않습니다.", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                            else{
                                                Toast.makeText(SearchPlaceActivity.this, "이미 투표가 시작되었습니다.", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    };
                                    delayHandler.postDelayed(r, 1500); // 1.5초후

                                    docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull com.google.android.gms.tasks.Task<DocumentSnapshot> task) {
                                            if (task.isSuccessful()) {
                                                DocumentSnapshot document = task.getResult();
                                                if (document.exists()) {
                                                    // 해당 문서가 존재하는 경우
                                                    List<HashMap<String, Object>> list = (List<HashMap<String, Object>>) document.get("place");
                                                    size = list.size();
                                                    state = document.getData().get("state").toString(); // 투표 상태
                                                } else {
                                                    // 존재하지 않는 문서
                                                    Log.d("Attend", "No Document");
                                                }
                                            } else {
                                                Log.d("Attend", "Task Fail : " + task.getException());
                                            }
                                        }
                                    });
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
    //투표리스트에 해당 장소가 있는지 확인
    private void checkList(LatLng location){
        DocumentReference docRef = db.collection("vote").document(id);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // 해당 문서가 존재하는 경우
                        List<HashMap<String,Object>> list = (List<HashMap<String, Object>>)document.get("place");
                        for(int i =0; i<list.size(); i++) {
                            HashMap<String, Object> hashMap = new HashMap<String, Object>();
                            hashMap = list.get(i);
                            GeoPoint geo = (GeoPoint) hashMap.get("latlng");
                            LatLng loc = new LatLng(geo.getLatitude(),geo.getLongitude());
                            if(loc.equals(location)){
                                result = true; // 투표리스트에 이미 있음
                                Log.d("CheckList", "Success");
                            }
                        }
                    } else {
                        // 존재하지 않는 문서
                        Log.d("CheckList", "No Document");
                    }
                } else {
                    Log.d("CheckList", "Task Fail : " + task.getException());
                }
            }
        });
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

