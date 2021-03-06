package com.example.mmmmeeting.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.mmmmeeting.Info.PostInfo;
import com.example.mmmmeeting.fragment.FragHome;
import com.example.mmmmeeting.view.ContentsItemView;
import com.example.mmmmeeting.Info.ScheduleInfo;
import com.example.mmmmeeting.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;

import static com.example.mmmmeeting.Util.INTENT_MEDIA;
import static com.example.mmmmeeting.Util.INTENT_PATH;
import static com.example.mmmmeeting.Util.isStorageUrl;
import static com.example.mmmmeeting.Util.showToast;

public class MakeScheduleActivity extends AppCompatActivity {
    private static final String TAG = "MakeScheduleActivity";
    private FirebaseUser user;
    private StorageReference storageRef;
    private ArrayList<String> pathList = new ArrayList<>();
    private LinearLayout parent;
    private EditText selectedEditText, contentsEditText, titleEditText;
    private TextView showInfo;
    private ScheduleInfo scheduleInfo;
    private int pathCount, successCount;
    private String meetingCode;
    private Button info;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_schedule);

        parent = findViewById(R.id.contentsLayout);
        contentsEditText = findViewById(R.id.contentsEditText);
        titleEditText = findViewById(R.id.titleEditText);
        info = findViewById(R.id.infoBtn);

        Bundle bundle = getIntent().getExtras();

        // 새로 작성한 경우 FragHome에서 bundle로 미팅 이름 받아옴
        if (bundle != null && meetingCode != null) {
            meetingCode = bundle.getString("Code");
            Log.d("update Test2", meetingCode);

            // 수정하는 경우 ContentSceduleAct에서 수정할 Post의 미팅 이름 받아옴
        }else if (getIntent().getSerializableExtra("scheduleInfo")!=null) {
            scheduleInfo = (ScheduleInfo) getIntent().getSerializableExtra("scheduleInfo");
            if (scheduleInfo.getMeetingID() != null) {
                meetingCode = scheduleInfo.getMeetingID();
            }
        }else{
            // 글 작성 후에는 외부에서 받아온 미팅 이름이 없어져서 MakeSchedule 자체에서 다시 미팅 이름 전달한거 받음
            // storeUpload->onSuccess
            meetingCode = getIntent().getExtras().getString("Code");
        }

        findViewById(R.id.onlineBtn).setOnClickListener(onClickListener);
        findViewById(R.id.offlineBtn).setOnClickListener(onClickListener);
        info.setOnClickListener(onClickListener);


        contentsEditText.setOnFocusChangeListener(onFocusChangeListener);
        titleEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    selectedEditText = null;
                }
            }
        });

        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();

        // 스케쥴 DB 정보 담은 코드
        scheduleInfo = (ScheduleInfo) getIntent().getSerializableExtra("scheduleInfo");


        postInit();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 0:
                if (resultCode == RESULT_OK) {
                    String path = data.getStringExtra(INTENT_PATH);
                    pathList.add(path);

                    ContentsItemView contentsItemView = new ContentsItemView(this);

                    if (selectedEditText == null) {
                        parent.addView(contentsItemView);
                    } else {
                        for (int i = 0; i < parent.getChildCount(); i++) {
                            if (parent.getChildAt(i) == selectedEditText.getParent()) {
                                parent.addView(contentsItemView, i + 1);
                                break;
                            }
                        }
                    }

                    contentsItemView.setOnFocusChangeListener(onFocusChangeListener);
                }
                break;
        }
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.infoBtn:
                    final Snackbar snackbar = Snackbar.make(v, "비대면 약속은 캘린더, 알림 기능만을 제공합니다!", Snackbar.LENGTH_INDEFINITE);
                    snackbar.setAction("확인", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            snackbar.dismiss();
                        }
                    });
                    snackbar.show();
                    break;

                case R.id.offlineBtn:
                    // 확인 버튼 누르면 Firestore에 업로드
                    storageUpload("offline");
                    break;

                case R.id.onlineBtn:
                    // 확인 버튼 누르면 Firestore에 업로드
                    storageUpload("online");
                    break;
            }
        }
    };

    View.OnFocusChangeListener onFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View v, boolean hasFocus) {
            if (hasFocus) {
                selectedEditText = (EditText) v;
            }
        }
    };


    // 글 ID 찾기, ScheduleInfo 정보 생성, 글 업로드
    private void storageUpload(String type) {
        final String title = ((EditText) findViewById(R.id.titleEditText)).getText().toString();

        if (title.length() > 0) {
            final ArrayList<String> contentsList = new ArrayList<>();
            final ArrayList<String> lateComerList = new ArrayList<>();
            user = FirebaseAuth.getInstance().getCurrentUser();

            // Firestore와 연동
            FirebaseStorage storage = FirebaseStorage.getInstance();
            StorageReference storageRef = storage.getReference();
            FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();

            // 만약 새로 작성한 글이면 scInfo == null -> schedule 밑에 새 ID로 문서 생성
            // 기존 글을 수정하는거면 scInfo != null -> scInfo ID 찾아서 해당 글 문서에 다시 업로드
            final DocumentReference documentReference = scheduleInfo == null ? firebaseFirestore.collection("schedule").document() : firebaseFirestore.collection("schedule").document(scheduleInfo.getId());

            final Date date = scheduleInfo == null ? new Date() : scheduleInfo.getCreatedAt();
            for (int i = 0; i < parent.getChildCount(); i++) {
                LinearLayout linearLayout = (LinearLayout) parent.getChildAt(i);
                for (int ii = 0; ii < linearLayout.getChildCount(); ii++) {
                    View view = linearLayout.getChildAt(ii);
                    if (view instanceof EditText) {
                        String text = ((EditText) view).getText().toString();
                        if (text.length() > 0) {
                            // 내용 리스트에 약속 설명 추가
                            contentsList.add(text);
                            if(scheduleInfo!=null){
                                finish();
                            }
                        }

                    }
                }
            }
            storeUpload(documentReference, new ScheduleInfo(title, meetingCode, contentsList, date, user.getUid(), type));

        } else {
            showToast(MakeScheduleActivity.this, "제목을 입력해주세요.");
        }
    }

    // db에 등록 성공했는지 검사, 다시 FragHome으로 돌아옴
    private void storeUpload(DocumentReference documentReference, final ScheduleInfo scheduleInfo) {
        documentReference.set(scheduleInfo.getScheduleInfo())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("scheduleInfo", scheduleInfo);
                        // 글 작성 후 다시 MakeSc로 돌아오면 meetingName이 사라져서 다시 전달함
                        resultIntent.putExtra("Code",meetingCode);
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                    }
                });
    }

    private void postInit() {
        if (scheduleInfo != null) {
            titleEditText.setText(scheduleInfo.getTitle());
            ArrayList<String> contentsList = scheduleInfo.getContents();
            for (int i = 0; i < contentsList.size(); i++) {
                String contents = contentsList.get(i);
                contentsEditText.setText(contents);
            }
        }
    }

    private void myStartActivity(Class c, int media, int requestCode) {
        Intent intent = new Intent(this, c);
        intent.putExtra(INTENT_MEDIA, media);
        startActivityForResult(intent, requestCode);
    }

}