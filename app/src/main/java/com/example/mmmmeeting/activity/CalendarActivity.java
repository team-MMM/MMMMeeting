// 공유 캘린더 코드

package com.example.mmmmeeting.activity;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.Executors;

import com.example.mmmmeeting.decorators.EventDecorator;
import com.example.mmmmeeting.decorators.OneDayDecorator;
import com.example.mmmmeeting.decorators.SaturdayDecorator;
import com.example.mmmmeeting.decorators.SundayDecorator;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.CalendarMode;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import com.example.mmmmeeting.BoardDeleter;
import com.example.mmmmeeting.Info.PostInfo;
import com.example.mmmmeeting.R;
import com.example.mmmmeeting.Info.ScheduleInfo;
import com.google.firebase.storage.FirebaseStorage;

public class CalendarActivity extends AppCompatActivity {
    public String fname=null; //날짜별 메모 저장 파일 이름
    public String str=null;
    public Button cha_Btn,del_Btn,save_Btn,sel_Btn;;
    public TextView diaryTextView,memotext;
    public EditText contextEditText;

    private final OneDayDecorator oneDayDecorator = new OneDayDecorator();
    MaterialCalendarView materialCalendarView;
    ArrayList<String> result = new ArrayList<>();//점 표시할 날짜들

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        materialCalendarView = (MaterialCalendarView)findViewById(R.id.calendarView);

        materialCalendarView.state().edit()
                .setFirstDayOfWeek(Calendar.SUNDAY)
               // .setMinimumDate(CalendarDay.from(2017, 0, 1)) // 달력의 시작
                // .setMaximumDate(CalendarDay.from(2030, 11, 31)) // 달력의 끝
                .setCalendarDisplayMode(CalendarMode.MONTHS)
                .commit();

        materialCalendarView.addDecorators(
                new SundayDecorator(),
                new SaturdayDecorator(),
                oneDayDecorator);

        diaryTextView=findViewById(R.id.diaryTextView);
        save_Btn=findViewById(R.id.save_Btn);
        del_Btn=findViewById(R.id.del_Btn);
        cha_Btn=findViewById(R.id.cha_Btn);
        sel_Btn=findViewById(R.id.sel_Btn);
        memotext=findViewById(R.id.memotext);
        contextEditText=findViewById(R.id.contextEditText);
        Intent intent=getIntent();

        final String userID=intent.getStringExtra("userID");  // 저장되는 파일 이름, db에서 모임 이름 받아와야함

        new ApiSimulator(result).executeOnExecutor(Executors.newSingleThreadExecutor()); // 데코 표시

        materialCalendarView.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView widget, @NonNull CalendarDay date, boolean selected) { //달력 날짜가 선택되면
                diaryTextView.setVisibility(View.VISIBLE); // 해당 날짜가 뜨는 textView Visible
                save_Btn.setVisibility(View.VISIBLE); // 저장 버튼 Visivble
                contextEditText.setVisibility(View.VISIBLE); // EditText가 Visible
                memotext.setVisibility(View.INVISIBLE); // 저장된 내용이 Visible
                cha_Btn.setVisibility(View.INVISIBLE); // 수정 버튼 Invisible
                del_Btn.setVisibility(View.INVISIBLE); // 삭제 버튼 Invisible
                sel_Btn.setVisibility(View.INVISIBLE); // 선택 버튼 Invisible

                int Year = date.getYear();
                int Month = date.getMonth() + 1;
                int Day = date.getDay();

                Log.i("Year test", Year + "");
                Log.i("Month test", Month + "");
                Log.i("Day test", Day + "");

                final String shot_Day = Year + "," + Month + "," + Day;

                Log.i("shot_Day test", shot_Day + "");
                materialCalendarView.clearSelection();

      //         Toast.makeText(getApplicationContext(), shot_Day , Toast.LENGTH_SHORT).show();

                diaryTextView.setText(String.format("%d / %d / %d",Year,Month,Day)); // 날짜를 보여주는 텍스트에 해당 날짜를 넣음
                contextEditText.setText(""); // EditTecx에 공백 넣음
                checkDay(Year,Month,Day,userID); // checkDay 호출

                sel_Btn.setOnClickListener(new View.OnClickListener() { //선택 버튼 누르면
                    @Override
                    public void onClick(View view) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(CalendarActivity.this);
                        builder.setTitle("날짜 확정")        // 제목
                                .setMessage(Year+"-"+Month+"-"+Day+" 로 설정하시겠습니까?")        // 메세지
                               // .setCancelable(false)        // 뒤로 버튼 클릭시 취소 가능 설정
                                .setPositiveButton("확인", new DialogInterface.OnClickListener(){
                                    // 확인 버튼 클릭시 설정, 오른쪽 버튼입니다.
                                    public void onClick(DialogInterface dialog, int whichButton){
                                        //약속 날짜를 확정
                                        //db로 해당 날짜 올리기
                                    }
                                })
                                .setNegativeButton("취소", new DialogInterface.OnClickListener(){// 취소 버튼 클릭시
                                    public void onClick(DialogInterface dialog, int whichButton){//취소 이벤트...
                                    }
                                });
                        AlertDialog dialog = builder.create();    // 알림창 객체 생성
                        dialog.show();    // 알림창 띄우기
                    }
                });

                save_Btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) { //저장 버튼 클릭
                        saveDiary(fname);
                        str=contextEditText.getText().toString(); // EditText 내용을 str에 저장
                        memotext.setText(str); // TextView에 str 출력
                        save_Btn.setVisibility(View.INVISIBLE); // 저장 버튼 Invisible
                        cha_Btn.setVisibility(View.VISIBLE); // 수정 버튼 Visible
                        del_Btn.setVisibility(View.VISIBLE); // 삭제 버튼 Visible
                        sel_Btn.setVisibility(View.VISIBLE); // 선택 버튼 Visible
                        contextEditText.setVisibility(View.INVISIBLE);
                        memotext.setVisibility(View.VISIBLE);

                        result.add(shot_Day);   // result에 메모가 저장된 날짜 추가
                        new ApiSimulator(result).executeOnExecutor(Executors.newSingleThreadExecutor()); // 데코 표시
                    }
                });
            }
        });
    }
    private class ApiSimulator extends AsyncTask<Void, Void, List<CalendarDay>> {

        ArrayList<String> Time_Result;

        ApiSimulator(ArrayList<String> Time_Result) {
            this.Time_Result = Time_Result;
        }

        @Override
        protected List<CalendarDay> doInBackground(@NonNull Void... voids) {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            Calendar calendar = Calendar.getInstance();

            calendar.add(Calendar.MONTH, -1); //한달전
            String name=null;
            ArrayList<CalendarDay> dates = new ArrayList<>();
            for (int i = 0; i < 30; i++) {//한달 동안 메모가 있으면 이벤트 표시
                CalendarDay day = CalendarDay.from(calendar);
                name=""+"null"+day.getYear()+"-"+(day.getMonth()+1)+""+"-"+(day.getDay()+1)+".txt";
                if(checkEvent(name)==1)
                    dates.add(day);
                calendar.add(Calendar.DATE, 1);
            }

            /*특정날짜 달력에 점표시해주는곳*/
            /*월은 0이 1월 년,일은 그대로*/
            //string 문자열인 Time_Result 을 받아와서 ,를 기준으로짜르고 string을 int 로 변환

            for (int i = 0; i < Time_Result.size(); i++) {

                String[] time = Time_Result.get(i).split(",");
                int year = Integer.parseInt(time[0]);
                int month = Integer.parseInt(time[1]);
                int dayy = Integer.parseInt(time[2]);

                calendar.set(year, month - 1, dayy);
                CalendarDay day = CalendarDay.from(calendar);
                dates.add(day);
            }

            return dates;
        }

        @Override
        protected void onPostExecute(@NonNull List<CalendarDay> calendarDays) {
            super.onPostExecute(calendarDays);

            if (isFinishing()) {
                return;
            }
            materialCalendarView.addDecorator(new EventDecorator(Color.BLUE, calendarDays, CalendarActivity.this));
        }
    }

    public void  checkDay(int cYear,int cMonth,int cDay,String userID){
        fname=""+userID+cYear+"-"+(cMonth+1)+""+"-"+cDay+".txt";//저장할 파일 이름설정 ex) 2019-01-20.txt
        FileInputStream fis=null;//FileStream fis 변수

        try{
            fis=openFileInput(fname); // fname 파일 오픈

            byte[] fileData=new byte[fis.available()];
            fis.read(fileData); // fileDate 읽음
            fis.close();

            str=new String(fileData); // str에 fileDate 저장

            contextEditText.setVisibility(View.INVISIBLE);
            memotext.setVisibility(View.VISIBLE);
            memotext.setText(str);

            save_Btn.setVisibility(View.INVISIBLE);
            cha_Btn.setVisibility(View.VISIBLE);
            del_Btn.setVisibility(View.VISIBLE);
            sel_Btn.setVisibility(View.VISIBLE); // 선택 버튼 Visible
            cha_Btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) { // 수정 버튼 클릭
                    contextEditText.setVisibility(View.VISIBLE);
                    memotext.setVisibility(View.INVISIBLE);
                    contextEditText.setText(str); // editText에 textView에 저장된 내용 출력

                    save_Btn.setVisibility(View.VISIBLE);
                    cha_Btn.setVisibility(View.INVISIBLE);
                    del_Btn.setVisibility(View.INVISIBLE);
                    sel_Btn.setVisibility(View.INVISIBLE);
                    memotext.setText(contextEditText.getText());
                }

            });
            del_Btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    memotext.setVisibility(View.INVISIBLE);
                    contextEditText.setText("");
                    contextEditText.setVisibility(View.VISIBLE);
                    save_Btn.setVisibility(View.VISIBLE);
                    cha_Btn.setVisibility(View.INVISIBLE);
                    del_Btn.setVisibility(View.INVISIBLE);
                    sel_Btn.setVisibility(View.INVISIBLE);
                    removeDiary(fname);

                    result.remove(cYear+","+cMonth+","+cDay);   // 이벤트 표시에서 메모 삭제되는 날 지우기
                    materialCalendarView.removeDecorators();    // 모든 데코 지우기
                    materialCalendarView.addDecorators(oneDayDecorator);    //오늘 날짜 데코
                    new ApiSimulator(result).executeOnExecutor(Executors.newSingleThreadExecutor()); // 데코 표시
                }
            });
            if(memotext.getText()==null){
                memotext.setVisibility(View.INVISIBLE);
                diaryTextView.setVisibility(View.VISIBLE);
                save_Btn.setVisibility(View.VISIBLE);
                cha_Btn.setVisibility(View.INVISIBLE);
                del_Btn.setVisibility(View.INVISIBLE);
                sel_Btn.setVisibility(View.INVISIBLE);
                contextEditText.setVisibility(View.VISIBLE);
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }
    @SuppressLint("WrongConstant")
    public void removeDiary(String readDay){ //달력 내용 삭제
        FileOutputStream fos=null;
        try{
            fos=openFileOutput(readDay,MODE_NO_LOCALIZED_COLLATORS);
            String content="";
            fos.write((content).getBytes());
            fos.close();

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @SuppressLint("WrongConstant")
    public void saveDiary(String readDay){  // 달력 내용 저장
        FileOutputStream fos=null;
        try{
            fos=openFileOutput(readDay,MODE_NO_LOCALIZED_COLLATORS);
            String content=contextEditText.getText().toString();
            fos.write((content).getBytes());
            fos.close();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public int checkEvent(String fname) { //메모가 있는지 확인
        FileInputStream fis = null;//FileStream fis 변수
        try {
            fis = openFileInput(fname); // fname 파일 오픈
            byte[] fileData = new byte[fis.available()];
            fis.read(fileData); // fileDate 읽음
            fis.close();
            str = new String(fileData); // str에 fileDate 저장
            if (str != null)
                return 1;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}
