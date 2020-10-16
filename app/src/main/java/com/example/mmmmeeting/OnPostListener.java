package com.example.mmmmeeting;

import com.example.mmmmeeting.Info.PostInfo;

public interface OnPostListener {
    void onDelete(PostInfo postInfo);
    void onModify();
}