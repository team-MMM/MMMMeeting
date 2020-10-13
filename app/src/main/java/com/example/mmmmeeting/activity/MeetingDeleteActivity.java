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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class MeetingDeleteActivity extends AppCompatActivity implements View.OnClickListener {

    Button delete;
    Button back3;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_meeting_delete);

        delete = findViewById(R.id.deleteBtn);
        back3 = findViewById(R.id.backBtn3);

        delete.setOnClickListener(this);
        back3.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.deleteBtn:
                checkCode();
                break;

            case R.id.backBtn3:
                myStartActivity(MainActivity.class);
                finish();
                break;
        }
    }

    private void checkCode() {
        final String code = ((EditText)findViewById(R.id.deleteCode)).getText().toString();
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

    private void checkUser(String code) {
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        db.collection("meetings")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            //모든 document 확인 (dou id + data arr { : , ... ,  })
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if(document.getData().get("userID").toString().contains(user.getUid())){
                                    DocumentReference userdel = db.collection("meetings").document(code);
                                    userdel.update("userID", FieldValue.arrayRemove(user.getUid()));
                                    startToast("모임에서 탈퇴했습니다.");
                                    Log.d("Delete", document.getId() + " => " + document.getData());
                                    myStartActivity(MainActivity.class);
                                    finish();
                                    break;
                                } else {
                                    Log.d("Delete", "No Document");
                                    startToast("해당 모임에 가입되어 있지 않습니다.");
                                    myStartActivity(MainActivity.class);
                                    finish();
                                }
                            }
                        } else {
                            Log.d("Delete", "Error getting documents: ", task.getException());
                        }
                    }
                });
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