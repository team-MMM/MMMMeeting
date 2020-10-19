package com.example.mmmmeeting.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.example.mmmmeeting.Info.MemberInfo;
import com.example.mmmmeeting.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Objects;

public class MemberInitActivity extends BasicActivity implements View.OnClickListener {

    Button checkButton;
    Button addressSearch;
    TextView addressTv;
    TextView nameTv;
    SharedPreferences sp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_info);

        checkButton = findViewById(R.id.checkButton);
        addressSearch = findViewById(R.id.addressSearchBtn);
        addressTv = findViewById(R.id.addressText);
        nameTv = (EditText)findViewById(R.id.nameEditText);

        // 이전 저장 값 보여주기
        sp = getSharedPreferences("sp", MODE_PRIVATE);
        String save = sp.getString("name", "");
        nameTv.setText(save); // 뷰에 반영

        checkButton.setOnClickListener(this);
        addressSearch.setOnClickListener(this);
    }

    @Override protected void onStop() {
        super.onStop();
        // 액티비티 종료전 저장
        sp = getSharedPreferences("sp", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit(); // editor 사용해 저장
        editor.putString("name", nameTv.getText().toString()); // 사용자 입력 값 입력
        editor.commit(); // 저장 반영
    }

    @Override
    protected void onResume() {
        super.onResume();

        // 주소 검색 후 도로명 전달받아 텍스트뷰에 설정
        if(getIntent().getExtras()!=null) {
            Intent intent = getIntent();
            addressTv.setText(intent.getExtras().getString("road"));
            Log.d("Setting",intent.getExtras().getString("road"));
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.checkButton:
                profileUpdate();
                break;

            case R.id.addressSearchBtn:
                myStartActivity(SearchAddressActivity.class);
                finish();
                break;
        }
    }


    private void profileUpdate(){
        String address = ((TextView)findViewById(R.id.addressText)).getText().toString();
        String name = ((EditText)findViewById(R.id.nameEditText)).getText().toString();


        if(name.length()>0) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            MemberInfo memberInfo = new MemberInfo(name, address);

            if (user != null) {
                db.collection("users").document(user.getUid()).set(memberInfo)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                startToast("회원정보 등록에 성공하였습니다.");
                                myStartActivity(MainActivity.class);
                                finish();
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                startToast("회원정보 등록에 실패하였습니다.");
                            }
                        });
            } else {
                startToast("회원정보를 입력해주세요.");
            }
        }
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
