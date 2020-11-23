package com.example.mmmmeeting.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.mmmmeeting.R;
import com.example.mmmmeeting.fragment.FragAccount;
import com.example.mmmmeeting.fragment.FragAttend;
import com.example.mmmmeeting.fragment.FragBoard;
import com.example.mmmmeeting.fragment.FragCalendar;
import com.example.mmmmeeting.fragment.FragChat;
import com.example.mmmmeeting.fragment.FragHome;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class MeetingActivity extends BasicActivity {

    private BottomNavigationView bottomNavigationView;
    private Bundle bundle = new Bundle();

    private FragmentManager fm;
    private FragmentTransaction ft;
    private FragCalendar fragCalendar;
    private FragChat fragChat;
    private FragAttend fragAlarm;
    private FragAccount fragAccount;
    private Fragment fragment_ac;
    String meetingCode;

    private boolean fr_check = false;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.frag_default);
        String getName = getIntent().getExtras().getString("Name");
        meetingCode = getIntent().getExtras().getString("Code");

        Log.d("Grid send code ", "meetingCode is " + meetingCode);
        setToolbarTitle(getName);

        FragHome fragHome = new FragHome();
        bundle.putString("Code", meetingCode);
        fragHome.setArguments(bundle);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_frame, fragHome)
                .commit();
        fragment_ac = new Fragment();

        bottomNavigationView = findViewById(R.id.bottomNavi);
        // 메뉴 바 아이콘을 눌렀을 때의 화면 동작
        // 각 화면 코드는 fragment 폴더에 있음
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    // 홈 화면(약속 목록)으로 이동
                    case R.id.menu_home:
                        FragHome fragHome = new FragHome();
                        bundle.putString("Code", meetingCode);;
                        fragHome.setArguments(bundle);

                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.main_frame, fragHome)
                                .commit();
                        return true;
                    case R.id.menu_chat:
                        FragChat fragChat = new FragChat();
                        bundle.putString("Code", meetingCode);;
                        fragChat.setArguments(bundle);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.main_frame, fragChat)
                                .commit();
                        return true;
                    case R.id.menu_board:
                        FragBoard fragBoard = new FragBoard();
                        bundle.putString("Code", meetingCode);;
                        fragBoard.setArguments(bundle);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.main_frame, fragBoard)
                                .commit();

                        return true;
                    case R.id.menu_alarm:
                        FragAttend fragAlarm = new FragAttend();
                        bundle.putString("Code", meetingCode);;
                        fragAlarm.setArguments(bundle);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.main_frame, fragAlarm)
                                .commit();
                        return true;
                    case R.id.menu_account:
                        fragAccount = new FragAccount();
                        bundle.putString("Code", meetingCode);;
                        fragAccount.setArguments(bundle);

                        if (fr_check == false) {
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.main_frame, fragAccount)
                                    .commit();
                        }
                        if (fr_check) {
                            //getSupportFragmentManager().beginTransaction().hide(fragAccount).commit();
                            //fragAccount_result = new FragAccount_Result();
                            getSupportFragmentManager().beginTransaction().replace(R.id.main_frame, fragment_ac).commit();
                        }
                        return true;
                }
                return false;
            }
        });

    }



    //Fragment fragment,
    public void replaceFragment(Fragment fragment, boolean check) {
        if (check) {
            fragment_ac = fragment;
            getSupportFragmentManager().beginTransaction().replace(R.id.main_frame, fragment_ac).commit();
        }
        if (check == false) {
            fragAccount = new FragAccount();
            bundle.putString("Code", meetingCode);
            fragAccount.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().replace(R.id.main_frame, fragAccount).commit();
        }
        fr_check = check;
        //fragmentManager.beginTransaction().hide(fragAccount).commit();
    }

    //메뉴바 코드
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.meetinginfo, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.meetingInfo:
                // 리더인지 확인
                checkLeader();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void checkLeader() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        DocumentReference docRef = db.collection("meetings").document(meetingCode);

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // 해당 문서가 존재하는 경우
                        Log.d("Document Read", document.getId() + " => " + document.getData());

                        Intent intent = new Intent(MeetingActivity.this, MeetingInfoActivity.class);
                        intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra("Name", getIntent().getExtras().getString("Name"));
                        intent.putExtra("Description", getIntent().getExtras().getString("Description"));
                        intent.putExtra("Code", meetingCode);

                        // 해당 모임의 리더 확인
                        if (document.get("leader").toString().equals(mAuth.getUid())) {
                            intent.putExtra("isLeader",true);
                            startActivity(intent);
                            return;
                        }
                        else{
                            intent.putExtra("isLeader",false);
                            startActivity(intent);
                        }

                        Log.d("Attend", "Data is : " + document.getId());
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
}
