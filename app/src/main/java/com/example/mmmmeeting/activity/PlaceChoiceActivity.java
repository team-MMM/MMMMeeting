// 중간 지점 또는 장소 검색을 선택하는 화면
package com.example.mmmmeeting.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mmmmeeting.Info.ScheduleInfo;
import com.example.mmmmeeting.R;

public class PlaceChoiceActivity extends AppCompatActivity implements View.OnClickListener {
    Button btn_middle, btn_search, btn_vote;
    String code;
    private ScheduleInfo scheduleInfo;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_choice);

        scheduleInfo = (ScheduleInfo) getIntent().getSerializableExtra("scheduleInfo");
        code = scheduleInfo.getMeetingID();

        btn_middle = findViewById(R.id.middleBtn);
        btn_search = findViewById(R.id.searchBtn);
        btn_vote = findViewById(R.id.voteBtn);

        btn_middle.setOnClickListener(this);
        btn_search.setOnClickListener(this);
        btn_vote.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            // 중간 지점 찾기
            case (R.id.middleBtn):
                myStartActivity(MiddlePlaceActivity.class, scheduleInfo);
                finish();
                break;
            // 장소 검색
            case(R.id.searchBtn):
                myStartActivity(SearchPlaceActivity.class, scheduleInfo);
                finish();
                break;
            // 장소 투표
            case(R.id.voteBtn):
                myStartActivity(VoteActivity.class, scheduleInfo);
                finish();
                break;
        }

    }


    private void myStartActivity(Class c, ScheduleInfo schInfo) {
        Intent intent = new Intent(this, c);
        intent.putExtra("scheduleInfo", schInfo);
        intent.putExtra("Code",schInfo.getMeetingID());
        startActivityForResult(intent, 0);
    }
}
