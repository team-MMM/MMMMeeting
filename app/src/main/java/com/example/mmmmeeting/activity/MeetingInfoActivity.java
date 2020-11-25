package com.example.mmmmeeting.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.mmmmeeting.Info.MeetingInfo;
import com.example.mmmmeeting.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Arrays;
import java.util.List;

public class MeetingInfoActivity extends AppCompatActivity implements View.OnClickListener{

    private TextView name, description, code, user, leadertv;
    private Button invite, delete, changeLeader;
    private String meetingname;
    private int num;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting_info);

        Intent intent = getIntent();
        meetingname = intent.getExtras().getString("Name");
        String meetingdescription = intent.getExtras().getString("Description");
        String meetingCode = intent.getExtras().getString("Code");
        boolean isLeader = intent.getExtras().getBoolean("isLeader");

        name = findViewById(R.id.meetingName);
        description = findViewById(R.id.meetingDescription);
        code = findViewById(R.id.meetingCode);
        user = findViewById(R.id.meetingUsers);
        invite = findViewById(R.id.inviteBtn);
        delete = findViewById(R.id.deleteBtn);
        changeLeader = findViewById(R.id.newLeader);
        leadertv = findViewById(R.id.meetingLeader);


        if(isLeader){
            changeLeader.setVisibility(View.VISIBLE);
        }

        invite.setOnClickListener(this);
        delete.setOnClickListener(this);
        changeLeader.setOnClickListener(this);

        name.setText(meetingname);
        description.setText(meetingdescription);
        code.setText(meetingCode);

        readmeeting(meetingCode);

        // 코드가 출력되는 TextView 클릭시 -> 코드 클립보드에 복사되도록
        code.setOnTouchListener(new View.OnTouchListener(){ //터치 이벤트 리스너 등록(누를때)
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction()==MotionEvent.ACTION_DOWN){ //눌렀을 때 동작
                    //클립보드 사용 코드
                    ClipboardManager clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
                    ClipData clipData = ClipData.newPlainText("Code",code.getText().toString()); //클립보드에 ID라는 이름표로 id 값을 복사하여 저장
                    Toast.makeText(MeetingInfoActivity.this, "모임코드가 복사되었습니다.", Toast.LENGTH_SHORT).show();
                    clipboardManager.setPrimaryClip(clipData);
                }
                return true;
            }
        });

    }

    // 모임코드 출력 위해 현재 모임 이름에 해당하는 모임 코드 찾기
    private void readmeeting(String meetingCode) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        DocumentReference docRef = db.collection("meetings").document(meetingCode);

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // 해당 문서가 존재하는 경우
                        // 찾은 모임의 사용자 확인
                        List list = (List) document.getData().get("userID");
                        num = list.size();

                        userFind(document.getData().get("userID"));

                        // 모임장 확인
                        if(document.getData().get("leader").toString().length()==0){
                            newLeader(document.getData().get("userID"),document.getId());
                        }
                        else {leaderFind(document.getData().get("leader").toString());}

                        Log.d("Attend", "Data is : " + document.getId());
                    } else {
                        // 존재하지 않는 문서
                        Log.d("Attend", "No Document");
                    }
                } else {
                    Log.d("Attend", "Task Fail : " + task.getException());
                }
            }

        });
    }

    private void leaderFind(String leader) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("users").document(leader);

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        leadertv.setText(document.get("name").toString());
                    } else {
                    }
                } else {
                    Log.d("Attend", "Task Fail : " + task.getException());
                }
            }
        });
    }

    private void newLeader(Object userID, String code) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 모든 유저 정보를 확인 -> 모임에 속한 유저와 같은 uid 발견시 가장 먼저 발견한 유저 리더로
        db.collection("users").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            //모든 document 확인
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if (userID.toString().contains(document.getId())) {
                                    db.collection("meetings").document(code).update("leader",document.getId());
                                    leaderFind(document.getId());
                                    Log.d("Document Read", document.getId() + " => " + document.getData());
                                    return;
                                } else {
                                    Log.d("Document Snapshot", "No Document");
                                }
                            }
                        } else {
                            Log.d("Document Read", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    private void userFind(Object userID) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 모든 유저 정보를 확인 -> 모임에 속한 유저와 같은 uid 발견시 해당 유저의 이름을 출력
        db.collection("users").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            //모든 document 확인
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if (userID.toString().contains(document.getId())) {
                                    String username =document.get("name").toString() + "  ";

                                    // 찾은 유저 이름을 텍스트뷰에 설정
                                    user.append(username);

                                    Log.d("Document Read", document.getId() + " => " + document.getData());
                                } else {
                                    Log.d("Document Snapshot", "No Document");
                                }
                            }
                        } else {
                            Log.d("Document Read", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

    private void myStartActivity(Class c, TextView code){
        Intent intent = new Intent(this,c);
        intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("Code", code.getText().toString());
        startActivity(intent);
    }

    private void myStartActivity(Class c, String meetingname, TextView code){
        Intent intent = new Intent(this,c);
        intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("Name", meetingname);
        intent.putExtra("Code", code.getText().toString());
        startActivity(intent);
    }

    private void myStartActivity(Class c){
        Intent intent = new Intent(this,c);
        intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("Code", code.getText().toString());
        startActivity(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case (R.id.inviteBtn):
                myStartActivity(inviteActivity.class, meetingname, code);
                break;
            case (R.id.newLeader):
                if(num == 1){
                    Toast.makeText(this,"양도할 사람이 없습니다.",Toast.LENGTH_SHORT).show();
                }else {
                    myStartActivity(newLeaderActivity.class, code);
                    finish();
                }
                break;
            case(R.id.deleteBtn):
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("모임 탈퇴")        // 제목
                        .setMessage( "[ "+meetingname+" ]"+ " 모임을 정말로 나가시겠습니까?")        // 메세지
                        // .setCancelable(false)        // 뒤로 버튼 클릭시 취소 가능 설정
                        .setPositiveButton("확인", new DialogInterface.OnClickListener(){
                            // 확인 버튼 클릭시 설정, 오른쪽 버튼입니다.
                            public void onClick(DialogInterface dialog, int whichButton){//약속 날짜를 확정 //db로 해당 날짜 올리기
                                myStartActivity(MeetingDeleteActivity.class, code);
                            }
                        })
                        .setNegativeButton("취소", new DialogInterface.OnClickListener(){// 취소 버튼 클릭시
                            public void onClick(DialogInterface dialog, int whichButton){//취소 이벤트...
                            }
                        });
                AlertDialog dialog = builder.create();    // 알림창 객체 생성
                dialog.show();    // 알림창 띄우기
                break;

        }
    }



    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}