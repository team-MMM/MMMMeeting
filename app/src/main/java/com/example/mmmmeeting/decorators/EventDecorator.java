package com.example.mmmmeeting.decorators;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.graphics.Typeface;
import android.text.style.StyleSpan;

import com.example.mmmmeeting.R;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.spans.DotSpan;

import java.util.Collection;
import java.util.HashSet;

/**
 * Decorate several days with a dot
 */
public class EventDecorator implements DayViewDecorator {

    private int color;
    private HashSet<CalendarDay> dates;
    private int check;
    private final Drawable drawable;

    public EventDecorator(int color, Collection<CalendarDay> dates, Activity context, int check) {
        this.color = color;
        this.dates = new HashSet<>(dates);
        this.check = check;
        drawable = context.getResources().getDrawable(R.drawable.more);
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return dates.contains(day);
    }

    @Override
    public void decorate(DayViewFacade view) {
        if(check==0){
            view.setSelectionDrawable(drawable); // 날짜 테두리 그리기
        }
        else if(check==1) {
            view.addSpan(new DotSpan(6, color)); // 날짜 밑에 점
        }
    }
}
