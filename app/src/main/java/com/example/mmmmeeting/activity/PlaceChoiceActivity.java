// 중간 지점 또는 장소 검색을 선택하는 화면
package com.example.mmmmeeting.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mmmmeeting.Info.ScheduleInfo;
import com.example.mmmmeeting.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.List;

public class PlaceChoiceActivity extends AppCompatActivity implements View.OnClickListener {
    Button btn_middle, btn_search, btn_vote;
    String code;
    private ScheduleInfo scheduleInfo;
    private View layout_placechoice;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_place_choice);

        layout_placechoice=findViewById(R.id.layout_placechoice);
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
                                    finish();
                                }
                                else{
                                    final Snackbar snackbar = Snackbar.make(layout_placechoice, "모임원이 1명일 때 중간지점을 찾을 수 없어요", Snackbar.LENGTH_INDEFINITE)
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
