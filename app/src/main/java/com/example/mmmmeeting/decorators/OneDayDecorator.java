package com.example.mmmmeeting.decorators;

import android.app.Activity;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;

import com.example.mmmmeeting.R;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;

import java.util.Date;

/**
 * Decorate a day by making the text big and bold
 */
public class OneDayDecorator implements DayViewDecorator { //오늘 날짜를 꾸며줌

    private CalendarDay date;
    private final Drawable drawable;

    public OneDayDecorator(Activity context) {
        date = CalendarDay.today();
        drawable = context.getResources().getDrawable(R.drawable.more);
    }

    @Override
    public boolean shouldDecorate(CalendarDay day) {
        return date != null && day.equals(date);
    }

    @Override
    public void decorate(DayViewFacade view) {
        view.setSelectionDrawable(drawable); // 날짜 테두리 그리기
    }

    /**
     * We're changing the internals, so make sure to call {@linkplain MaterialCalendarView#invalidateDecorators()}
     */
    public void setDate(Date date) {
        this.date = CalendarDay.from(date);
    }
}
