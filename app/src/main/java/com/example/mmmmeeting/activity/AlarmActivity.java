package com.example.mmmmeeting.activity;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.mmmmeeting.AlarmReceiver;
import com.example.mmmmeeting.DeviceBootReceiver;
import com.example.mmmmeeting.Info.ScheduleInfo;
import com.example.mmmmeeting.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import androidx.appcompat.app.AppCompatActivity;

public class AlarmActivity extends AppCompatActivity {
    ScheduleInfo schedule;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Calendar calendar;
        calendar = (Calendar) getIntent().getSerializableExtra("alarm");

        /*
        Date currentDateTime = calendar.getTime();
        String date_text = new SimpleDateFormat("yyyy년 MM월 dd일 EE요일 a hh시 mm분 ", Locale.getDefault()).format(currentDateTime);
        Toast.makeText(getApplicationContext(), date_text + "으로 약속이 설정되었습니다!", Toast.LENGTH_SHORT).show();
*/
        //  Preference에 설정한 값 저장
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        calendar.set(Calendar.HOUR_OF_DAY, hour - 1);
        SharedPreferences.Editor editor = getSharedPreferences("daily alarm", MODE_PRIVATE).edit();
        editor.putLong("nextNotifyTime", (long) calendar.getTimeInMillis());
        editor.apply();

        diaryNotification(calendar);

        setResult(RESULT_OK);
        finish();
    }


    void diaryNotification(Calendar calendar) {
//        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
//        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
//        Boolean dailyNotify = sharedPref.getBoolean(SettingsActivity.KEY_PREF_DAILY_NOTIFICATION, true);
        Boolean dailyNotify = true; // 무조건 알람을 사용

        String y = String.valueOf(calendar.get(Calendar.YEAR));
        String m = String.valueOf(calendar.get(Calendar.MONTH));
        String d = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
        String h = String.valueOf(calendar.get(Calendar.HOUR_OF_DAY));
        String s = String.valueOf(calendar.get(Calendar.MINUTE));

        String hh =  String.valueOf(calendar.get(Calendar.HOUR_OF_DAY)+1); //원래 시간

        String one = y + m;
        String two = d + h + s;
        int in1 = Integer.parseInt(one);
        int in2 = Integer.parseInt(two);
        int index = in1 - in2; //약속 id로
        String time = hh + "시 " + s + "분";

        String schedule = (String) getIntent().getSerializableExtra("date");

        PackageManager pm = this.getPackageManager();
        ComponentName receiver = new ComponentName(this, DeviceBootReceiver.class);
        Intent alarmIntent = new Intent(this, AlarmReceiver.class);

        alarmIntent.putExtra("index", index);
        alarmIntent.putExtra("scInfo", schedule);
        alarmIntent.putExtra("time", time);

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, index, alarmIntent, 0);
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        // 사용자가 매일 알람을 허용했다면
        if (dailyNotify) {
            if (alarmManager != null) {
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), pendingIntent);
                }
            }

            // 부팅 후 실행되는 리시버 사용가능하게 설정
            pm.setComponentEnabledSetting(receiver,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);

        }/*
        else { //Disable Daily Notifications
            if (PendingIntent.getBroadcast(this, index, alarmIntent, 0) != null && alarmManager != null) {
                alarmManager.cancel(pendingIntent);
                //Toast.makeText(this,"Notifications were disabled",Toast.LENGTH_SHORT).show();
            }
            pm.setComponentEnabledSetting(receiver,
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP);
        }*/
    }
}
