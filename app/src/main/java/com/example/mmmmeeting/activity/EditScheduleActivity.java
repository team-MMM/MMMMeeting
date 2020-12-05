package com.example.mmmmeeting.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;

import androidx.appcompat.app.AppCompatActivity;

import com.example.mmmmeeting.Info.ScheduleInfo;
import com.example.mmmmeeting.R;
import com.example.mmmmeeting.view.ContentsItemView;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import static com.example.mmmmeeting.Util.INTENT_PATH;
import static com.example.mmmmeeting.Util.showToast;

public class EditScheduleActivity extends AppCompatActivity {
    private FirebaseUser user;
    private StorageReference storageRef;
    private ArrayList<String> pathList = new ArrayList<>();
    private LinearLayout parent;
    private EditText selectedEditText, contentsEditText, titleEditText;
    private ScheduleInfo scheduleInfo;
    private int pathCount, successCount;
    private String meetingCode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_schedule);

        parent = findViewById(R.id.contentsLayout);
        contentsEditText = findViewById(R.id.contentsEditText);
        titleEditText = findViewById(R.id.titleEditText);

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
        }
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {

                case R.id.check:
                    // 확인 버튼 누르면 Firestore에 업로드
                    if(scheduleInfo!=null){
                        edit();
                    }
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

    private void edit(){

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        final DocumentReference documentReference = db.collection("schedule").document(scheduleInfo.getId());

        final String title = ((EditText) findViewById(R.id.titleEditText)).getText().toString();
        System.out.println("I'm edit");
        if (title.length() > 0) {
            final ArrayList<String> contentsList = new ArrayList<>();

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
                    }
                }
            }

            if (successCount == 0) {
                documentReference.update("title",title);
                documentReference.update("contents",contentsList);
                scheduleInfo.setTitle(title);
                scheduleInfo.setContents(contentsList);
                Intent resultIntent = new Intent();
                resultIntent.putExtra("scheduleInfo", scheduleInfo);
                resultIntent.putExtra("Code",meetingCode);
                setResult(RESULT_OK, resultIntent);
                finish();

            }

        } else {
            showToast(EditScheduleActivity.this, "제목을 입력해주세요.");
        }
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

}