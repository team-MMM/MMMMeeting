package com.example.mmmmeeting.activity;

import android.os.Bundle;
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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class CheckLateActivity extends AppCompatActivity implements View.OnClickListener {

    private int hour,minute;
    private Date calDate;
    private String scID;
    private boolean timeFlag;
    private boolean lateFlag;
    FirebaseUser user;
    FirebaseFirestore db;

    Calendar calendar;
    Calendar tempCal;
    TextView meetingText;
    Button attendanceBtn;

    MyThread thread;

    GoogleMap mMap;

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
        ScheduleInfo scInfo = (ScheduleInfo) getIntent().getSerializableExtra("scheduleInfo");
        scID=scInfo.getId();
        DocumentReference docRef = db.collection("schedule").document(scID);

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Date meetingDate =  document.getDate("meetingDate");
                        // 미팅 날짜를 get으로 편하게 받아오기 위해 캘린더 객체 생성
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

        thread = new MyThread();
        thread.start();

        //MyWorker.start(this);

    }

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

                // now > calDate : 현재 시간이 약속 시간을 지났을 때 약 2초 뒤에 창 종료됨
                if(now.compareTo(calDate) == 1){
                    lateFlag = true;
                    try {
                        thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    showToast();
                    break;
                }else if(nowHour == hour && nowMinute >= minute-5 ){
                    // 약속 시간 5분 전부터 flag true를 줘서 출석체크 버튼을 누를 수 있음
                    timeFlag = true;
                }
            }
            finish();

        }

        void stopThread(){
            isRun = false;
        }
    }

    public void showToast(){
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(),"약속 시간이 지났습니다.",Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.checkAttendBtn:
                checkAttend();
        }
    }

    private void checkAttend(){

        // 약속 시간이 지났을 때
        if(lateFlag == true){
            Toast.makeText(getApplicationContext(),"약속 시간이 지나서 출석체크 할 수 없습니다.",Toast.LENGTH_SHORT).show();
            lateFlag = false;
            return;
        }
        // 약속 시간이 지나지 않았지만 5분 전이 아닐 때
        if(timeFlag == false){
            Toast.makeText(getApplicationContext(),"만남 5분 전부터 출석체크 가능합니다.",Toast.LENGTH_SHORT).show();
            return;
        }

        // 약속 시간 5분 전일 때
        // 토스트가 너무 빨리 표시돼서 1초 딜레이
        try {
            thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Toast.makeText(getApplicationContext(),"출석체크 완료!",Toast.LENGTH_SHORT).show();

        thread.stopThread();
        String date_text = new SimpleDateFormat(
                "hh시 mm분").format(calDate);
        System.out.println(date_text);
        Toast.makeText(getApplicationContext(),"약속 시간 " + date_text+"을 잘 지키셨군요!", Toast.LENGTH_SHORT).show();
        // timeFlag 재설정
        timeFlag = false;
        // 스케쥴 lateComer에 출첵한 사람 저장 -> 타이틀 변경 예정
        DocumentReference docRef = db.collection("schedule").document(scID);
        docRef.update("lateComer", FieldValue.arrayUnion(user.getUid()));

    }


    // 종료될 때 뒤로 가기 버튼 누를 때 등등 thread도 종료되도록
    @Override
    protected void onDestroy() {
        super.onDestroy();
        thread.stopThread();

    }


    @Override
    protected void onStop() {
        super.onStop();
        if(thread!=null){
            thread.stopThread();
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if(thread!=null){
            thread.stopThread();
        }
    }


}





