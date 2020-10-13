package com.example.mmmmeeting.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import androidx.annotation.NonNull;

import com.example.mmmmeeting.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends BasicActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setToolbarTitle("우리 지금 만나");

        mAuth = FirebaseAuth.getInstance();
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

    public void signOut(){
        FirebaseAuth.getInstance().signOut();
    }

    public void revokeAccess() {
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
