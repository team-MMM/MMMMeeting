package com.example.mmmmeeting.activity;
//AppCompatActivity
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.mmmmeeting.R;
import com.example.mmmmeeting.fragment.FragAccount;
import com.example.mmmmeeting.fragment.FragAlarm;
import com.example.mmmmeeting.fragment.FragCalendar;
import com.example.mmmmeeting.fragment.FragHome;
import com.example.mmmmeeting.fragment.FragPhoto;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MeetingActivity extends BasicActivity {

    private BottomNavigationView bottomNavigationView;
    private FragmentManager fm;
    private FragmentTransaction ft;
    private FragCalendar fragCalendar;
    private FragPhoto fragPhoto;
    private FragAlarm fragAlarm;
    private FragAccount fragAccount;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.frag_main);

        setToolbarTitle(getIntent().getExtras().getString("Name"));

        FragHome fragHome = new FragHome();
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_frame,fragHome)
                .commit();

        bottomNavigationView = findViewById(R.id.bottomNavi);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()){
                    case R.id.menu_home:
                        setFrag(0);
                        return true;
                    case R.id.menu_calendar:
                        setFrag(1);
                        return true;
                    case R.id.menu_photo:
                        setFrag(2);
                        return true;
                    case R.id.menu_alarm:
                        setFrag(3);
                        return true;
                    case R.id.menu_account:
                        setFrag(4);
                        return true;
                }
                return false;
            }
        });


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

    private void setFrag(int n){
        switch (n){
            case 0:
                FragHome fragHome = new FragHome();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_frame,fragHome)
                        .commit();
                break;
            case 1:
                FragCalendar fragCalendar = new FragCalendar();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_frame,fragCalendar)
                        .commit();
                break;
            case 2:
                FragPhoto fragPhoto = new FragPhoto();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_frame,fragPhoto)
                        .commit();
                break;
            case 3:
                FragAlarm fragAlarm = new FragAlarm();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_frame,fragAlarm)
                        .commit();
                break;
            case 4:
                FragAccount fragAccount = new FragAccount();
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.main_frame,fragAccount)
                        .commit();
                break;
        }
    }

}
