package com.example.mmmmeeting.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class MeetingDeleteActivity extends AppCompatActivity {


    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        String code = intent.getExtras().getString("Code");
        checkCode(code);
    }

    // 입력한 코드가 존재하는지 확인
    private void checkCode(String code) {
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

                    if (document.getData().get("userID").toString().contains(user.getUid())) {
                        // db에서 현재 유저 uid 삭제
                        if(document.get("leader").equals(user.getUid())){
                            userdel.update("leader", "");
                        }
                        userdel.update("userID", FieldValue.arrayRemove(user.getUid()));
                        startToast("모임에서 탈퇴했습니다.");
                        Log.d("Delete2", document.getId() + " => " + document.getData());

                        meetingMemberCheck(code); //모임의 모임원이 아무도 없는지 확인 후 없으면 모임 제거거

                        myStartActivity(MainActivity.class);
                        finish();
                    } else {
                        Log.d("Delete", "No Document");
                        startToast("해당 모임에 가입되어 있지 않습니다.");
                        myStartActivity(MainActivity.class);
                        finish();
                    }

                } else {
                    Log.d("Delete", "Task Fail : " + task.getException());
                }
            }
        });
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
                        // 스케쥴, 게시판 DB에서 모임 이름으로 찾아서 삭제
                        scheduleDelete(code);
                        boardDelete(code);
                        chatDelete(code);

                        meetingdel.delete();
                    }
                } else {
                    Log.d("Delete", "Task Fail : " + task.getException());
                }
            }
        });
    }


    private void scheduleDelete(String code){
        CollectionReference scheduleDel = db.collection("schedule");
        scheduleDel.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            //모든 document 확인
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // 문서가 참조하는 이름이 삭제 해야하는 모임 이름과 같으면 일정 삭제
                                if(document.getData().get("meetingID").toString().equals(code)){
                                    Log.d("일정 삭제",document.getData().get("title").toString());
                                    scheduleDel.document(document.getId()).delete();
                                    return;
                                }
                            }
                        } else {
                            Log.d("Document Read", "Error getting documents: ", task.getException());
                        }
                    }
                });

    }

    private void boardDelete(String code){
        CollectionReference boardDel = db.collection("posts");
        boardDel.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            //모든 document 확인
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // 문서가 참조하는 이름이 삭제 해야하는 모임 이름과 같으면 게시글 삭제
                                if(document.getData().get("meetingID").toString().equals(code)){
                                    Log.d("일정 삭제",document.getData().get("title").toString());
                                    boardDel.document(document.getId()).delete();
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

    private void chatDelete(String code){
        FirebaseDatabase firebaseDatabase= FirebaseDatabase.getInstance();
        DatabaseReference chatRef= firebaseDatabase.getReference("chat").child(code);
        chatRef.removeValue();
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