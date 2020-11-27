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
import com.example.mmmmeeting.view.ReadScheduleView_new;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

import androidx.annotation.NonNull;

public class ContentScheduleActivity extends BasicActivity implements View.OnClickListener {
    private ScheduleInfo scheduleInfo;
    private ScheduleDeleter boardDeleter;
    private ReadScheduleView_new readContentsVIew;
    private LinearLayout contentsLayout;
    Button btn_calendar, btn_place, btn_attendance;
    Button btn_middle, btn_search, btn_vote;
    private View content_schedule;
    String code;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_schedule);
        content_schedule=findViewById(R.id.content_schedule);


        scheduleInfo = (ScheduleInfo) getIntent().getSerializableExtra("scheduleInfo");
        code = scheduleInfo.getMeetingID();

        contentsLayout = findViewById(R.id.contentsLayout);
        readContentsVIew = findViewById(R.id.readScheduleView_new);

        btn_calendar = findViewById(R.id.calendarBtn);
        btn_attendance = findViewById(R.id.attendanceBtn);

        btn_middle = findViewById(R.id.middleBtn);
        btn_search = findViewById(R.id.searchBtn);
        btn_vote = findViewById(R.id.voteBtn);

        btn_middle.setOnClickListener(this);
        btn_search.setOnClickListener(this);
        btn_vote.setOnClickListener(this);

        btn_calendar.setOnClickListener(this);
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

            // 중간 지점 찾기
            case (R.id.middleBtn):
                FirebaseFirestore db = FirebaseFirestore.getInstance();
                db.collection("meetings").document(code)
                        .get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                // 해당 문서가 존재하는 경우
                                // document에서 이름이 userID인 필드의 데이터 얻어옴
                                List users = (List) document.getData().get("userID");
                                if(users.size()!=1){
                                    myStartActivity(MiddlePlaceActivity.class, scheduleInfo);
                                }
                                else{
                                    final Snackbar snackbar = Snackbar.make(content_schedule, "모임원이 1명일 때 중간지점을 찾을 수 없어요", Snackbar.LENGTH_INDEFINITE)
                                            .setActionTextColor(getColor(R.color.colorPrimary));
                                    snackbar.setAction("확인", new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            snackbar.dismiss();
                                        }
                                    });
                                    snackbar.show();
                                }
                            } else {
                                // 존재하지 않는 문서
                                Log.d("Attend", "No Document");
                            }
                        } else {
                            Log.d("Attend", "Task Fail : " + task.getException());
                        }

                    }
                });
                break;
            // 장소 검색
            case(R.id.searchBtn):
                myStartActivity(SearchPlaceActivity.class, scheduleInfo);
                break;
            // 장소 투표
            case(R.id.voteBtn):
                myStartActivity(VoteActivity.class, scheduleInfo);
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
