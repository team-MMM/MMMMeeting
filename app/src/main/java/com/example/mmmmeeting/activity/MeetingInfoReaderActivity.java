package com.example.mmmmeeting.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mmmmeeting.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Arrays;

public class MeetingInfoReaderActivity extends AppCompatActivity {

    TextView name, description, code, user, readertv;
    Button invite, changeReader;
    String meetingname, meetingdescription ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting_info_reader);

        Intent intent = getIntent();
        meetingname = intent.getExtras().getString("Name");
        meetingdescription = intent.getExtras().getString("Description");

        name = findViewById(R.id.meetingName);
        description = findViewById(R.id.meetingDescription);
        code = findViewById(R.id.meetingCode);
        user = findViewById(R.id.meetingUsers);
        invite = findViewById(R.id.inviteBtn);
        readertv = findViewById(R.id.meetingReader);
        changeReader = findViewById(R.id.newReader);

        name.setText(meetingname);
        description.setText(meetingdescription);

        codeFind(meetingname);

        // 초대 버튼 클릭시 -> inviteActivity 넘어가서 초대문자 보내기
        invite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MeetingInfoReaderActivity.this, inviteActivity.class);
                intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("Name", meetingname);
                intent.putExtra("Code", code.getText().toString());
                startActivity(intent);
            }
        });

        changeReader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MeetingInfoReaderActivity.this, newReaderActivity.class);
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
                    Toast.makeText(MeetingInfoReaderActivity.this, "모임코드가 복사되었습니다.", Toast.LENGTH_SHORT).show();
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
                                    readerFind(document.getData().get("reader").toString());
                                    Log.d("Document Read", document.getId() + " => " + document.getData());
                                    break;
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

    private void readerFind(String reader) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("users").document(reader);

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        readertv.setText(document.get("name").toString());
                    } else {
                    }
                } else {
                    Log.d("Attend", "Task Fail : " + task.getException());
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