package com.example.mmmmeeting;

import com.example.mmmmeeting.Info.PostInfo;
import com.example.mmmmeeting.Info.ScheduleInfo;

public interface OnPostListener {
    void onDelete(PostInfo postInfo);
    void onModify();
}
