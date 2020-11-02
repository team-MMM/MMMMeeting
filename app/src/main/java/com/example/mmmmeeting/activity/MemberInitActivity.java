package com.example.mmmmeeting.activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
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

import java.util.ArrayList;
import java.util.Objects;

public class MemberInitActivity extends BasicActivity implements View.OnClickListener, RatingBar.OnRatingBarChangeListener {

    Button checkButton,addressSearch;
    TextView addressTv, nameTv;
    RatingBar restaurant, cafe, shopping, subway;
    SharedPreferences sp;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_info);

        checkButton = findViewById(R.id.checkButton);
        addressSearch = findViewById(R.id.addressSearchBtn);
        addressTv = findViewById(R.id.addressText);
        nameTv = (EditText)findViewById(R.id.nameEditText);

        restaurant = findViewById(R.id.restaurantRate);
        cafe = findViewById(R.id.cafetRate);
        shopping = findViewById(R.id.shoppingRate);
        subway = findViewById(R.id.subwayRate);

        beforeInfo();
        
        checkButton.setOnClickListener(this);
        addressSearch.setOnClickListener(this);

        restaurant.setOnRatingBarChangeListener(this);
        cafe.setOnRatingBarChangeListener(this);
        shopping.setOnRatingBarChangeListener(this);
        subway.setOnRatingBarChangeListener(this);
    }

    private void beforeInfo() {
        // 이전 저장 값 보여주기 -> 창 띄울 때 자동으로 띄워져 있게
        sp = getSharedPreferences("sp", MODE_PRIVATE);
        String name = sp.getString("name", "");
        String address = sp.getString("address","");
        float restaurantBar = sp.getFloat("restaurant",0);
        float cafeBar = sp.getFloat("cafe",0);
        float shoppingBar = sp.getFloat("shopping",0);
        float subwayBar = sp.getFloat("subway",0);

        // 뷰에 반영
        nameTv.setText(name);
        addressTv.setText(address);
        restaurant.setRating(restaurantBar);
        cafe.setRating(cafeBar);
        shopping.setRating(shoppingBar);
        subway.setRating(subwayBar);
    }

    @Override protected void onStop() {
        super.onStop();
        // 액티비티 종료전 저장
        sp = getSharedPreferences("sp", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit(); // editor 사용해 저장

        // 사용자 입력 값 입력
        editor.putString("name", nameTv.getText().toString());
        editor.putString("address", addressTv.getText().toString());
        editor.putFloat("restaurant", restaurant.getRating());
        editor.putFloat("cafe", cafe.getRating());
        editor.putFloat("shopping", shopping.getRating());
        editor.putFloat("subway", subway.getRating());
        editor.commit(); // 저장 반영
    }

    @Override
    protected void onResume() {
        super.onResume();

        // 주소 검색 후 도로명 전달받아 텍스트뷰에 설정
        if(getIntent().getExtras()!=null) {
            Intent intent = getIntent();
            addressTv.setText(intent.getExtras().getString("road"));
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            // 저장 버튼 -> 유저 정보 db 저장
            case R.id.checkButton:
                if(((TextView)findViewById(R.id.addressText)).getText().toString().length()==0){
                    Toast.makeText(this, "주소를 입력하세요.", Toast.LENGTH_SHORT).show();
                    break;
                }
                else if(((EditText)findViewById(R.id.nameEditText)).getText().toString().length()==0){
                    Toast.makeText(this, "이름을 입력하세요.", Toast.LENGTH_SHORT).show();
                    break;
                } else {
                    profileUpdate();
                    break;
                }
            // 주소 찾기
            case R.id.addressSearchBtn:
                myStartActivity(SearchAddressActivity.class);
                finish();
                break;
        }
    }


    // 변경된 유저 정보 db에 저장
    private void profileUpdate(){
        String address = ((TextView)findViewById(R.id.addressText)).getText().toString();
        String name = ((EditText)findViewById(R.id.nameEditText)).getText().toString();

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // 멤버 정보 객체 생성 -> db저장
        MemberInfo memberInfo = new MemberInfo(name, address);
        memberInfo.setRating("restaurant", restaurant.getRating());
        memberInfo.setRating("cafe", cafe.getRating());
        memberInfo.setRating("subway", subway.getRating());
        memberInfo.setRating("shopping", shopping.getRating());

        Log.d("Rating Change", memberInfo.getRating().toString());

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

    private void startToast(String msg){
        Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
    }

    private void myStartActivity(Class c){
        Intent intent = new Intent(this,c);
        intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    @Override
    public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
        switch (ratingBar.getId()){
            case R.id.restaurantRate:
                Log.d("Rate Test", "restaurant change"+restaurant.getRating());
                break;
            case R.id.cafetRate:
                Log.d("Rate Test", "cafe change"+cafe.getRating());
                break;
            case R.id.shoppingRate:
                Log.d("Rate Test", "shopping change"+shopping.getRating());
                break;
            case R.id.subwayRate:
                Log.d("Rate Test", "subway change"+ subway.getRating());
                break;
        }
    }
}
