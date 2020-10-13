package com.example.mmmmeeting.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;

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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class MainActivity extends BasicActivity implements View.OnClickListener {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    Button addMeeting;
    Button deleteMeeting;
    Button attendMeeting;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setToolbarTitle("우리 지금 만나");

        addMeeting = findViewById(R.id.meetingAdd);
        deleteMeeting = findViewById(R.id.meetingDelete);
        attendMeeting = findViewById(R.id.meetingAttend);

        addMeeting.setOnClickListener(this);
        deleteMeeting.setOnClickListener(this);
        attendMeeting.setOnClickListener(this);

        showMeetings();

    }

    private void showMeetings(){
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final GridView gridView = findViewById(R.id.gridView);
        final GridListAdapter adapter = new GridListAdapter();

        mAuth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("meetings")
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            //모든 document 출력 (dou id + data arr { : , ... ,  })
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if(document.getData().get("userID").toString().contains(user.getUid())){
                                    adapter.addItem(new GridItems(document.getData().get("name").toString(),document.getData().get("description").toString()));
                                    Log.d("Document Read", document.getId() + " => " + document.getData());

                                } else {
                                    Log.d("Document Snapshot", "No Document");
                                }
                            }
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
                revokeAccess();
                finish();
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
    //

    private void signOut(){
        FirebaseAuth.getInstance().signOut();
    }

    private void revokeAccess() {
        db = FirebaseFirestore.getInstance();
        mAuth.getCurrentUser().delete();

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
                finish();
                break;
            case(R.id.meetingDelete):
                myStartActivity(MeetingDeleteActivity.class);
                finish();
                break;
            case(R.id.meetingAttend):
                myStartActivity(MeetingAttendActivity.class);
                finish();
                break;
        }

    }

}
