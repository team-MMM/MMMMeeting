package com.example.mmmmeeting.activity;

import androidx.appcompat.app.AppCompatActivity;

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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import com.example.mmmmeeting.Info.ScheduleInfo;
import com.example.mmmmeeting.DeviceBootReceiver;
import com.example.mmmmeeting.AlarmReceiver;
import com.example.mmmmeeting.R;
import android.os.Bundle;

public class NoticeActivity extends AppCompatActivity {

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_notice);

            final TimePicker picker=(TimePicker)findViewById(R.id.timePicker);
            picker.setIs24HourView(true);


            // 앞서 설정한 값으로 보여주기
            // 없으면 디폴트 값은 현재시간
            SharedPreferences sharedPreferences = getSharedPreferences("daily alarm", MODE_PRIVATE);
            long millis = sharedPreferences.getLong("nextNotifyTime", Calendar.getInstance().getTimeInMillis());

            Calendar nextNotifyTime = new GregorianCalendar();
            nextNotifyTime.setTimeInMillis(millis);

            /*
            Date nextDate = nextNotifyTime.getTime();
            String date_text = new SimpleDateFormat("yyyy년 MM월 dd일 EE요일 a hh시 mm분 ", Locale.getDefault()).format(nextDate);
            Toast.makeText(getApplicationContext(),"[처음 실행시] 다음 알람은 " + date_text + "으로 알람이 설정되었습니다!", Toast.LENGTH_SHORT).show();

             */


            // 이전 설정값으로 TimePicker 초기화
            Date currentTime = nextNotifyTime.getTime();
            SimpleDateFormat HourFormat = new SimpleDateFormat("kk", Locale.getDefault());
            SimpleDateFormat MinuteFormat = new SimpleDateFormat("mm", Locale.getDefault());

            int pre_hour = Integer.parseInt(HourFormat.format(currentTime));
            int pre_minute = Integer.parseInt(MinuteFormat.format(currentTime));


            if (Build.VERSION.SDK_INT >= 23 ){
                picker.setHour(pre_hour);
                picker.setMinute(pre_minute);
            }
            else{
                picker.setCurrentHour(pre_hour);
                picker.setCurrentMinute(pre_minute);
            }


            Button button = (Button) findViewById(R.id.button);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {

                    int hour, hour_24, minute;
                    String am_pm;
                    if (Build.VERSION.SDK_INT >= 23 ){
                        hour_24 = picker.getHour();
                        minute = picker.getMinute();
                    }
                    else{
                        hour_24 = picker.getCurrentHour();
                        minute = picker.getCurrentMinute();
                    }
                    if(hour_24 > 12) {
                        am_pm = "PM";
                        hour = hour_24 - 12;
                    }
                    else
                    {
                        hour = hour_24;
                        am_pm="AM";
                    }

                    Calendar da = (Calendar) getIntent().getSerializableExtra("alarm");
                    if(da!=null){
                        diaryNotification(da);
                    }

                  //  Intent intent = getIntent();
                    int day[] = new int[2];
                    day = (int[]) getIntent().getSerializableExtra("meetingdate");

                    // 현재 지정된 시간으로 알람 시간 설정
                    Calendar calendar = Calendar.getInstance();
                    calendar.setTimeInMillis(System.currentTimeMillis());
                    calendar.set(Calendar.MONTH , day[0]);
                    calendar.set(Calendar.DAY_OF_MONTH , day[1]);
                    calendar.set(Calendar.HOUR_OF_DAY, hour_24);
                    calendar.set(Calendar.MINUTE, minute);
                    calendar.set(Calendar.SECOND, 0);

                    /*
                    // 이미 지난 시간을 지정했다면 다음날 같은 시간으로 설정
                    if (calendar.before(Calendar.getInstance())) {
                        calendar.add(Calendar.DATE, 1);
                    }*/

                    Date currentDateTime = calendar.getTime();
                    String date_text = new SimpleDateFormat("yyyy년 MM월 dd일 EE요일 a hh시 mm분 ", Locale.getDefault()).format(currentDateTime);
                    Toast.makeText(getApplicationContext(),date_text + "으로 약속이 설정되었습니다!", Toast.LENGTH_SHORT).show();

                    //  Preference에 설정한 값 저장
                    calendar.set(Calendar.HOUR_OF_DAY, hour_24-1);
                    SharedPreferences.Editor editor = getSharedPreferences("daily alarm", MODE_PRIVATE).edit();
                    editor.putLong("nextNotifyTime", (long)calendar.getTimeInMillis());
                    editor.apply();

                    diaryNotification(calendar);

                    Calendar cal = calendar;
//                    cal.set(Calendar.HOUR_OF_DAY, hour_24-9); //db와 9시간 차 없애줘야함(UTC+9)
                    cal.set(Calendar.HOUR_OF_DAY, hour_24);

                    Date dateTime = cal.getTime();
                    Intent intentT = new Intent();
                    intentT.putExtra("fulldate" , dateTime); //전체 날짜 전달

                    setResult(RESULT_OK, intentT);
                    finish();
                }

            });
        }


    void diaryNotification(Calendar calendar)
    {
//        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
//        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
//        Boolean dailyNotify = sharedPref.getBoolean(SettingsActivity.KEY_PREF_DAILY_NOTIFICATION, true);
        Boolean dailyNotify = true; // 무조건 알람을 사용

        String schedule = (String) getIntent().getSerializableExtra("scInfo");

        System.out.println("확인확인     " + schedule);

        String y =  String.valueOf(calendar.get(Calendar.YEAR));
        String m =  String.valueOf(calendar.get(Calendar.MONTH));
        String d =  String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
        String h =  String.valueOf(calendar.get(Calendar.HOUR_OF_DAY));
        String s =  String.valueOf(calendar.get(Calendar.MINUTE));

        String hh =  String.valueOf(calendar.get(Calendar.HOUR_OF_DAY)+1); //원래 시간

        String one = y + m;
        String two = d + h + s;
        int in1 = Integer.parseInt(one);
        int in2 = Integer.parseInt(two);
        int index = in1 - in2; //약속 id로
        String time = hh + "시 " + s + "분";

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
