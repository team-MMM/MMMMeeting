package com.example.mmmmeeting.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.mmmmeeting.R;
import com.example.mmmmeeting.fragment.FragAccount;
import com.example.mmmmeeting.fragment.FragAccount_Result;
import com.example.mmmmeeting.fragment.FragAlarm;
import com.example.mmmmeeting.fragment.FragBoard;
import com.example.mmmmeeting.fragment.FragCalendar;
import com.example.mmmmeeting.fragment.FragChat;
import com.example.mmmmeeting.fragment.FragHome;
import com.example.mmmmeeting.fragment.FragChat;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MeetingActivity extends BasicActivity {

    private BottomNavigationView bottomNavigationView;
    private Bundle bundle = new Bundle();
    TextView name, description;
    Button invite;

    private FragmentManager fm;
    private FragmentTransaction ft;
    private FragCalendar fragCalendar;
    private FragChat fragChat;
    private FragAlarm fragAlarm;
    private FragAccount fragAccount;
    private Fragment fragment_ac;

    private boolean fr_check=false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.frag_default);
        setToolbarTitle(getIntent().getExtras().getString("Name"));

        FragHome fragHome = new FragHome();
        bundle.putString("Name", getIntent().getExtras().getString("Name"));
        fragHome.setArguments(bundle);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_frame,fragHome)
                .commit();
        fragment_ac = new Fragment();

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
                        bundle.putString("Name", getIntent().getExtras().getString("Name"));
                        fragHome.setArguments(bundle);

                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.main_frame,fragHome)
                                .commit();
                        return true;
                    case R.id.menu_chat:
                        FragChat fragChat = new FragChat();
                        bundle.putString("Name", getIntent().getExtras().getString("Name"));
                        fragChat.setArguments(bundle);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.main_frame, fragChat)
                                .commit();
                        return true;
                    case R.id.menu_board:
                        FragBoard fragBoard = new FragBoard();
                        bundle.putString("Name", getIntent().getExtras().getString("Name"));
                        fragBoard.setArguments(bundle);
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
                        fragAccount = new FragAccount();
                        bundle.putString("Name", getIntent().getExtras().getString("Name"));
                        fragAccount.setArguments(bundle);

                        if(fr_check==false) {
                            getSupportFragmentManager().beginTransaction()
                                    .replace(R.id.main_frame, fragAccount)
                                    .commit();
                        }
                        if(fr_check){
                            //getSupportFragmentManager().beginTransaction().hide(fragAccount).commit();
                            //fragAccount_result = new FragAccount_Result();
                            getSupportFragmentManager().beginTransaction().replace(R.id.main_frame,fragment_ac).commit();
                        }
                        return true;
                }
                return false;
            }
        });


    }

    //Fragment fragment,
    public void replaceFragment(Fragment fragment,boolean check) {
        if(check){
            fragment_ac = fragment;
            getSupportFragmentManager().beginTransaction().replace(R.id.main_frame,fragment_ac).commit();
        }
        if(check==false){
            fragAccount = new FragAccount();
            bundle.putString("Name", getIntent().getExtras().getString("Name"));
            fragAccount.setArguments(bundle);
            getSupportFragmentManager().beginTransaction().replace(R.id.main_frame,fragAccount).commit();
        }
        fr_check=check;
        //fragmentManager.beginTransaction().hide(fragAccount).commit();
    }

    //메뉴바 코드
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.meetinginfo,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.meetingInfo:
                // 미팅 정보 아이콘 클릭시 미팅 정보 출력하는 액티비티로 이동
                Intent intent = new Intent(MeetingActivity.this, MeetingInfoActivity.class);
                intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("Name",getIntent().getExtras().getString("Name"));
                intent.putExtra("Description",getIntent().getExtras().getString("Description"));
                startActivity(intent);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }
    //

}
