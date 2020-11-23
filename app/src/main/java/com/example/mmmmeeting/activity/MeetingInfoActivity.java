package com.example.mmmmeeting.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

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

public class MeetingInfoActivity extends AppCompatActivity {

    TextView name, description, code, user, leadertv;
    Button invite,changeLeader;
    String meetingname;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting_info);

        Intent intent = getIntent();
        meetingname = intent.getExtras().getString("Name");
        String meetingdescription = intent.getExtras().getString("Description");
        boolean isLeader = intent.getExtras().getBoolean("isLeader");

        name = findViewById(R.id.meetingName);
        description = findViewById(R.id.meetingDescription);
        code = findViewById(R.id.meetingCode);
        user = findViewById(R.id.meetingUsers);
        invite = findViewById(R.id.inviteBtn);
        leadertv = findViewById(R.id.meetingLeader);
        changeLeader = findViewById(R.id.newLeader);

        name.setText(meetingname);
        description.setText(meetingdescription);

        codeFind(meetingname);

        if(isLeader){
            changeLeader.setVisibility(View.VISIBLE);
        }

        // 초대 버튼 클릭시 -> inviteActivity 넘어가서 초대문자 보내기
        invite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MeetingInfoActivity.this, inviteActivity.class);
                intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("Name", meetingname);
                intent.putExtra("Code", code.getText().toString());
                startActivity(intent);
            }
        });

        changeLeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MeetingInfoActivity.this, newLeaderActivity.class);
                intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("Code", code.getText().toString());
                startActivity(intent);
                finish();
            }
        });

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
    private void codeFind(String meetingname) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("meetings").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            //모든 document 출력 (dou id + data arr { : , ... ,  })
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // 모임 이름이 같은 경우 해당 모임의 코드를 텍스트뷰에 출력
                                if (document.get("name").toString().equals(meetingname)) {
                                    code.setText(document.getId());
                                    // 찾은 모임의 사용자 확인
                                    userFind(document.getData().get("userID"));

                                    // 모임장 확인
                                    if(document.getData().get("leader").toString().length()==0){
                                        newLeader(document.getData().get("userID"),document.getId());
                                    }
                                    else {leaderFind(document.getData().get("leader").toString());}

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
        final String[] users = {""};

        // 모든 유저 정보를 확인 -> 모임에 속한 유저와 같은 uid 발견시 해당 유저의 이름을 출력
        db.collection("users").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            //모든 document 확인
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if (userID.toString().contains(document.getId())) {
                                    users[0] += document.get("name").toString() + "  ";
                                    Log.d("Document Read", document.getId() + " => " + document.getData());
                                } else {
                                    Log.d("Document Snapshot", "No Document");
                                }
                            }
                            // 찾은 유저 이름을 텍스트뷰에 설정
                            user.setText(Arrays.toString(users));
                        } else {
                            Log.d("Document Read", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }
}