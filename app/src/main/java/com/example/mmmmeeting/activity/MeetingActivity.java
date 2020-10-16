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
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.mmmmeeting.R;
import com.example.mmmmeeting.fragment.FragAccount;
import com.example.mmmmeeting.fragment.FragAlarm;
import com.example.mmmmeeting.fragment.FragCalendar;
import com.example.mmmmeeting.fragment.FragHome;
import com.example.mmmmeeting.fragment.FragPhoto;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MeetingActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    TextView name, description;
    Button invite;

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
