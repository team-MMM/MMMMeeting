package com.example.mmmmeeting.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.mmmmeeting.R;
import com.example.mmmmeeting.Info.GridItems;
import com.example.mmmmeeting.adapter.GridListAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class MeetingGridActivity extends AppCompatActivity implements View.OnClickListener {
    private FirebaseAuth mAuth ;
    Button addMeeting;
    Button deleteMeeting;
    Button attendMeeting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.my_meeting_grid_view);

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final GridView gridView = findViewById(R.id.gridView);
        final GridListAdapter adapter = new GridListAdapter();


        FirebaseFirestore db = FirebaseFirestore.getInstance();

        mAuth = FirebaseAuth.getInstance();
        addMeeting = (Button) findViewById(R.id.meetingAdd);
        deleteMeeting = (Button) findViewById(R.id.meetingDelete);
        attendMeeting = (Button) findViewById(R.id.meetingAttend);


        addMeeting.setOnClickListener(this);
        deleteMeeting.setOnClickListener(this);
        attendMeeting.setOnClickListener(this);



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


        /* 선택하면 모임 화면으로 전환되게 하려고 했는데 안 됨..ㅠㅠ
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Intent intent = new Intent(getApplicationContext(),EachMeetingActivity.class);
                GridItems gridItem = (GridItems) adapter.getItem(position);
                intent.putExtra("Name",gridItem.getName());
                Log.d("getName",gridItem.getName());
                intent.putExtra("Description",gridItem.getDescription());
                startActivity(intent);
            }
        });

         */

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case (R.id.meetingAdd):
                myStartActivity(MakeMeetingActivity.class);
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

    private void myStartActivity(Class c){
        Intent intent = new Intent(this,c);
        intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}