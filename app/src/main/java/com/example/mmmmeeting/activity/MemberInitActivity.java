package com.example.mmmmeeting.activity;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.example.mmmmeeting.Info.MemberInfo;
import com.example.mmmmeeting.Info.ScheduleInfo;
import com.example.mmmmeeting.R;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserInfo;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import static com.example.mmmmeeting.Util.INTENT_MEDIA;
import static com.example.mmmmeeting.Util.INTENT_PATH;
import static com.example.mmmmeeting.Util.GALLERY_IMAGE;
import static com.example.mmmmeeting.Util.isProfileUrl;
import static com.example.mmmmeeting.Util.isStorageUrl;
import static com.example.mmmmeeting.Util.showToast;
import static com.example.mmmmeeting.Util.storageUrlToName;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.lang.reflect.Member;
import java.util.ArrayList;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class MemberInitActivity extends BasicActivity implements View.OnClickListener, RatingBar.OnRatingBarChangeListener {

    private Button checkButton,addressSearch;
    private TextView addressTv, nameTv;
    private RatingBar restaurant, cafe, shopping, park, act;
    private SharedPreferences sp;
    private ImageView profileImageVIew;
    private String profilePath;
    private RelativeLayout buttonBackgroundLayout;
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();

    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_info);

        checkButton = findViewById(R.id.checkButton);
        addressSearch = findViewById(R.id.addressSearchBtn);
        addressTv = findViewById(R.id.addressText);
        nameTv = (EditText)findViewById(R.id.nameEditText);

        profileImageVIew = findViewById(R.id.profileImageView);
        buttonBackgroundLayout = findViewById(R.id.buttonsBackgroundLayout);

        restaurant = findViewById(R.id.restaurantRate);
        cafe = findViewById(R.id.cafetRate);
        shopping = findViewById(R.id.shoppingRate);
        park = findViewById(R.id.parkRate);
        act = findViewById(R.id.actRate);

        sp = getSharedPreferences("sp", MODE_PRIVATE);


        beforeInfo();

        System.out.println("main:"+profilePath);

        checkButton.setOnClickListener(this);
        addressSearch.setOnClickListener(this);
        profileImageVIew.setOnClickListener(this);

        restaurant.setOnRatingBarChangeListener(this);
        cafe.setOnRatingBarChangeListener(this);
        shopping.setOnRatingBarChangeListener(this);
        park.setOnRatingBarChangeListener(this);
        act.setOnRatingBarChangeListener(this);

        buttonBackgroundLayout.setOnClickListener(this);
        findViewById(R.id.delete).setOnClickListener(this);
        findViewById(R.id.gallery).setOnClickListener(this);


    }

    private void beforeInfo() {
        // 이전 저장 값 보여주기 -> 창 띄울 때 자동으로 띄워져 있게

        String name = sp.getString("name", "");
        String address = sp.getString("address","");
        float restaurantBar = sp.getFloat("restaurant",0);
        float cafeBar = sp.getFloat("cafe",0);
        float shoppingBar = sp.getFloat("shopping",0);
        float parkBar = sp.getFloat("park",0);
        float actBar = sp.getFloat("act",0);
        profilePath = sp.getString("profilePath","");


        // 뷰에 반영
        nameTv.setText(name);
        addressTv.setText(address);
        restaurant.setRating(restaurantBar);
        cafe.setRating(cafeBar);
        shopping.setRating(shoppingBar);
        park.setRating(parkBar);
        act.setRating(actBar);

        if(profilePath.equals("")){
            profileImageVIew.setImageResource(R.drawable.profile);

        }else if(profilePath!=null) {
            Glide.with(this).load(profilePath).centerCrop().override(500).into(profileImageVIew);
        }
    }



    @Override
    protected void onResume() {
        super.onResume();

        // 주소 검색 후 도로명 전달받아 텍스트뷰에 설정
        if(getIntent().getExtras()!=null) {
            Intent intent = getIntent();
            addressTv.setText(intent.getExtras().getString("road"));

            String name = sp.getString("name", "");
            float restaurantBar = sp.getFloat("restaurant",0);
            float cafeBar = sp.getFloat("cafe",0);
            float shoppingBar = sp.getFloat("shopping",0);
            float parkBar = sp.getFloat("park",0);
            float actBar = sp.getFloat("act",0);
            profilePath = sp.getString("profilePath","");

            nameTv.setText(name);
            restaurant.setRating(restaurantBar);
            cafe.setRating(cafeBar);
            shopping.setRating(shoppingBar);
            park.setRating(parkBar);
            act.setRating(actBar);

            if(profilePath.equals("")){
                profileImageVIew.setImageResource(R.drawable.profile);

            }else if(profilePath!=null) {
                Glide.with(this).load(profilePath).centerCrop().override(500).into(profileImageVIew);
            }
        }

    }

    private void save(){
        SharedPreferences.Editor editor = sp.edit(); // editor 사용해 저장
        // 사용자 입력 값 입력
        editor.putString("name", nameTv.getText().toString());
        editor.putString("address", addressTv.getText().toString());
        editor.putFloat("restaurant", restaurant.getRating());
        editor.putFloat("cafe", cafe.getRating());
        editor.putFloat("shopping", shopping.getRating());
        editor.putFloat("park", park.getRating());
        editor.putFloat("act", act.getRating());
        editor.putString("profilePath",profilePath);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            // 저장 버튼 -> 유저 정보 db 저장
            case R.id.checkButton:
                // 주소, 이름 필수 입력
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
                save();
                myStartActivity(SearchAddressActivity.class);
                break;

            case R.id.profileImageView:
                save();
                buttonBackgroundLayout.setVisibility(View.VISIBLE);
                break;

            case R.id.buttonsBackgroundLayout:
                buttonBackgroundLayout.setVisibility(View.GONE);
                break;

            case R.id.gallery:
                myStartActivity(GalleryActivity.class,GALLERY_IMAGE,0);
                break;

            case R.id.delete:
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                System.out.println("delpath:"+profilePath);
                if(isProfileUrl(profilePath)){
                    final StorageReference mountainImagesRef = storageRef.child("users/" + user.getUid() + "/profileImage.jpg");
                    mountainImagesRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            showToast(MemberInitActivity.this,"프로필 사진을 삭제하였습니다.");
                            db.collection("users").document(user.getUid())
                                    .update("profilePath","");
                            profilePath = "";
                            System.out.println("db:"+profilePath);
                            profileImageVIew.setImageResource(R.drawable.profile);
                            buttonBackgroundLayout.setVisibility(View.GONE);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            showToast(MemberInitActivity.this,"프로필 사진 삭제에 실패하였습니다.");
                        }
                    });
                }else{
                    // 방금 올린 사진일 때
                    profilePath = "";
                    System.out.println("del:"+profilePath);
                    profileImageVIew.setImageResource(R.drawable.profile);
                    buttonBackgroundLayout.setVisibility(View.GONE);
                }
                break;
        }
    }


    // 변경된 유저 정보 db에 저장
    private void profileUpdate(){
        String address = ((TextView)findViewById(R.id.addressText)).getText().toString();
        String name = ((EditText)findViewById(R.id.nameEditText)).getText().toString();


        // 멤버 정보 객체 생성 -> db저장
        MemberInfo memberInfo = new MemberInfo(name, address);
        memberInfo.setRating("restaurant", restaurant.getRating());
        memberInfo.setRating("cafe", cafe.getRating());
        memberInfo.setRating("park", park.getRating());
        memberInfo.setRating("shopping", shopping.getRating());
        memberInfo.setRating("act", act.getRating());

        Log.d("Rating Change", memberInfo.getRating().toString());

        if (user != null) {
            if (profilePath != null) {
                if(profilePath.equals("") || isProfileUrl(profilePath)){
                    memberInfo.setProfilePath(profilePath);
                    storeUploader(memberInfo);
                }else{
                    System.out.println(profilePath);
                    System.out.println("profile");
                    final StorageReference mountainImagesRef = storageRef.child("users/" + user.getUid() + "/profileImage.jpg");
                    try {
                        InputStream stream = new FileInputStream(new File(profilePath));
                        UploadTask uploadTask = mountainImagesRef.putStream(stream);
                        uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                            @Override
                            public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                                if (!task.isSuccessful()) {
                                    throw task.getException();
                                }
                                return mountainImagesRef.getDownloadUrl();
                            }
                        }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                            @Override
                            public void onComplete(@NonNull Task<Uri> task) {
                                if (task.isSuccessful()) {
                                    Uri downloadUri = task.getResult();
                                    memberInfo.setProfilePath(downloadUri.toString());
                                    System.out.println("profilepath"+downloadUri.toString());
                                    storeUploader(memberInfo);
                                } else {
                                    startToast("이미지 업로드가 실패하였습니다.");
                                }
                            }
                        });
                    } catch (FileNotFoundException e) {
                        Log.e("로그", "에러: " + e.toString());
                    }
                }
            } else {
                storeUploader(memberInfo);
            }
        }else {
            startToast("회원정보를 입력해주세요.");
        }

    }

    private void storeUploader(MemberInfo memberInfo){
        db.collection("users").document(user.getUid()).set(memberInfo)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        SharedPreferences.Editor editor = sp.edit(); // editor 사용해 저장

                        // 사용자 입력 값 입력
                        editor.putString("name", nameTv.getText().toString());
                        editor.putString("address", addressTv.getText().toString());
                        editor.putFloat("restaurant", restaurant.getRating());
                        editor.putFloat("cafe", cafe.getRating());
                        editor.putFloat("shopping", shopping.getRating());
                        editor.putFloat("park", park.getRating());
                        editor.putFloat("act", act.getRating());
                        if(memberInfo.getProfilePath()!=null) {
                            System.out.println(memberInfo.getProfilePath());
                            editor.putString("profilePath", memberInfo.getProfilePath());
                        }else{
                            editor.remove("profilePath");
                            System.out.println("remove");
                        }

                        editor.commit(); // 저장 반영
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

    }

    private void startToast(String msg){
        Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
    }



    private void myStartActivity(Class c){
        Intent intent = new Intent(this,c);
        intent.addFlags(intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }


    private void myStartActivity(Class c, int media, int requestCode) {
        Intent intent = new Intent(this, c);
        intent.putExtra(INTENT_MEDIA, media);
        startActivityForResult(intent, requestCode);
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
            case R.id.parkRate:
                Log.d("Rate Test", "subway change"+ park.getRating());
                break;
            case R.id.actRate:
                Log.d("Rate Test", "subway change"+ act.getRating());
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            profilePath = data.getStringExtra(INTENT_PATH);
            System.out.println(profilePath);
            Glide.with(this).load(profilePath).centerCrop().override(500).into(profileImageVIew);
            buttonBackgroundLayout.setVisibility(View.GONE);
        }


    }

}
