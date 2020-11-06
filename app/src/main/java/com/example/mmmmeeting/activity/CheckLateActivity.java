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

    private Handler handler;
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


        String apiKey = getString(R.string.api_key);

        meetingText = findViewById(R.id.meetingTime);
        attendanceBtn = findViewById(R.id.checkAttendBtn);
        attendanceBtn.setOnClickListener(this);


        db = FirebaseFirestore.getInstance();
        user = FirebaseAuth.getInstance().getCurrentUser();
        calendar = Calendar.getInstance();
        calDate = new Date();



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
                        //System.out.println(meetingDate);
                        calendar.setTime(meetingDate);
                        // 미팅 날짜 시간, 분으로 받아옴
                        hour = calendar.get(Calendar.HOUR_OF_DAY);
                        minute = calendar.get(Calendar.MINUTE);
                        // 미팅 시간을 저장함
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



    @Override
    protected void onDestroy() {
        super.onDestroy();
        thread.stopThread();

    }


    class MyThread extends Thread{
        boolean isRun = true;

        @Override
        public void run() {
            while(isRun){
                try {
                    thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                SimpleDateFormat sdf = new SimpleDateFormat("hh:mm:ss");
                Date now = new Date();
                String current = sdf.format(now);

                attendanceBtn.setVisibility(View.VISIBLE);
                // now > calDate : 현재 시간이 약속 시간을 지났을 때
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

    private void checkAttend(){

        Toast.makeText(getApplicationContext(),"출석체크 완료!",Toast.LENGTH_SHORT).show();
        try {
            thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        thread.stopThread();
        String date_text = new SimpleDateFormat(
                "hh시 mm분 ").format(calDate);
        System.out.println(date_text);
        Toast.makeText(getApplicationContext(),"약속 시간 " + date_text+"을 잘 지키셨군요!", Toast.LENGTH_SHORT).show();

        DocumentReference docRef = db.collection("schedule").document(scID);
        docRef.update("lateComer", FieldValue.arrayUnion(user.getUid()));

    }
}





