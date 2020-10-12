package com.example.mmmmeeting.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mmmmeeting.R;

public class EachMeetingActivity extends BasicActivity implements View.OnClickListener {

    ImageView myMeeting;
    TextView roomName,roomDescription;
    Button btnPost;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_meeting);


        roomName = (TextView) findViewById(R.id.roomName);
        roomDescription = (TextView) findViewById(R.id.roomDescription);
        myMeeting = (ImageView) findViewById(R.id.meetingImg);
        btnPost = (Button) findViewById(R.id.btn_post);


        Intent intent = getIntent();
        String name = intent.getExtras().getString("Name");
        String description = intent.getExtras().getString("Description");

        roomName.setText(name);
        roomDescription.setText(description);



        btnPost.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btn_post:
                write_post();
                break;

        }

    }

    private void write_post(){
        myStartActivity(WritePostActivity.class);
        finish();
    }


    private void startToast(String msg){
        Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
    }

    private void myStartActivity(Class c){
        Intent intent = new Intent(this,c);
        intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }
}
