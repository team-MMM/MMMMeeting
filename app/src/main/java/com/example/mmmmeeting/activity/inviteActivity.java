package com.example.mmmmeeting.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.mmmmeeting.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class inviteActivity extends AppCompatActivity {

    EditText phone;
    Button send;
    private final int MY_PERMISSION_REQUEST_SMS = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS)!= PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.SEND_SMS)){
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("info");
                builder.setMessage("SMS 허가가 없으면 앱이 정상 작동하지 않을 수 있습니다.");

                builder.setNeutralButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions(inviteActivity.this,new String[] {Manifest.permission.SEND_SMS},MY_PERMISSION_REQUEST_SMS);
                    }
                });

                AlertDialog dialog = builder.create();
                dialog.show();
            }else{
                ActivityCompat.requestPermissions(this,new String[] {Manifest.permission.SEND_SMS}, MY_PERMISSION_REQUEST_SMS);
            }
        }

        send = findViewById(R.id.sendBtn);
        phone = findViewById(R.id.phone_number);

        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FirebaseFirestore db = FirebaseFirestore.getInstance();
                Intent intent = getIntent();
                String meetingname = intent.getExtras().getString("Name");
                Log.d("SendSMS",meetingname);

                db.collection("meetings").get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    //모든 document 확인 (dou id + data arr { : , ... ,  })
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        if(document.get("name").toString().equals(meetingname)){
                                            Log.d("SendSMS",meetingname+"//"+document.get("name"));
                                            sendSMS(document.getId());
                                            break;
                                        }
                                    }
                                } else {
                                    Log.d("SendSMS", "Error getting documents: ", task.getException());
                                }
                            }
                        });

            }
        });
    }

    private void sendSMS(String code){
        Intent intent = getIntent();
        String meetingname = intent.getExtras().getString("Name");

        String phoneNo = phone.getText().toString();
        String sms = meetingname+"에서의 초대 코드 : " + code;

        try {
            //전송
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneNo, null, sms, null, null);
            Log.d("SendSMS", phoneNo+"//"+sms);
            Toast.makeText(getApplicationContext(), "전송 완료!", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), "SMS faild, please try again later!", Toast.LENGTH_LONG).show();
            e.printStackTrace();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case MY_PERMISSION_REQUEST_SMS:{
                if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}