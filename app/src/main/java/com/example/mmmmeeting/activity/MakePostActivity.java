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

import com.bumptech.glide.Glide;
import com.example.mmmmeeting.Info.PostInfo;
import com.example.mmmmeeting.R;
import com.example.mmmmeeting.view.ContentsItemView;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
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

import static com.example.mmmmeeting.Util.GALLERY_IMAGE;
import static com.example.mmmmeeting.Util.INTENT_MEDIA;
import static com.example.mmmmeeting.Util.INTENT_PATH;
import static com.example.mmmmeeting.Util.isStorageUrl;
import static com.example.mmmmeeting.Util.showToast;
import static com.example.mmmmeeting.Util.storageUrlToName;

public class MakePostActivity extends BasicActivity {
    private static final String TAG = "WritePostActivity";
    private FirebaseUser user;
    private FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
    private StorageReference storageRef;
    private ArrayList<String> pathList = new ArrayList<>();
    private ArrayList<String> showList = new ArrayList<>();
    private LinearLayout parent;
    private RelativeLayout buttonsBackgroundLayout;
    private RelativeLayout loaderLayout;
    private ImageView selectedImageVIew;
    private EditText selectedEditText;
    private EditText descriptionText;
    private EditText titleEditText;
    private PostInfo postInfo;
    private int pathCount, successCount;
    private String meetingcode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_post);
        setToolbarTitle("게시글 작성");

        parent = findViewById(R.id.contentsLayout);
        buttonsBackgroundLayout = findViewById(R.id.buttonsBackgroundLayout);
        loaderLayout = findViewById(R.id.loaderLyaout);
        descriptionText = findViewById(R.id.contentsEditText);
        titleEditText = findViewById(R.id.titleEditText);

        findViewById(R.id.check).setOnClickListener(onClickListener);
        findViewById(R.id.image).setOnClickListener(onClickListener);
        findViewById(R.id.delete).setOnClickListener(onClickListener);

        buttonsBackgroundLayout.setOnClickListener(onClickListener);
        titleEditText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    selectedEditText = null;
                }
            }
        });

        // 저장소
        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageRef = storage.getReference();


        Bundle bundle = getIntent().getExtras();

        // 새로 작성한 경우 FragBoard에서 bundle로 미팅 이름 받아옴
        if (bundle != null && meetingcode != null) {
            meetingcode = bundle.getString("Code");
            Log.d("update Test2", meetingcode);

            // 수정하는 경우 ContentBoardAct에서 수정할 Post의 미팅 이름 받아옴
        }else if (getIntent().getSerializableExtra("postInfo")!=null) {
            postInfo = (PostInfo) getIntent().getSerializableExtra("postInfo");
            if (postInfo.getMeetingID() != null) {
                meetingcode = postInfo.getMeetingID();
            }
        }else{
            // 글 작성 후에는 외부에서 받아온 미팅 이름이 없어져서 MakePost 자체에서 다시 미팅 이름 전달한거 받음
            // storeUpload->onSuccess
            meetingcode = getIntent().getExtras().getString("Code");
        }
        postInit();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
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

            contentsItemView.setImage(path);
            contentsItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    buttonsBackgroundLayout.setVisibility(View.VISIBLE);
                    selectedImageVIew = (ImageView) v;
                }
            });
            contentsItemView.setOnFocusChangeListener(onFocusChangeListener);
        }
    }

    View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.check:
                    if(postInfo!=null){
                        edit();
                    }else {
                        storageUpload();
                    }
                    break;
                case R.id.image:
                    myStartActivity(GalleryActivity.class, GALLERY_IMAGE, 0);
                    break;

                case R.id.buttonsBackgroundLayout:
                    if (buttonsBackgroundLayout.getVisibility() == View.VISIBLE) {
                        buttonsBackgroundLayout.setVisibility(View.GONE);
                    }
                    break;
                // 개별 사진 삭제
                case R.id.delete:
                    final View selectedView = (View) selectedImageVIew.getParent();
                    String path;
                    int now = parent.indexOfChild(selectedView) - 1;
                    int contSize = postInfo.getContents().size();
                    // 새로 추가한 사진일 때
                    if(now >= contSize){
                        path = pathList.get(now-contSize);
                    }else {
                    // db에 올라가 있는 사진일 때
                        path = postInfo.getContents().get(now);
                    }
                    // 혹시 모르니까 db에 올라간 형식인지 다시 검사..
                    if(isStorageUrl(path)){
                        StorageReference desertRef = storageRef.child("posts/" + postInfo.getId() + "/" + storageUrlToName(path));
                        desertRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                showToast(MakePostActivity.this, "파일을 삭제하였습니다.");
                                ArrayList<String> temp = postInfo.getContents();
                                temp.remove(parent.indexOfChild(selectedView) - 1);
                                firebaseFirestore.collection("posts").document(postInfo.getId())
                                        .update("contents",temp);
                                parent.removeView(selectedView);
                                buttonsBackgroundLayout.setVisibility(View.GONE);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                showToast(MakePostActivity.this, "파일을 삭제하는데 실패하였습니다.");
                            }
                        });
                    }else{
                        // 방금 올린 사진일 때
                        pathList.remove(parent.indexOfChild(selectedView) - 1-postInfo.getContents().size());
                        System.out.println(parent.indexOfChild(selectedView) - 1);
                        parent.removeView(selectedView);
                        buttonsBackgroundLayout.setVisibility(View.GONE);
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

    // 수정하는 경우
    private void edit() {
        String title = ((EditText) findViewById(R.id.titleEditText)).getText().toString();
        String description = ((EditText) findViewById(R.id.contentsEditText)).getText().toString();

        if (title.length() > 0) {
            loaderLayout.setVisibility(View.VISIBLE);
            final ArrayList<String> contentsList = postInfo.getContents();

            // 만약 새로 작성한 글이면 postInfo == null -> posts 밑에 새 ID로 문서 생성
            // 기존 글을 수정하는거면 postInfo != null -> postInfo ID 찾아서 해당 글 문서에 다시 업로드
            final DocumentReference documentReference = firebaseFirestore.collection("posts").document(postInfo.getId());
            final Date date = postInfo.getCreatedAt();

            postInfo.setTitle(title);
            postInfo.setDescription(description);

            // 사진을 바꾸지 않았을 때 타이틀, 설명만 수정
            if(pathList.size() == 0){
                documentReference.update("title",title);
                documentReference.update("description",description);

                Intent resultIntent = new Intent();
                resultIntent.putExtra("postinfo", postInfo);
                // 글 작성 후 다시 MakePost로 돌아오면 meetingName이 사라져서 다시 전달함
                resultIntent.putExtra("Code",meetingcode);
                setResult(RESULT_OK, resultIntent);
                finish();
            }

            // 새로 올린 사진은 저장소에 올리고 contents 리스트 db에도 반영
            for (int i = 0; i < pathList.size(); i++) {

                pathCount = i;
                successCount++;
                String path = pathList.get(pathCount);
                contentsList.add(path);
                System.out.println("cont1: "+contentsList);
                String[] pathArray = path.split("\\.");
                // posts 테이블의 문서 ID 받아서 해당 문서에 정보 업로드
                final StorageReference mountainImagesRef = storageRef.child("posts/" + postInfo.getId() + "/" + (contentsList.size()-1) + "." + pathArray[pathArray.length - 1]);
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
                            System.out.println("index: "+index);
                            mountainImagesRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    contentsList.set(index, uri.toString());
                                    postInfo.setContents(contentsList);
                                    // 새로운 파일들 저장소에 다 올렸을 때 db에 contentlist 저장
                                    successCount--;
                                    updateDB(postInfo);
                                    System.out.println("cont2: "+contentsList);

                                }
                            });
                        }
                    });
                } catch (FileNotFoundException e) {
                    Log.e("로그", "에러: " + e.toString());
                }

            }

        } else {
            showToast(MakePostActivity.this, "제목을 입력해주세요.");
        }
    }

    private void updateDB(PostInfo postInfo){
        if(successCount == 0){
            DocumentReference documentReference = firebaseFirestore.collection("posts").document(postInfo.getId());
            documentReference.update("title",postInfo.getTitle());
            documentReference.update("description",postInfo.getDescription());
            documentReference.update("contents",postInfo.getContents());

            Intent resultIntent = new Intent();
            resultIntent.putExtra("postinfo", postInfo);
            // 글 작성 후 다시 MakePost로 돌아오면 meetingName이 사라져서 다시 전달함
            resultIntent.putExtra("Code",meetingcode);
            setResult(RESULT_OK, resultIntent);
            finish();
        }
    }

    // 처음 올리는 글일 때
    private void storageUpload() {
        String title = ((EditText) findViewById(R.id.titleEditText)).getText().toString();
        String description = ((EditText) findViewById(R.id.contentsEditText)).getText().toString();

        if (title.length() > 0) {
            loaderLayout.setVisibility(View.VISIBLE);
            final ArrayList<String> contentsList = new ArrayList<>();
            user = FirebaseAuth.getInstance().getCurrentUser();

            FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
            // 만약 새로 작성한 글이면 postInfo == null -> posts 밑에 새 ID로 문서 생성
            // 기존 글을 수정하는거면 postInfo != null -> postInfo ID 찾아서 해당 글 문서에 다시 업로드
            final DocumentReference documentReference = firebaseFirestore.collection("posts").document();
            final Date date = new Date();

            for (int i = 0; i < parent.getChildCount(); i++) {
                LinearLayout linearLayout = (LinearLayout) parent.getChildAt(i);
                for (int ii = 0; ii < linearLayout.getChildCount(); ii++) {
                    View view = linearLayout.getChildAt(ii);
                    if (view instanceof EditText) {
                        String text = ((EditText) view).getText().toString();
                        if (text.length() > 0) {
                            // 내용 리스트에 약속 설명 추가
                        }

                    }
                    else if (!isStorageUrl(pathList.get(pathCount))) {
                        String path = pathList.get(pathCount);
                        successCount++;
                        contentsList.add(path);
                        String[] pathArray = path.split("\\.");
                        // posts 테이블의 문서 ID 받아서 해당 문서에 정보 업로드
                        final StorageReference mountainImagesRef = storageRef.child("posts/" + documentReference.getId() + "/" + pathCount + "." + pathArray[pathArray.length - 1]);
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
                                                storeUpload(documentReference, new PostInfo(title, description, contentsList, user.getUid(), date, meetingcode));
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
            if(successCount == 0) {
                storeUpload(documentReference, new PostInfo(title, description, contentsList, user.getUid(), date, meetingcode));
            }
        } else {
            showToast(MakePostActivity.this, "제목을 입력해주세요.");
        }
    }

    // db에 등록 성공했는지 검사, 다시 FragBoard로 돌아옴
    private void storeUpload(DocumentReference documentReference, final PostInfo postInfo) {
        documentReference.set(postInfo.getPostInfo())
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d(TAG, "DocumentSnapshot successfully written!");
                        loaderLayout.setVisibility(View.GONE);
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("postinfo", postInfo);
                        // 글 작성 후 다시 MakePost로 돌아오면 meetingName이 사라져서 다시 전달함
                        resultIntent.putExtra("Code",meetingcode);
                        setResult(RESULT_OK, resultIntent);
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w(TAG, "Error writing document", e);
                        loaderLayout.setVisibility(View.GONE);
                    }
                });
    }

    // 작성한 글이 보이게 함
    private void postInit() {
        if (postInfo != null) {
            titleEditText.setText(postInfo.getTitle());
            descriptionText.setText(postInfo.getDescription());
            ArrayList<String> contentsList = postInfo.getContents();
            for (int i = 0; i < contentsList.size(); i++) {
                String contents = contentsList.get(i);
                if (isStorageUrl(contents)) {
                    showList.add(contents);
                    ContentsItemView contentsItemView = new ContentsItemView(this);
                    parent.addView(contentsItemView);

                    contentsItemView.setImage(contents);
                    // 이미지 누르면 삭제 버튼 보이게 함
                    contentsItemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            buttonsBackgroundLayout.setVisibility(View.VISIBLE);
                            selectedImageVIew = (ImageView) v;
                        }
                    });

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