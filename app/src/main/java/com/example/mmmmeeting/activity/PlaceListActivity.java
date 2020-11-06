package com.example.mmmmeeting.activity;

import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.mmmmeeting.Info.MemberInfo;
import com.example.mmmmeeting.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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

    TextView view1;

    private String str_url = null;
    private String placeInfo;

    private String Tag = "category Test";
    String name;
    ArrayList<String> category=new ArrayList<>();
    ArrayList<Float[]> userRatings =new ArrayList<>();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    int count; //success

    Spinner spinner;
    ArrayAdapter<String> arrayAdapter;

    Handler mHandler = new Handler(){
        public void handleMessage(android.os.Message msg)
        {
            Bundle bd = msg.getData( ) ;            /// 전달 받은 메세지에서 번들을 받음
            ArrayList<String> categoryList = bd.getStringArrayList("arg");    /// 번들에 들어있는 값 꺼냄
            // Category 찾은 다음에 쓸 함수
            spinnerAdd(categoryList);
        } ;
    } ;

    private void spinnerAdd(ArrayList<String> categoryList) {

        arrayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, categoryList);

        spinner.setAdapter(arrayAdapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener(){

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                layoutclear();

                switch (categoryList.get(position)) {
                    case "shopping":
                        //shopping_mall + department_store
                        Log.d(Tag, "Shopping");
                        showPlaceInformation("shopping_mall",1000);
                        showPlaceInformation("department_store",1000);
                        break;
                    case "activity":
                        // amusement_park + aquarium +art_gallery +stadium +zoo
                        Log.d(Tag, "Activity");
                        showPlaceInformation("amusement_park",1000);
                        showPlaceInformation("aquarium",1000);
                        showPlaceInformation("art_gallery",1000);
                        showPlaceInformation("stadium",1000);
                        showPlaceInformation("zoo",1000);
                        break;
                    case "cafe":
                        Log.d(Tag, "Cafe");
                        showPlaceInformation("cafe",500);
                        break;
                    case "restaurant":
                        Log.d(Tag, "restaurant");
                        showPlaceInformation("restaurant",500);
                        break;
                    case "park":
                        Log.d(Tag, "park");
                        showPlaceInformation("park",1000);
                        break;
                }

                new Handler().postDelayed(new Runnable(){
                    @Override
                    public void run() {
                        runOnUiThread(new Runnable(){
                            @Override
                            public void run() {
                                Log.d(Tag, "check count " + count);
                                if(count==0){
                                view1.setVisibility(View.VISIBLE);
                                }
                            }
                        });
                    }
                }, 1000); // 1초후
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void layoutclear() {
        count=0;

        mMap.clear();
        place_list_view.removeAllViews();

        MarkerOptions marker = new MarkerOptions();
        marker.position(midpoint).title("중간지점")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
        mMap.addMarker(marker);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(midpoint, 15));
        Log.d(Tag, "Clear");
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        //이전 엑티비티에서 중간지점 받아오기(일단 주석처리)
        Intent i = getIntent();
        midpoint = i.getParcelableExtra("midpoint");
        name = i.getStringExtra("name");
        Log.d("name Test", name);

        setContentView(R.layout.activity_place_list);

        String apiKey = getString(R.string.api_key);

//        previous_marker = new ArrayList<Marker>();

        place_list_view = findViewById(R.id.place_list_view);
        spinner = findViewById(R.id.categoryList);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.place_map);
        mapFragment.getMapAsync(this);

        select();
    }

    private void select(){
        //1. DB에서 별점 읽어오기 (meeting 에서 -> ui목록 접근 -> ui의 별점 읽기)
        meetingFind();
        // 다음 동작- 가중치
        Runnable r = new Runnable() {
            @Override
            public void run() {
                addWeight();

                getHighest();
                Log.d(Tag, "After Category: "+ category);

                Bundle bd = new Bundle();      /// 번들 생성
                bd.putStringArrayList("arg", category); // 번들에 값 넣기
                Message msg = mHandler.obtainMessage();   /// 핸들에 전달할 메세지 구조체 받기
                msg.setData(bd);                     /// 메세지에 번들 넣기
                mHandler.sendMessage(msg);

            }
        };

        mHandler.postDelayed(r, 1000); // 1초후
    }

    public void showPlaceInformation(String type, int radius)
    {
        //mMap.clear();//지도 클리어
        String apiKey = getString(R.string.api_key);

        new NRPlaces.Builder()
                .listener(PlaceListActivity.this)
                .key(apiKey)
                .latlng(midpoint.latitude, midpoint.longitude)//현재 위치
                .radius(radius) //범위 내에서 검색(미터)
                .type(type)
                .build()
                .execute();
    }


    @Override
    public void onPlacesFailure(PlacesException e) {
        Log.d(Tag, "In fail");

        // 성공횟수가 0
        if(count==0){
            runOnUiThread(new Runnable(){
                @Override
                public void run() {
                    // 계속 실패만 하는 경우 => 중복 출력 방지
                    place_list_view.removeAllViews();

                    //layout_width, layout_height, gravity 설정
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                    lp.gravity = Gravity.CENTER;
                    lp.setMargins(10,100,10,10);

                    view1 = new TextView(PlaceListActivity.this);

                    // 결과 레이아웃이 없으면 View.VISIBLE로..
                    // 어디에 넣어야 결과가 잘 나오려나
                    view1.setVisibility(View.GONE);


                    view1.setText("중간지점 근처에 현재 카테고리에 해당하는 장소가 존재하지 않습니다.");
                    view1.setTextSize(25f);
                    view1.setTextColor(Color.BLACK);
                    view1.setBackgroundColor(Color.WHITE);
                    view1.setGravity(Gravity.CENTER);
                    view1.setPadding(20,20,20,20);
                    view1.setLayoutParams(lp);
                    //부모 뷰에 추가
                    place_list_view.addView(view1);

                    Log.d(Tag, "on Failure");
                }
            });
        }
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
                count++;
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
                        mMap.addMarker(markerOptions);

//                        Marker item = mMap.addMarker(markerOptions);
//                        previous_marker.add(item);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    i++;
                }
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
            Log.d(Tag,"주소 미발견");
//            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
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


    // 1-1. 미팅 이름으로 사용자 테이블 접근
    private void meetingFind() {
        db.collection("meetings").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    //모든 document 출력 (dou id + data arr { : , ... ,  })
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        // 모임 이름이 같은 경우 해당 모임의 코드를 텍스트뷰에 출력
                        if (document.get("name").toString().equals(name)) {
                            // 찾은 모임의 사용자 테이블로
                            List<String> users = (List<String>) document.get("userID");
                            for (int i = 0; i < users.size(); i++) {
                                userRating(users.get(i));
                            }
                            break;
                        }
                    }
                } else {
                    Log.d(Tag, "Error getting documents: ", task.getException());
                }
            }
        });
    }

    // 1-2 사용자 별점 가져오기
    private void userRating(String userID) {
        DocumentReference docRef = db.collection("users").document(userID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        MemberInfo user = document.toObject(MemberInfo.class);
                        MapToArray(user.getRating());
                    }
                } else {
                    Log.d(Tag, "Task Fail : " + task.getException());
                }
            }
        });
    }

    // 1-3 맵 -> 배열로 변경 (계산 편리, 카테고리 정렬)
    private void MapToArray(Map<String, Float> rating) {
        Float[] temp = new Float[rating.size()];
        // 배열에 저장
        for (String key : rating.keySet()) {
            switch (key){
                case "restaurant":
                    temp[0]=rating.get(key);
                    break;
                case "cafe":
                    temp[1]=rating.get(key);
                    break;
                case "park":
                    temp[2]=rating.get(key);
                    break;
                case "shopping":
                    temp[3]=rating.get(key);
                    break;
                case "act":
                    temp[4]=rating.get(key);
                    break;
            }
        }
        this.userRatings.add(temp);
//        Log.d(Tag,"size is "+Arrays.toString(temp));
    }

    // 2-1 가중치 계산
    private void addWeight() {
        for(int i=0; i< userRatings.size(); i++){
//            Log.d(Tag,Arrays.toString(userRatings.get(i)));
            // 2. 분산 구하기
            double std = calVariance(userRatings.get(i));
            if (std > 1.3) {  // 현재 설정 : 표준편차 값이 1.15 이상인 경우 카테고리의 점수 조정
                // 사용자 별점 업데이트
                this.userRatings.set(i,ratingUpdate(userRatings.get(i)));
            }
        }
    }

    // 2-1. 분산 계산 -> 표준편차 계산
    private double calVariance(Float[] rating) {
        // 분산 = 편차 제곱의 합 / 변량의 수  => (편차 = 값- 평균)
        double avg = 0;

        // 1. 평균 구하기
        for (int i = 0; i < rating.length; i++) {
            avg += rating[i];
        }
        avg /= rating.length;

        //2. 편차 제곱
        double variance = 0;
        for (int i = 0; i < rating.length; i++) {
            variance += (rating[i] - avg) * (rating[i] - avg);
        }

        variance /= rating.length;  // 3. 분산
        double std = Math.sqrt(variance); // +) 표준 편차
//        Log.d(Tag, "variance: " + variance + ",  standard deviation: " + std);
        return std;
    }

    // 2-2. 가중치 설정
    private Float[] ratingUpdate(Float[] rating) {

        for (int i = 0; i < rating.length; i++) {
            if (rating[i]>2.5) {
                rating[i] = rating[i] * 2.0f;
            }else{
                rating[i] = rating[i] * 0.5f;
            }
        }
        // 가장 좋아하는 카테고리 점수에 가산점, 싫어하는건 감점
        Log.d(Tag, "After update rating is " + Arrays.toString(rating));

        return rating;
    }

    // 3-2 최고값
    private void getHighest() {
        Float[] avgRating = new Float[userRatings.get(0).length];
        for(int i=0; i<userRatings.get(0).length; i++){ // 카테고리마다
            Float sum=0f;
            for(int j=0; j<userRatings.size(); j++){ // 사용자 평가 합
                sum +=userRatings.get(j)[i];
            }
            avgRating[i]=sum/userRatings.size();
        }
        Log.d(Tag, Arrays.toString(avgRating));

        ArrayList<Integer> index= new ArrayList<>();

        for(int i=0; i<3;i++ ){ // 가장 높은 3개 항목
            int tempIndex = 0;
            float high = avgRating[0];

            for (int j= 0; j < avgRating.length; j++) {
                if (high <= avgRating[j]) {
                    high = avgRating[j];
                    tempIndex = j;
                }
            }

            avgRating[tempIndex]=0f;
            index.add(tempIndex);
        }
        // 0=식당 , 1=카페, 2=공원, 3=쇼핑몰, 4=액티비티
        this.category.clear();

        for (int i =0 ; i< index.size(); i++) {
            switch (index.get(i)) {
                case 0: this.category.add("restaurant"); break;
                case 1: this.category.add("cafe"); break;
                case 2: this.category.add("park"); break;
                case 3: this.category.add("shopping"); break;
//                    this.category.add("shopping_mall"); this.category.add("department_store"); break;
                case 4: this.category.add("activity"); break;
//                    this.category.add("amusement_park");
//                    this.category.add("aquarium");
//                    this.category.add("art_gallery");
//                    this.category.add("stadium");
//                    this.category.add("zoo");
//                    break;
            }
        }
    }
}
