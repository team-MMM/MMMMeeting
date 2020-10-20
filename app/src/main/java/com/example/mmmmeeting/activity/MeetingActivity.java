package com.example.mmmmeeting.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mmmmeeting.R;
import com.example.mmmmeeting.fragment.FragAccount;
import com.example.mmmmeeting.fragment.FragAlarm;
import com.example.mmmmeeting.fragment.FragChat;
import com.example.mmmmeeting.fragment.FragHome;
import com.example.mmmmeeting.fragment.FragBoard;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MeetingActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    TextView name, description;
    Button invite;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.frag_default);

        name = (TextView) findViewById(R.id.name);
        description = (TextView) findViewById(R.id.description);
        invite = (Button) findViewById(R.id.inviteBtn);

        Intent intent = getIntent();
        String meetingname = intent.getExtras().getString("Name");
        String meetingdescription = intent.getExtras().getString("Description");

        name.setText(meetingname);
        description.setText(meetingdescription);
        invite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MeetingActivity.this,inviteActivity.class);
                intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("Name",meetingname);
                intent.putExtra("Description",meetingdescription);
                startActivity(intent);
            }
        });


        FragHome fragHome = new FragHome();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_frame,fragHome)
                .commit();

        bottomNavigationView = findViewById(R.id.bottomNavi);
        // 메뉴 바 아이콘을 눌렀을 때의 화면 동작
        // 각 화면 코드는 fragment 폴더에 있음
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()){
                    // 홈 화면(약속 목록)으로 이동
                    case R.id.menu_home:
                        FragHome fragHome = new FragHome();
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.main_frame,fragHome)
                                .commit();
                        return true;
                    case R.id.menu_chat:
                        FragChat fragChat = new FragChat();
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.main_frame, fragChat)
                                .commit();
                        return true;
                    case R.id.menu_board:
                        FragBoard fragBoard = new FragBoard();
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.main_frame, fragBoard)
                                .commit();
                        return true;
                    case R.id.menu_alarm:
                        FragAlarm fragAlarm = new FragAlarm();
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.main_frame,fragAlarm)
                                .commit();
                        return true;
                    case R.id.menu_account:
                        FragAccount fragAccount = new FragAccount();
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.main_frame,fragAccount)
                                .commit();
                        return true;
                }
                return false;
            }
        });


    }

}
