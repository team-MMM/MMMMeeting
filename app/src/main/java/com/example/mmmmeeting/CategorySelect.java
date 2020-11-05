package com.example.mmmmeeting;




import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;


import com.example.mmmmeeting.Info.MemberInfo;
import com.example.mmmmeeting.activity.SearchPlaceActivity;
import com.google.android.gms.maps.GoogleMap;
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


import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import java.util.Locale;
import java.util.Map;

import androidx.annotation.NonNull;
import noman.googleplaces.NRPlaces;
import noman.googleplaces.Place;
import noman.googleplaces.PlacesException;
import noman.googleplaces.PlacesListener;


//// category Test
//        CategorySelect category  = new CategorySelect(meetingname);
//                category.select();
//// category Test

public class CategorySelect implements PlacesListener {


    //
    GoogleMap mMap;
    List<Marker> previous_marker = null;

    //
    private String Tag = "category Test";
    String name;
    ArrayList<String> category=new ArrayList<>();
    ArrayList<Float[]> userRatings =new ArrayList<>();
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    public CategorySelect(String name){
        this.name = name;
        previous_marker = new ArrayList<Marker>();

    }

    Handler mHandler = new Handler(){
        public void handleMessage(android.os.Message msg)
        {
            Bundle bd = msg.getData( ) ;            /// 전달 받은 메세지에서 번들을 받음
            ArrayList<String> categoryList = bd.getStringArrayList("arg");    /// 번들에 들어있는 값 꺼냄
            // Category 찾은 다음에 쓸 함수
            sendCategory(categoryList);
            Log.d(Tag,"Send is "+ categoryList.toString());
        } ;
    } ;

    private String sendCategory(ArrayList<String> category) {
        // 카테고리 얻은 다음 동작 기술
        Log.d(Tag,"Send2 is "+ category.toString());
        return category.toString();
    }

    public void select(){
        //1. DB에서 별점 읽어오기 (meeting 에서 -> ui목록 접근 -> ui의 별점 읽기)
        meetingFind();
        // 다음 동작- 가중치
        Runnable r = new Runnable() {
            @Override
            public void run() {
                getHighest();
                Log.d(Tag, "before Category: "+ category);

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

    // 1-1. 미팅 이름으로 사용자 테이블 접근
    private void meetingFind() {
        db.collection("meetings").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
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
                            return;
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
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
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
                case "subway":
                    temp[2]=rating.get(key);
                    break;
                case "shopping":
                    temp[3]=rating.get(key);
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

        float high = avgRating[0];
        ArrayList<Integer> index= new ArrayList<>();
        for (int i = 0; i < avgRating.length; i++) {
            if (high <= avgRating[i]) {
                high = avgRating[i];
            }
        }

        // 중복 최대값 확인
        for (int i=0; i<avgRating.length;i++){
            if(high == avgRating[i]){
                index.add(i);
            }
        }

        // 0 = 식당 , 1 = 카페, 2 = 지하철, 3 = 쇼핑몰
        this.category.clear();
        for (int i =0 ; i< index.size(); i++) {
            switch (index.get(i)) {
                case 0: this.category.add("식당"); break;
                case 1: this.category.add("카페"); break;
                case 2: this.category.add("지하철"); break;
                case 3: this.category.add("쇼핑몰"); break;
            }
        }
    }

    private  void showPlaceInformation(LatLng location, String type)
    {
        mMap.clear();//지도 클리어
        String apiKey = "AIzaSyAMjzNcvmVwExmuyfw82V-G-DXmhVAUymY";
//        String apiKey = getString(R.string.api_key);

        if (previous_marker != null)
            previous_marker.clear();//지역정보 마커 클리어

        new NRPlaces.Builder()
                .listener(CategorySelect.this)
                .key(apiKey)
                .latlng(location.latitude, location.longitude)//현재 위치
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
    public void onPlacesSuccess(List<Place> places) {

        // 액티비티가 아니라 UI가 안되나볻,
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                for (Place place : places) {
//
//                    LatLng latLng
//                            = new LatLng(place.getLatitude()
//                            , place.getLongitude());
//
//                    //주소
//                    String markerSnippet = getCurrentAddress(latLng);
//
//                    MarkerOptions markerOptions = new MarkerOptions();
//                    markerOptions.position(latLng);
//                    markerOptions.title(place.getName());
//                    markerOptions.snippet(markerSnippet);
//                    Marker item = mMap.addMarker(markerOptions);
//                    previous_marker.add(item);
//
//                }
////
////                //중복 마커 제거
////                HashSet<Marker> hashSet = new HashSet<Marker>();
////                hashSet.addAll(previous_marker);
////                previous_marker.clear();
////                previous_marker.addAll(hashSet);
//
//            }
//        });
    }

//    public String getCurrentAddress(LatLng latlng) {
//
//        //지오코더... GPS를 주소로 변환
//        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
//
//        List<Address> addresses;
//
//        try {
//
//            addresses = geocoder.getFromLocation(
//                    latlng.latitude,
//                    latlng.longitude,
//                    1);
//        } catch (IOException ioException) {
//            //네트워크 문제
////            Toast.makeText(this, "지오코더 서비스 사용불가", Toast.LENGTH_LONG).show();
//            return "지오코더 서비스 사용불가";
//        } catch (IllegalArgumentException illegalArgumentException) {
////            Toast.makeText(this, "잘못된 GPS 좌표", Toast.LENGTH_LONG).show();
//            return "잘못된 GPS 좌표";
//
//        }
//
//
//        if (addresses == null || addresses.size() == 0) {
////            Toast.makeText(this, "주소 미발견", Toast.LENGTH_LONG).show();
//            return "주소 미발견";
//
//        } else {
//            Address address = addresses.get(0);
//            return address.getAddressLine(0).toString();
//        }
//
//    }

    @Override
    public void onPlacesFinished() {

    }
}
