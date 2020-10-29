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

import com.example.mmmmeeting.Info.GridItems;
import com.example.mmmmeeting.R;
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

public class MeetingDeleteActivity extends AppCompatActivity {

    Button delete;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting_delete);

        delete = findViewById(R.id.deleteBtn);

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkCode();
            }
        });
    }

    // 입력한 코드가 존재하는지 확인
    private void checkCode() {
        final String code = ((EditText) findViewById(R.id.deleteCode)).getText().toString();
        DocumentReference docRef = db.collection("meetings").document(code);

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("Delete", "Data is : " + document.getId());
                        checkUser(code);
                    } else {
                        Log.d("Delete", "No Document");
                        startToast("존재하지 않는 코드입니다.");
                        myStartActivity(MainActivity.class);
                        finish();
                    }
                } else {
                    Log.d("Delete", "Task Fail : " + task.getException());
                }
            }

        });
    }

    // 존재하는 코드인 경우 해당 모임에 현재 유저가 존재하는지 확인
    private void checkUser(String code) {
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        DocumentReference userdel = db.collection("meetings").document(code);

        userdel.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // 해당 문서가 존재하는 경우
                        if (document.getData().get("userID").toString().contains(user.getUid())) {
                            // db에서 현재 유저 uid 삭제
                            userdel.update("userID", FieldValue.arrayRemove(user.getUid()));
                            startToast("모임에서 탈퇴했습니다.");
                            Log.d("Delete2", document.getId() + " => " + document.getData());
                            Log.d("Delete2", document.getId() + " => " + document.getData().get("userId"));

                            // 모임원 없는 모임이 된 경우 모임 삭제
                            if(document.getData().get("userId")==null){
                                // 모임에 있는 약속, 게시판 내용도 전부 삭제해야..
                                userdel.delete();
                            }

                            myStartActivity(MainActivity.class);
                            finish();
                        } else {
                            Log.d("Delete", "No Document");
                            startToast("해당 모임에 가입되어 있지 않습니다.");
                            myStartActivity(MainActivity.class);
                            finish();
                        }
                    } else {
                        // 존재하지 않는 문서
                        Log.d("Delete", "No Document");
                    }
                } else {
                    Log.d("Delete", "Task Fail : " + task.getException());
                }
            }
        });
    }

    private void startToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void myStartActivity(Class c) {
        Intent intent = new Intent(this, c);
        intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

}