package com.example.mmmmeeting.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.mmmmeeting.Info.GridItems;
import com.example.mmmmeeting.R;
import com.example.mmmmeeting.adapter.GridListAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class MainActivity extends BasicActivity implements View.OnClickListener {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    Button addMeeting;
    Button deleteMeeting;
    Button attendMeeting;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setToolbarTitle("우리 지금 만나");

        addMeeting = findViewById(R.id.meetingAdd);
        deleteMeeting = findViewById(R.id.meetingDelete);
        attendMeeting = findViewById(R.id.meetingAttend);

        addMeeting.setOnClickListener(this);
        deleteMeeting.setOnClickListener(this);
        attendMeeting.setOnClickListener(this);

    }

    @Override
    protected void onResume() {
        super.onResume();
        showMeetings();
    }

    // 모임 목록을 보여줌
    private void showMeetings(){
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final GridView gridView = findViewById(R.id.gridView);
        final GridListAdapter adapter = new GridListAdapter();

        mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 현재 user의 uid 확인 -> 모든 모임 정보를 확인
        // 모임 정보에 현재 유저의 uid가 존재하면 화면에 출력
        // 모임 정보에 현재 유저 uid 없으면 출력 X
        db.collection("meetings")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            //모든 document 확인
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if(document.getData().get("userID").toString().contains(user.getUid())){
                                    // 현재 유저가 존재하는 모임을 찾은 경우
                                    // adapter에 모임에 대한 정보를 갖는 객체 생성해 저장
                                    adapter.addItem(new GridItems(document.getData().get("name").toString(),document.getData().get("description").toString()));
                                    Log.d("Document Read", document.getId() + " => " + document.getData());
                                } else {
                                    Log.d("Document Snapshot", "No Document");
                                }
                            }
                            // 모든 문서 확인이 끝나면
                            // gridView에 adapter 할당 -> adapter에 있는 아이템 그리드 뷰로 화면에 출력
                            gridView.setAdapter(adapter);
                        } else {
                            Log.d("Document Read", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    //메뉴바 코드
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.setting,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.revoke:
                revokeAccess();
                finish();
                return true;

            case R.id.logout:
                signOut();
                finish();
                return true;

            case R.id.memberInfo:
                myStartActivity(MemberInitActivity.class);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    // 로그아웃 함수
    private void signOut(){
        FirebaseAuth.getInstance().signOut();
    }

    // 회원 탈퇴 함수
    private void revokeAccess() {
        db = FirebaseFirestore.getInstance();

        // 인증제거
        mAuth.getCurrentUser().delete();

        // user 테이블에서 현재 user uid로 저장된 문서 삭제
        db.collection("users").document(mAuth.getUid())
                .delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("revoke User","DocumentSnapshot successfully deleted!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("revoke User", "Error deleting document", e);
                    }
                });

//      현재 user가 속한 모임의 모임원 정보에서 uid 제거
        db.collection("meetings").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            //모든 document 확인
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // 현재 uid가 모임 정보에 존재하는 경우
                                if(document.getData().get("userID").toString().contains(mAuth.getUid())){
                                    // db에서 현재 유저 uid 삭제
                                    DocumentReference userdel = db.collection("meetings").document(document.getId());
                                    userdel.update("userID", FieldValue.arrayRemove(mAuth.getUid()));
                                    Log.d("Delete", document.getId() + " => " + document.getData());
                                    finish();
                                    break;
                                } else {
                                    Log.d("Delete", "No Document");
                                    finish();
                                }
                            }
                        } else {
                            Log.d("Delete", "Error getting documents: ", task.getException());
                        }
                    }
                });

        Toast.makeText(MainActivity.this, "회원탈퇴를 완료했습니다.", Toast.LENGTH_SHORT).show();
    }


    private void myStartActivity(Class c){
        Intent intent = new Intent(this,c);
        intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case (R.id.meetingAdd):
                myStartActivity(MakeMeetingActivity.class);
                break;
            case(R.id.meetingDelete):
                myStartActivity(MeetingDeleteActivity.class);
                break;
            case(R.id.meetingAttend):
                myStartActivity(MeetingAttendActivity.class);
                break;
        }

    }

}
