package com.example.mmmmeeting.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;

import com.example.mmmmeeting.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends BasicActivity implements View.OnClickListener {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    Button btnMyMeeting, btnMyMap;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setToolbarTitle("우리 지금 만나");

        mAuth = FirebaseAuth.getInstance();

        btnMyMeeting = (Button) findViewById(R.id.btn_checkMeeting);
        btnMyMap = (Button) findViewById(R.id.btn_myMap);

        btnMyMeeting.setOnClickListener(this);
        btnMyMap.setOnClickListener(this);
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

    private  void signOut(){
        FirebaseAuth.getInstance().signOut();
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()){

            case R.id.btn_checkMeeting:
                myStartActivity(MeetingGridActivity.class);
                break;

            // 지도 화면으로 이동
            case R.id.btn_myMap:
                myStartActivity(MapPlaceActivity.class);
                break;
        }
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
}
