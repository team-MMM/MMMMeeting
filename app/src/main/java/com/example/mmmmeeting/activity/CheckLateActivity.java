package com.example.mmmmeeting.activity;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Timer;

public class CheckLateActivity extends AppCompatActivity implements View.OnClickListener {

    private int hour,minute;
    private Date calDate;
    private String scID;
    private long meetingTime;
    FirebaseUser user;
    FirebaseFirestore db;

    Calendar calendar;
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

                // 아직 약속 시간을 지나지 않았으니까 버튼 보이게 함
                attendanceBtn.setVisibility(View.VISIBLE);
                // now > calDate : 현재 시간이 약속 시간을 지났을 때 버튼 안 보이게, 창 종료됨
                if(now.compareTo(calDate) == 1){
                    attendanceBtn.setVisibility(View.INVISIBLE);
                    showToast();
                    break;
                }
                Log.d("my",current+" "+calDate);

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

        Toast.makeText(getApplicationContext(),"출석체크 완료!",Toast.LENGTH_SHORT).show();
        // 토스트가 너무 빨리 표시돼서 1초 딜레이
        try {
            thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        thread.stopThread();
        String date_text = new SimpleDateFormat(
                "hh시 mm분").format(calDate);
        System.out.println(date_text);
        Toast.makeText(getApplicationContext(),"약속 시간 " + date_text+"을 잘 지키셨군요!", Toast.LENGTH_SHORT).show();

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





