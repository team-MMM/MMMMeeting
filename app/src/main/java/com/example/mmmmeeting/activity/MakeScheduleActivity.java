package com.example.mmmmeeting.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.mmmmeeting.Info.PostInfo;
import com.example.mmmmeeting.view.ContentsItemView;
import com.example.mmmmeeting.Info.ScheduleInfo;
import com.example.mmmmeeting.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
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
    private EditText selectedEditText;
    private EditText contentsEditText;
    private EditText titleEditText;
    private ScheduleInfo scheduleInfo;
    private int pathCount, successCount;
    private String meetingName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_make_schedule);

        parent = findViewById(R.id.contentsLayout);
        contentsEditText = findViewById(R.id.contentsEditText);
        titleEditText = findViewById(R.id.titleEditText);

        Bundle bundle = getIntent().getExtras();

        // 새로 작성한 경우 FragHome에서 bundle로 미팅 이름 받아옴
        if (bundle != null && meetingName != null) {
            meetingName = bundle.getString("Name");
            Log.d("update Test2", meetingName);

            // 수정하는 경우 ContentSceduleAct에서 수정할 Post의 미팅 이름 받아옴
        }else if (getIntent().getSerializableExtra("scheduleInfo")!=null) {
            scheduleInfo = (ScheduleInfo) getIntent().getSerializableExtra("scheduleInfo");
            if (scheduleInfo.getMeetingID() != null) {
                meetingName = scheduleInfo.getMeetingID();
            }
        }else{
            // 글 작성 후에는 외부에서 받아온 미팅 이름이 없어져서 MakeSchedule 자체에서 다시 미팅 이름 전달한거 받음
            // storeUpload->onSuccess
            meetingName = getIntent().getExtras().getString("Name");
        }

        findViewById(R.id.check).setOnClickListener(onClickListener);

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
                /*
            case 1:
                if (resultCode == RESULT_OK) {
                    String path = data.getStringExtra(INTENT_PATH);
                    pathList.set(parent.indexOfChild((View) selectedImageVIew.getParent()) - 1, path);
                    Glide.with(this).load(path).override(1000).into(selectedImageVIew);
                }
                break;

                 */
        }
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.check:
                    // 확인 버튼 누르면 Firestore에 업로드
                    storageUpload();
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
    private void storageUpload() {
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
                        }
                    } else if (!isStorageUrl(pathList.get(pathCount))) {
                        String path = pathList.get(pathCount);
                        successCount++;
                        contentsList.add(path);
                        String[] pathArray = path.split("\\.");
                        // schedule 테이블의 문서 ID 받아서 해당 문서에 정보 업로드
                        final StorageReference mountainImagesRef = storageRef.child("schedule/" + documentReference.getId() + "/" + pathCount + "." + pathArray[pathArray.length - 1]);
                        try {
                            InputStream stream = new FileInputStream(new File(pathList.get(pathCount)));
                            StorageMetadata metadata = new StorageMetadata.Builder().setCustomMetadata("index", "" + (contentsList.size() - 1)).build();
                            UploadTask uploadTask = mountainImagesRef.putStream(stream, metadata);
                            uploadTask.addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                }
                            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    final int index = Integer.parseInt(taskSnapshot.getMetadata().getCustomMetadata("index"));
                                    mountainImagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {
                                            successCount--;
                                            contentsList.set(index, uri.toString());
                                            if (successCount == 0) {
                                                ScheduleInfo scheduleInfo = new ScheduleInfo(title, meetingName, contentsList, date, user.getUid());
                                                storeUpload(documentReference, scheduleInfo);
                                            }
                                        }
                                    });
                                }
                            });
                        } catch (FileNotFoundException e) {
                            Log.e("로그", "에러: " + e.toString());
                        }
                        pathCount++;
                    }
                }
            }
            if (successCount == 0) {
                storeUpload(documentReference, new ScheduleInfo(title, meetingName, contentsList, date, user.getUid()));
            }
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
                        resultIntent.putExtra("Name",meetingName);
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
                if (isStorageUrl(contents)) {
                    pathList.add(contents);
                    ContentsItemView contentsItemView = new ContentsItemView(this);
                    parent.addView(contentsItemView);

                    contentsItemView.setImage(contents);
                    contentsItemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                        }
                    });

                    contentsItemView.setOnFocusChangeListener(onFocusChangeListener);
                    if (i < contentsList.size() - 1) {
                        String nextContents = contentsList.get(i + 1);
                        if (!isStorageUrl(nextContents)) {
                            contentsItemView.setText(nextContents);
                        }
                    }
                } else if (i == 0) {
                    contentsEditText.setText(contents);
                }
            }
        }
    }

    private void myStartActivity(Class c, int media, int requestCode) {
        Intent intent = new Intent(this, c);
        intent.putExtra(INTENT_MEDIA, media);
        startActivityForResult(intent, requestCode);
    }

}