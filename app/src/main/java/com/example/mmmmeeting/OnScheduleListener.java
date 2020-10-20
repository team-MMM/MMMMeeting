package com.example.mmmmeeting;

import com.example.mmmmeeting.Info.ScheduleInfo;

public interface OnScheduleListener {
    void onDelete(ScheduleInfo postInfo);
    void onModify();
}
