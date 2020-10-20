// 약속 내용 보여줌 + 날짜 정하기 / 모임 장소 정하기 화면

package com.example.mmmmeeting.activity;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.mmmmeeting.Info.ScheduleInfo;
import com.example.mmmmeeting.OnScheduleListener;
import com.example.mmmmeeting.R;
import com.example.mmmmeeting.ScheduleDeleter;
import com.example.mmmmeeting.fragment.FragCalendar;
import com.example.mmmmeeting.fragment.FragChat;
import com.example.mmmmeeting.fragment.FragHome;
import com.example.mmmmeeting.fragment.FragPlace;
import com.example.mmmmeeting.view.ReadScheduleView;

public class ContentScheduleActivity extends BasicActivity implements View.OnClickListener {
    private ScheduleInfo postInfo;
    private ScheduleDeleter boardDeleter;
    private ReadScheduleView readContentsVIew;
    private LinearLayout contentsLayout;
    Button btn_calendar, btn_place;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_schedule);


        postInfo = (ScheduleInfo) getIntent().getSerializableExtra("scheduleInfo");
        contentsLayout = findViewById(R.id.contentsLayout);
        readContentsVIew = findViewById(R.id.readScheduleView);

        btn_calendar = findViewById(R.id.calendarBtn);
        btn_place = findViewById(R.id.placeBtn);

        btn_calendar.setOnClickListener(this);
        btn_place.setOnClickListener(this);

        boardDeleter = new ScheduleDeleter(this);
        boardDeleter.setOnPostListener(onPostListener);
        uiUpdate();
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.calendarBtn:
                myStartActivity(CalendarActivity.class);
                break;

            case R.id.placeBtn:
                myStartActivity(PlaceChoiceActivity.class);
                break;
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 0:
                if (resultCode == Activity.RESULT_OK) {
                    postInfo = (ScheduleInfo)data.getSerializableExtra("scheduleInfo");
                    contentsLayout.removeAllViews();
                    uiUpdate();
                }
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.post, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.delete:
                boardDeleter.storageDelete(postInfo);
                return true;
            case R.id.modify:
                myStartActivity(MakeScheduleActivity.class, postInfo);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    OnScheduleListener onPostListener = new OnScheduleListener() {
        @Override
        public void onDelete(ScheduleInfo postInfo) {
            Log.e("로그 ","삭제 성공");
        }

        @Override
        public void onModify() {
            Log.e("로그 ","수정 성공");
        }
    };

    private void uiUpdate(){
        readContentsVIew.setScheduleInfo(postInfo);
    }

    private void myStartActivity(Class c, ScheduleInfo postInfo) {
        Intent intent = new Intent(this, c);
        intent.putExtra("scheduleInfo", postInfo);
        startActivityForResult(intent, 0);
    }

    private void myStartActivity(Class c) {
        Intent intent = new Intent(this,c);
        intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

}