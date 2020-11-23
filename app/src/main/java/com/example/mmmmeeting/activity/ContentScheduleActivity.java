// 약속 내용 보여줌 + 날짜 정하기 / 모임 장소 정하기 화면

package com.example.mmmmeeting.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.example.mmmmeeting.Info.ScheduleInfo;
import com.example.mmmmeeting.OnScheduleListener;
import com.example.mmmmeeting.R;
import com.example.mmmmeeting.ScheduleDeleter;
import com.example.mmmmeeting.view.ReadScheduleView;

public class ContentScheduleActivity extends BasicActivity implements View.OnClickListener {
    private ScheduleInfo scheduleInfo;
    private ScheduleDeleter boardDeleter;
    private ReadScheduleView readContentsVIew;
    private LinearLayout contentsLayout;
    Button btn_calendar, btn_place, btn_attendance;
    String code;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_schedule);


        scheduleInfo = (ScheduleInfo) getIntent().getSerializableExtra("scheduleInfo");
        code = scheduleInfo.getMeetingID();

        contentsLayout = findViewById(R.id.contentsLayout);
        readContentsVIew = findViewById(R.id.readScheduleView);

        btn_calendar = findViewById(R.id.calendarBtn);
        btn_place = findViewById(R.id.placeBtn);
        btn_attendance = findViewById(R.id.attendanceBtn);

        btn_calendar.setOnClickListener(this);
        btn_place.setOnClickListener(this);
        btn_attendance.setOnClickListener(this);

        boardDeleter = new ScheduleDeleter(this);
        boardDeleter.setOnPostListener(onPostListener);
        uiUpdate();
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.calendarBtn:
                myStartActivity(CalendarActivity.class, scheduleInfo);
                break;

            case R.id.placeBtn:
                myStartActivity(PlaceChoiceActivity.class, scheduleInfo);
                break;

            case R.id.attendanceBtn:
                myStartActivity(CheckLateActivity.class, scheduleInfo);
                break;
        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 0:
                if (resultCode == Activity.RESULT_OK) {
                    scheduleInfo = (ScheduleInfo)data.getSerializableExtra("scheduleInfo");
                    System.out.println("I'mback");
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
                boardDeleter.storageDelete(scheduleInfo);
                return true;
            case R.id.modify:
                myStartActivity(MakeScheduleActivity.class, scheduleInfo);
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
        readContentsVIew.setScheduleInfo(scheduleInfo);
    }

    private void myStartActivity(Class c, ScheduleInfo schInfo) {
        Intent intent = new Intent(this, c);
        intent.putExtra("scheduleInfo", schInfo);
        intent.putExtra("Code",schInfo.getMeetingID());
        startActivityForResult(intent, 0);
    }

}
