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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;

public class MeetingAttendActivity extends AppCompatActivity implements View.OnClickListener {

    Button attend;
    Button back2;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting_attend);

        attend = findViewById(R.id.attendBtn);
        back2 = findViewById(R.id.backBtn2);
        attend.setOnClickListener(this);
        back2.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.attendBtn:
                checkCode();
                break;
            case R.id.backBtn2:
                myStartActivity(MainActivity.class);
                finish();
        }
    }

    // 입력한 코드가 db에 존재하는 미팅 코드인지 확인
    private void checkCode() {
        final String code = ((EditText)findViewById(R.id.attendCode)).getText().toString();
        DocumentReference docRef = db.collection("meetings").document(code);

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // 입력한 코드에 해당하는 문서가 존재하는 경우
                        Log.d("Attend", "Data is : " + document.getId());
                        updateUser(code);
                    } else {
                        Log.d("Attend", "No Document");
                        // 3. code 없으면 dialog or toast Message -> 존재하지 않음 알리고 종료, 메인으로 복귀
                        startToast("존재하지 않는 코드입니다.");
                        myStartActivity(MainActivity.class);
                        finish();
                    }
                } else {
                    Log.d("Attend", "Task Fail : " + task.getException());
                }
            }

        });
    }

    private void updateUser(String code) {
        // 2. code 존재시 code document -> userID -> 현재 UID add
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        DocumentReference newUser = db.collection("meetings").document(code);
        newUser.update("userID", FieldValue.arrayUnion(user.getUid()));
        startToast("새로운 모임에 참가했습니다.");
        myStartActivity(MainActivity.class);
        finish();
    }

    private void startToast(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void myStartActivity(Class c){
        Intent intent = new Intent(this,c);
        intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

}