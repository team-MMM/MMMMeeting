package com.example.mmmmeeting.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.mmmmeeting.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class newLeaderActivity extends AppCompatActivity {

    Button changeLeader;
    EditText newLeaderName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_leader);
        changeLeader = findViewById(R.id.changeBtn);
        newLeaderName = findViewById(R.id.newLeaderName);

        changeLeader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getIntent();
                String username = newLeaderName.getText().toString();
                String meetingCode =  intent.getExtras().getString("Code");
                newLeader(username,meetingCode);
                finish();
            }
        });
    }

    private void newLeader(String user, String code) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 모임 사용자 중 같은 이름이 존재
        db.collection("meetings").document(code).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                // 같은 이름 있나 확인
                                userNameCheck(user, document.get("userID"), code);
                            } else {
                            }
                        } else {
                            Log.d("Attend", "Task Fail : " + task.getException());
                        }
                    }
                });
    }


    private void userNameCheck(String userName, Object userID, String code) {
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
                                    if(document.get("name").toString().equals(userName)){
                                        db.collection("meetings").document(code).update("leader",document.getId());
                                        Toast.makeText(newLeaderActivity.this, "모임장이 변경되었습니다", Toast.LENGTH_SHORT).show();
                                        Log.d("Reader Read", document.getId() + " => " + document.getData());
                                        return;
                                    }
                                    Log.d("Document Read", document.getId() + " => " + document.getData());
                                }
                            }
                            Toast.makeText(newLeaderActivity.this, "이름을 다시 확인하세요.", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.d("Document Read", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

}