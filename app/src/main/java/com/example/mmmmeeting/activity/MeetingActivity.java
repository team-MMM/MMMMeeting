package com.example.mmmmeeting.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.mmmmeeting.R;
import com.example.mmmmeeting.fragment.FragAccount;
import com.example.mmmmeeting.fragment.FragAlarm;
import com.example.mmmmeeting.fragment.FragBoard;
import com.example.mmmmeeting.fragment.FragCalendar;
import com.example.mmmmeeting.fragment.FragChat;
import com.example.mmmmeeting.fragment.FragHome;
import com.example.mmmmeeting.fragment.FragChat;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class MeetingActivity extends BasicActivity {

    private BottomNavigationView bottomNavigationView;
    private Bundle bundle = new Bundle();
    TextView name, description;
    Button invite;

    private FragmentManager fm;
    private FragmentTransaction ft;
    private FragCalendar fragCalendar;
    private FragChat fragChat;
    private FragAlarm fragAlarm;
    private FragAccount fragAccount;
    String getName;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.frag_default);
        getName = getIntent().getExtras().getString("Name");
        setToolbarTitle(getName);

        FragHome fragHome = new FragHome();
        bundle.putString("Name", getName);
        fragHome.setArguments(bundle);


        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main_frame,fragHome)
                .commit();

        bottomNavigationView = findViewById(R.id.bottomNavi);
        // 메뉴 바 아이콘을 눌렀을 때의 화면 동작
        // 각 화면 코드는 fragment 폴더에 있음
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch(item.getItemId()){
                    // 홈 화면(약속 목록)으로 이동
                    case R.id.menu_home:
                        FragHome fragHome = new FragHome();
                        bundle.putString("Name", getIntent().getExtras().getString("Name"));
                        fragHome.setArguments(bundle);

                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.main_frame,fragHome)
                                .commit();

                        return true;
                    case R.id.menu_chat:
                        FragChat fragChat = new FragChat();
                        bundle.putString("Name", getIntent().getExtras().getString("Name"));
                        fragChat.setArguments(bundle);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.main_frame, fragChat)
                                .commit();
                        return true;
                    case R.id.menu_board:
                        FragBoard fragBoard = new FragBoard();
                        bundle.putString("Name", getIntent().getExtras().getString("Name"));
                        fragBoard.setArguments(bundle);
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.main_frame, fragBoard)
                                .commit();
                        return true;
                    case R.id.menu_alarm:
                        FragAlarm fragAlarm = new FragAlarm();
                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.main_frame,fragAlarm)
                                .commit();
                        return true;
                    case R.id.menu_account:
                        FragAccount fragAccount = new FragAccount();
                        bundle.putString("Name", getIntent().getExtras().getString("Name"));
                        fragAccount.setArguments(bundle);

                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.main_frame,fragAccount)
                                .commit();
                        return true;
                }
                return false;
            }
        });


    }

    //메뉴바 코드
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.meetinginfo,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.meetingInfo:
                // 리더인지 확인
                checkReader(getName);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void checkReader(String getName) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth mAuth=FirebaseAuth.getInstance();
        db.collection("meetings").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            boolean check=false;
                            //모든 document 출력 (dou id + data arr { : , ... ,  })
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // 모임 이름이 같은 경우 해당 모임의 리더 확인
                                if (document.get("name").toString().equals(getName)&&document.get("reader").toString().equals(mAuth.getUid())) {
                                    check=true;
                                    Log.d("Document Read", document.getId() + " => " + document.getData());
                                    break;
                                } else {
                                    Log.d("Document Snapshot", "No Document");
                                }
                            }
                            if(check){
                                Intent intent = new Intent(MeetingActivity.this, MeetingInfoReaderActivity.class);
                                intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
                                intent.putExtra("Name",getIntent().getExtras().getString("Name"));
                                intent.putExtra("Description",getIntent().getExtras().getString("Description"));
                                startActivity(intent);
                            }
                            else{
                                Intent intent = new Intent(MeetingActivity.this, MeetingInfoActivity.class);
                                intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
                                intent.putExtra("Name",getIntent().getExtras().getString("Name"));
                                intent.putExtra("Description",getIntent().getExtras().getString("Description"));
                                startActivity(intent);
                            }
                        } else {
                            Log.d("Document Read", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

}
