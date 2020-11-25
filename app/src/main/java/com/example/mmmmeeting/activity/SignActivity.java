package com.example.mmmmeeting.activity;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.target.ImageViewTarget;
import com.example.mmmmeeting.R;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class SignActivity extends BasicActivity {
    private FirebaseAuth mAuth = null;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int RC_SIGN_IN = 9001;
    private SignInButton signInButton;
    private ImageView img_logo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setToolbarTitle("우리 지금 만나");

        img_logo = (ImageView)findViewById(R.id.img_logo);

        Glide.with(SignActivity.this)
                .load(R.drawable.wmn_logo)
                .into(new GifDrawableImageViewTarget(img_logo, 1));


        signInButton = findViewById(R.id.signInButton);
        mAuth = FirebaseAuth.getInstance();

        if (mAuth.getCurrentUser() != null) {
            Intent intent = new Intent(getApplication(), MainActivity.class);
            startActivity(intent);
            finish();
        }

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });

    }
    public class GifDrawableImageViewTarget extends ImageViewTarget<Drawable> {

        private int mLoopCount = GifDrawable.LOOP_FOREVER;

        public GifDrawableImageViewTarget(ImageView view, int loopCount) {
            super(view);
            mLoopCount = loopCount;
        }

        public GifDrawableImageViewTarget(ImageView view, int loopCount, boolean waitForLayout) {
            super(view, waitForLayout);
            mLoopCount = loopCount;
        }

        @Override
        protected void setResource(@Nullable Drawable resource) {
            if (resource instanceof GifDrawable) {
                ((GifDrawable) resource).setLoopCount(mLoopCount);
            }
            view.setImageDrawable(resource);
        }
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Result returned from launching the Intent from GoogleSignInApi.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                // Google Sign In was successful, authenticate with Firebase
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account);
            } catch (ApiException e) {
            }
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            readDocId(user);
                        } else {
                            startToast("인증에 실패하였습니다.");
                        }
                    }
                });
    }

    private void updateUI(boolean check) { //update ui code here
        Intent intent;
        if(check){
            // 같은 uid 존재 -> 바로 메인 화면으로
            Log.d("Document Read","exist uid");
            intent = new Intent(this,MainActivity.class);
        }else{
            // 같은 uid 없으면 -> 회원 정보 입력 창으로
            Log.d("Document Read","new uid");
            intent = new Intent(this, MemberInitActivity.class);
        }
        startActivity(intent);
    }

    private void startToast(String msg){
        Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();
    }

    // user table 읽어서 같은 uid가 존재하는지 확인
    private void readDocId(final FirebaseUser user){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if(user!=null){
            DocumentReference docRef = db.collection("users").document(user.getUid());
            docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if(task.isSuccessful()){
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            // 로그인 시도한 uid가 이미 document에 존재
                            Log.d("Document Snapshot", "Data is : "+document.getId());
                            updateUI(true);
                        } else {
                            // 로그인 시도한 uid가 document에 없음
                            Log.d("Document Snapshot", "No Document");
                            updateUI(false);
                        }
                    }
                    else {
                        Log.d("Document Snapshot", "Task Fail : "+task.getException());
                    }
                }

            });
        }
    }
}