package com.example.mmmmeeting.activity;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class MainActivity extends BasicActivity implements View.OnClickListener {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    Button addMeeting;
    Button attendMeeting;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setToolbarTitle("우리 지금 만나");

        addMeeting = findViewById(R.id.meetingAdd);
        attendMeeting = findViewById(R.id.meetingAttend);

        addMeeting.setOnClickListener(this);
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

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("회원 탈퇴")        // 제목
                        .setMessage( "우리 지금 만나 앱을 정말로 탈퇴하시겠습니까?")        // 메세지
                        // .setCancelable(false)        // 뒤로 버튼 클릭시 취소 가능 설정
                        .setPositiveButton("확인", new DialogInterface.OnClickListener(){
                            // 확인 버튼 클릭시 설정, 오른쪽 버튼입니다.
                            public void onClick(DialogInterface dialog, int whichButton){
                                revokeAccess();
                                finish();
                            }
                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener(){// 취소 버튼 클릭시
                            public void onClick(DialogInterface dialog, int whichButton){//취소 이벤트...
                            }
                        });
                AlertDialog dialog = builder.create();    // 알림창 객체 생성
                dialog.show();    // 알림창 띄우기

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
                                    if(document.get("leader").equals(mAuth.getUid())){
                                        userdel.update("leader", "");
                                    }
                                    userdel.update("userID", FieldValue.arrayRemove(mAuth.getUid()));
                                    Log.d("Delete", document.getId() + " => " + document.getData());

                                    meetingMemberCheck(document.getId());

                                    finish();
                                    return;
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

    private void meetingMemberCheck(String code) {
        DocumentReference meetingdel = db.collection("meetings").document(code);

        meetingdel.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    Log.d("Delete3", document.get("userID").toString());
                    // 모임원 없는 모임이 된 경우 모임 삭제

                    Log.d("Delete3 Test", "users:"+(document.get("userID").toString().length()));
                    // 모임원 없음 = 2
                    // 모임원 한 명 = 30
                    // 이 후 모임원 한 명 증가시마다 length +30 씩 증가

                    if (document.get("userID").toString().length()==2) {
                        // 모임에 있는 약속, 게시판 내용도 전부 삭제해야..
                        meetingdel.delete();
                    }
                } else {
                    Log.d("Delete", "Task Fail : " + task.getException());
                }
            }
        });
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
            case(R.id.meetingAttend):
                myStartActivity(MeetingAttendActivity.class);
                break;
        }

    }

}
