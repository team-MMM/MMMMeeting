package com.example.mmmmeeting.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mmmmeeting.Info.ScheduleInfo;
import com.example.mmmmeeting.R;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.type.LatLng;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class CheckLateActivity extends AppCompatActivity implements View.OnClickListener {

    private ScheduleInfo postInfo;
    private int hour,minute;
    private Date calDate;
    private String scID;
    FirebaseUser user;
    FirebaseFirestore db;

    Calendar calendar;
    Calendar tempCal;
    TextView meetingText;
    Button attendanceBtn;
    Date meetingDate;
    String place;

    Handler handler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON,
                WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_check_late);


        meetingText = findViewById(R.id.meetingTime);
        attendanceBtn = findViewById(R.id.checkAttendBtn);
        attendanceBtn.setOnClickListener(this);


        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        calendar = Calendar.getInstance();
        calDate = new Date();
        tempCal = Calendar.getInstance();

        // 해당 일정의 ID를 통해 meetingDate를 찾아냄

        postInfo = (ScheduleInfo) getIntent().getSerializableExtra("scheduleInfo");
        scID=postInfo.getId();
        DocumentReference docRef = db.collection("schedule").document(scID);

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        meetingDate =  document.getDate("meetingDate");
                        Map<String, String> placeMap = (Map<String, String>) document.getData().get("meetingPlace");

                        if(placeMap!=null){
                            place = placeMap.get("name");
                        }
                        // 미팅 날짜를 get으로 편하게 받아오기 위해 캘린더 객체 생성
                        if(meetingDate == null){
                            meetingText.setText("약속 일정이 정해지지 않았습니다.");
                            return;
                        }

                        calendar.setTime(meetingDate);
                        hour = calendar.get(Calendar.HOUR_OF_DAY);
                        minute = calendar.get(Calendar.MINUTE);
                        calDate = calendar.getTime();

                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일 a hh : mm");
                        meetingText.setText(sdf.format(calDate));

                    } else {
                        Log.d("Attend", "No Document");
                    }
                } else {
                    Log.d("Attend", "Task Fail : " + task.getException());
                }
            }

        });

    }
/*
    class MyThread extends Thread{
        boolean isRun = true;

        @Override
        public void run() {
            while(isRun){
                // 1초 간격으로 시간 갱신 -> 실제 시간 받아올 수 있음(정확한지는 모르겠음)
                try {
                    thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // 확인용 String 생성
                SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss");
                Date now = new Date();
                String current = sdf.format(now);

                // 약속 시간 5분전인지 알기 위해 시, 분 받아옴
                tempCal.setTime(now);
                int nowHour = tempCal.get(Calendar.HOUR_OF_DAY);
                int nowMinute = tempCal.get(Calendar.MINUTE);

                Log.d("my",current+" "+calDate);

                if(nowHour >= hour && nowMinute > minute){
                    // flag를 없애고 bundle로 값을 전달해줌
                    Bundle bd = new Bundle();
                    bd.putString("arg", "TimeCheck");
                    sendMessage(bd);
                    break;
                // 약속 시간이 3:00과 같이 5분보다 작아서 시단위가 바뀌는 경우
                }else if(minute<5){
                    if(nowHour == hour -1 && nowMinute >= minute + 60 - 5){
                        Bundle bd = new Bundle();
                        bd.putString("arg","TimeCheck");
                        sendMessage(bd);
                        break;
                    }
                }else if(nowHour == hour && nowMinute >= minute-5){
                    Bundle bd = new Bundle();
                    bd.putString("arg","TimeCheck");
                    sendMessage(bd);
                    break;
                }else{
                    Bundle bd = new Bundle();
                    bd.putString("arg","TimeCheck");
                    sendMessage(bd);
                    break;
                }
            }

        }

        void stopThread(){
            isRun = false;
        }
    }

    private void sendMessage(Bundle bd){
        Message msg = handler.obtainMessage();
        msg.setData(bd);
        handler.sendMessage(msg);
    }

*/

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.checkAttendBtn:
                // 버튼 누르면 thread 실행해서 시간 받아오고 바로 종료되게 함
                // -> thread를 계속 실행하니까 flag값이 계속 바뀜
                if(meetingDate == null || place == null){
                    Toast.makeText(getApplicationContext(),"약속 시간 또는 장소가 정해지지 않았습니다.",Toast.LENGTH_SHORT).show();
                    return;
                }
                System.out.println("meetingID"+postInfo.getMeetingID());
                myStartActivity(CurrentMapActivity.class, postInfo, hour, minute);

        }
    }


    private void myStartActivity(Class c, ScheduleInfo postInfo, int hour, int minute) {
        Intent intent = new Intent(this,c);
        intent.putExtra("scheduleInfo", postInfo);
        intent.putExtra("hour", hour);
        intent.putExtra("minute", minute);
        startActivityForResult(intent, 0);
    }


}





