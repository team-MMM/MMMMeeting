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
    Button btn_middle, btn_search;
    private ScheduleInfo postInfo;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_choice);

        postInfo = (ScheduleInfo) getIntent().getSerializableExtra("scheduleInfo");

        btn_middle = findViewById(R.id.middleBtn);
        btn_search = findViewById(R.id.searchBtn);

        btn_middle.setOnClickListener(this);
        btn_search.setOnClickListener(this);

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            // 중간 지점 찾기
            case (R.id.middleBtn):
                myStartActivity(MiddlePlaceActivity.class, postInfo);
                finish();
                break;
            // 장소 검색
            case(R.id.searchBtn):
                myStartActivity(SearchPlaceActivity.class, postInfo);
                finish();
                break;
        }

    }

    //Activity 이동하는 함수
    private void myStartActivity(Class c){
        Intent intent = new Intent(this,c);
        intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    private void myStartActivity(Class c, ScheduleInfo postInfo) {
        Intent intent = new Intent(this, c);
        intent.putExtra("scheduleInfo", postInfo);
        startActivityForResult(intent, 0);
    }
}
