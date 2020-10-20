package com.example.mmmmeeting;

//Firestore의 db와 연동, 게시글 삭제를 담당

import android.app.Activity;

import androidx.annotation.NonNull;

import com.example.mmmmeeting.Info.ScheduleInfo;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;

import static com.example.mmmmeeting.Util.isStorageUrl;
import static com.example.mmmmeeting.Util.showToast;
import static com.example.mmmmeeting.Util.storageUrlToName;

public class ScheduleDeleter {
    private Activity activity;
    private OnScheduleListener onPostListener;
    private int successCount;

    public ScheduleDeleter(Activity activity) {
        this.activity = activity;
    }

    public ScheduleDeleter() {

    }

    public void setOnPostListener(OnScheduleListener onPostListener){
        this.onPostListener = onPostListener;
    }

    public void storageDelete(final ScheduleInfo postInfo){
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        final String id = postInfo.getId();
        ArrayList<String> contentsList = postInfo.getContents();
        for (int i = 0; i < contentsList.size(); i++) {
            String contents = contentsList.get(i);
            if (isStorageUrl(contents)) {
                successCount++;
                StorageReference desertRef = storageRef.child("schedule/" + id + "/" + storageUrlToName(contents));
                desertRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        successCount--;
                        storeDelete(id, postInfo);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        showToast(activity, "Error");
                    }
                });
            }
        }
        storeDelete(id, postInfo);
    }


    private void storeDelete(final String id, final ScheduleInfo postInfo) {
        FirebaseFirestore firebaseFirestore = FirebaseFirestore.getInstance();
        if (successCount == 0) {
            firebaseFirestore.collection("schedule").document(id)
                    .delete()
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            showToast(activity, "게시글을 삭제하였습니다.");
                            onPostListener.onDelete(postInfo);
                            //postsUpdate();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            showToast(activity, "게시글을 삭제하지 못하였습니다.");
                        }
                    });
        }
    }
}