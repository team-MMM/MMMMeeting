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

import com.example.mmmmeeting.Info.GridItems;
import com.example.mmmmeeting.R;
import com.example.mmmmeeting.view.ContentsItemView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Arrays;

public class MeetingInfoActivity extends AppCompatActivity {

    TextView name, description, code, user;
    Button invite, back4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting_info);

        Intent intent = getIntent();
        String meetingname = intent.getExtras().getString("Name");
        String meetingdescription = intent.getExtras().getString("Description");

        name = (TextView) findViewById(R.id.meetingName);
        description = (TextView) findViewById(R.id.meetingDescription);
        code = findViewById(R.id.meetingCode);
        user = findViewById(R.id.meetingUsers);
        invite = findViewById(R.id.inviteBtn);
        back4 = findViewById(R.id.backBtn4);

        name.setText(meetingname);
        description.setText(meetingdescription);

        invite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MeetingInfoActivity.this, inviteActivity.class);
                intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.putExtra("Name", meetingname);
                intent.putExtra("Description", meetingdescription);
                startActivity(intent);
            }
        });


        back4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        codeFind(meetingname);

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

    private void codeFind(String meetingname) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("meetings").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            //모든 document 출력 (dou id + data arr { : , ... ,  })
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if (document.get("name").toString().equals(meetingname)) {
                                    code.setText(document.getId());
                                    userFind(document.getData().get("userID"));
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

    private void userFind(Object userID) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final String[] users = {""};

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
                            user.setText(Arrays.toString(users));
                        } else {
                            Log.d("Document Read", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }
}