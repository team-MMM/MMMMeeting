package com.example.mmmmeeting.activity;

import android.content.Intent;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
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
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mmmmeeting.Info.ScheduleInfo;
import com.example.mmmmeeting.Info.VoteInfo;
import com.example.mmmmeeting.R;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.GeoPoint;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Collections;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class VoteActivity extends AppCompatActivity {
    ScheduleInfo schedule;
    String id = null;
    String scheduleId;
    LinearLayout fl_place_list, place_list_view;
    public Button start_Btn,end_Btn;
    HashMap<String, Object> votePlace = new HashMap<>(); // 투표할 장소
    int selected_count; //투표수
    List<HashMap<String,Object>> list = new ArrayList<>(); // 투표할 장소정보 리스트
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vote);
        place_list_view = findViewById(R.id.place_list_view);

        start_Btn=findViewById(R.id.start_Btn);
        end_Btn=findViewById(R.id.end_Btn);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        Intent intent = getIntent();
        schedule = (ScheduleInfo) getIntent().getSerializableExtra("scheduleInfo");
        scheduleId = schedule.getId();

        db.collection("vote").whereEqualTo("scheduleID", scheduleId).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                id = document.getId(); // document 이름(id)
                                checkVote();
                            }
                       //     if(id==null){
                                //없으면 없다고 화면 띄워주기
                    //        }
                        } else {
                            Log.d("Document Read", "Error getting documents: ", task.getException());
                        }
                    }
                });

        start_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { // 시작 버튼 클릭
                db.collection("vote").document(id).update("state", "invalid"); //투표상태를 변경
            }

        });

        end_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { // 종료 버튼 클릭
                //일단 최다 득표
                int count; // 장소별 투표수
                int max; // 최대값
                int check=0; // 최다 득표가 여러개인지 체크
                ArrayList<Integer> vote_count = new ArrayList<>(); // 장소별 투표수 리스트
                List<HashMap<String,Object>> maxList = new ArrayList<>(); //최다 득표 장소 리스트
                for (int i = 0; i < list.size(); i++) { // vote_count에 투표수를 넣음
                    votePlace = list.get(i);
                    count=Integer.parseInt(String.valueOf(votePlace.get("vote")));
                    vote_count.add(count);
                }
                max = Collections.max(vote_count); // 최다 득표수 찾기
                for (int i = 0; i < vote_count.size(); i++) { // 최다 득표 장소들을 리스트에 넣음
                    if(vote_count.get(i)==max){
                        check++; // 여러개임을 표시
                        maxList.add(list.get(i));
                    //    votePlace = list.get(i);
                    }
                }
                if(check==1){ //최다 득표 장수가 하나면 바로 등록
                    String name = (String) votePlace.get("name");
                    Toast.makeText(VoteActivity.this, "가장 많은 투표를 받은 장소는 " + name +"입니다.", Toast.LENGTH_SHORT).show();
                    db.collection("schedule").document(scheduleId).update("meetingPlace", name); // db에 최종장소 올리기
                    Toast.makeText(VoteActivity.this, name +"(으)로 약속장소가 설정되었습니다.", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(VoteActivity.this, "가장 많은 투표를 받은 장소가 " + check +"곳입니다."+ "\n" + "최종 선택이 필요합니다.", Toast.LENGTH_SHORT).show();
                    place_list_view.removeAllViews(); // view 지우기
                    createList(maxList); // 최다 득표로 목록 다시 생성
                }
            }

        });

    }

    public void checkVote(){
        DocumentReference docRef = db.collection("vote").document(id);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull com.google.android.gms.tasks.Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // 해당 문서가 존재하는 경우
                        list = (List<HashMap<String, Object>>) document.get("place");
                        createList(list);
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

    public void createList(List<HashMap<String,Object>> voteList){
        for (int i = 0; i < voteList.size(); i++) {
            votePlace = voteList.get(i);

            String placeName = (String) votePlace.get("name");
            GeoPoint g = (GeoPoint) votePlace.get("latlng");
            LatLng latLng = new LatLng((double) g.getLatitude(), (double) g.getLongitude());
            String placeAddress = getCurrentAddress(latLng); //주소
            selected_count = Integer.parseInt(String.valueOf(votePlace.get("vote")));

            LinearLayout.LayoutParams param = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);

            LinearLayout.LayoutParams fl_param = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);

            fl_place_list = new LinearLayout(VoteActivity.this);
            fl_place_list.setOrientation(LinearLayout.VERTICAL);
            fl_place_list.setLayoutParams(fl_param);
            fl_place_list.setBackgroundColor(Color.WHITE);
            fl_place_list.setPadding(0,10,0,30);

            RelativeLayout.LayoutParams rl_param = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);

            RelativeLayout pl_name = new RelativeLayout(VoteActivity.this);
            pl_name.setLayoutParams(rl_param);

            //장소 이름, 주소 출력부분
            TextView pInfo = new TextView(VoteActivity.this);
            SpannableString s = new SpannableString(placeName+"\n\n"+placeAddress);
            s.setSpan(new RelativeSizeSpan(1.8f),0,placeName.length(),0);
            s.setSpan(new ForegroundColorSpan(Color.parseColor("#62ABD9")),0,placeName.length(),0);
            pInfo.setText(s);
            pInfo.setLayoutParams(rl_param);
            pl_name.addView(pInfo);

            //좋아요버튼
            Button favorite = new Button(VoteActivity.this);
            RelativeLayout.LayoutParams btn_param = new RelativeLayout.LayoutParams(90,90);
            btn_param.addRule(RelativeLayout.ALIGN_PARENT_RIGHT,RelativeLayout.TRUE);
            favorite.setLayoutParams(btn_param);
            favorite.setPadding(0,20,5,0);
            favorite.setId(i);
            favorite.setBackground(ContextCompat.getDrawable(VoteActivity.this,R.drawable.heart));
            pl_name.addView(favorite);

            //좋아요 count
            TextView favorite_count = new TextView(VoteActivity.this);
            favorite_count.setId(i);
            RelativeLayout.LayoutParams fc_param = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
            fc_param.addRule(RelativeLayout.LEFT_OF, favorite.getId());
            fc_param.addRule(RelativeLayout.BELOW, favorite.getId());
            favorite_count.setLayoutParams(fc_param);

            pl_name.addView(favorite_count);

            fl_place_list.addView(pl_name);

            //LinearLayout 생성
            LinearLayout ly = new LinearLayout(VoteActivity.this);
            ly.setLayoutParams(param);
            ly.setOrientation(LinearLayout.HORIZONTAL);

            fl_place_list.addView(ly);

            place_list_view.addView(fl_place_list);

            favorite.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v) {
                    TextView textView;
                    v.setSelected(!v.isSelected());//선택여부 반전
                    switch (v.getId()){
                        case (0):
                            votePlace=voteList.get(0);
                            selected_count = Integer.parseInt(String.valueOf(votePlace.get("vote")));
                           // TextView textView1 = (TextView) findViewById(0);
                            textView= (TextView) findViewById(0);
                            if(v.isSelected()){//현재 좋아요 누른 상태
                                selected_count++;
                                textView.setText(String.valueOf(selected_count));
                                updateDB(selected_count, votePlace);
                            }
                            else{
                                if(selected_count>0)
                                selected_count--;
                                textView.setText(String.valueOf(selected_count)) ;
                                updateDB(selected_count, votePlace);
                            }
                            break;
                        case(1):
                            votePlace=voteList.get(1);
                            selected_count = Integer.parseInt(String.valueOf(votePlace.get("vote")));
                            //TextView textView2 = (TextView) findViewById(1);
                            textView= (TextView) findViewById(1);
                            if(v.isSelected()){//현재 좋아요 누른 상태
                                selected_count++;
                                textView.setText(String.valueOf(selected_count)) ;
                                updateDB(selected_count, votePlace);
                            }
                            else{
                                if(selected_count>0)
                                    selected_count--;
                                textView.setText(String.valueOf(selected_count)) ;
                                updateDB(selected_count, votePlace);
                            }
                            break;
                        case(2):
                            votePlace=voteList.get(2);
                            selected_count = Integer.parseInt(String.valueOf(votePlace.get("vote")));
                            //TextView textView3 = (TextView) findViewById(2);
                            textView= (TextView) findViewById(2);
                            if(v.isSelected()){//현재 좋아요 누른 상태
                                selected_count++;
                                textView.setText(String.valueOf(selected_count)) ;
                                updateDB(selected_count, votePlace);
                            }
                            else{
                                if(selected_count>0)
                                    selected_count--;
                                textView.setText(String.valueOf(selected_count)) ;
                                updateDB(selected_count, votePlace);
                            }
                            break;
                        case(3):
                            votePlace=voteList.get(3);
                            selected_count = Integer.parseInt(String.valueOf(votePlace.get("vote")));
                            //TextView textView4 = (TextView) findViewById(3);
                            textView= (TextView) findViewById(3);
                            if(v.isSelected()){//현재 좋아요 누른 상태
                                selected_count++;
                                textView.setText(String.valueOf(selected_count)) ;
                                updateDB(selected_count, votePlace);
                            }
                            else{
                                if(selected_count>0)
                                    selected_count--;
                                textView.setText(String.valueOf(selected_count)) ;
                                updateDB(selected_count, votePlace);
                            }
                            break;
                        case(4):
                            votePlace=voteList.get(4);
                            selected_count = Integer.parseInt(String.valueOf(votePlace.get("vote")));
                            //TextView textView5 = (TextView) findViewById(4);
                            textView= (TextView) findViewById(4);
                            if(v.isSelected()){//현재 좋아요 누른 상태
                                selected_count++;
                                textView.setText(String.valueOf(selected_count)) ;
                                updateDB(selected_count, votePlace);
                            }
                            else{
                                if(selected_count>0)
                                    selected_count--;
                                textView.setText(String.valueOf(selected_count)) ;
                                updateDB(selected_count, votePlace);
                            }
                            break;
                    }
                }
            });
        };
    }

    public void updateDB(int selected_count, HashMap<String, Object> votePlace){
        db.collection("vote").document(id).update("place", FieldValue.arrayRemove(votePlace));
        votePlace.put("vote", selected_count);
        db.collection("vote").document(id).update("place", FieldValue.arrayUnion(votePlace));
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
}
