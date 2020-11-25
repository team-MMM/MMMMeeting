// 공유 캘린더 코드

package com.example.mmmmeeting.activity;

import android.app.Activity;
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
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.Iterator;
import java.util.concurrent.Executors;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import com.google.firebase.Timestamp;

import com.example.mmmmeeting.decorators.EventDecorator;
import com.example.mmmmeeting.decorators.OneDayDecorator;
import com.example.mmmmeeting.decorators.SaturdayDecorator;
import com.example.mmmmeeting.decorators.SundayDecorator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.CalendarMode;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;


import com.example.mmmmeeting.BoardDeleter;
import com.example.mmmmeeting.Info.PostInfo;
import com.example.mmmmeeting.R;
import com.example.mmmmeeting.Info.ScheduleInfo;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.DocumentSnapshot;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import com.google.firebase.storage.FirebaseStorage;

public class CalendarActivity extends BasicActivity {
    public String fname=null; //날짜별 메모 저장 파일 이름
    public String str=null;
    public Button cha_Btn,del_Btn,save_Btn,sel_Btn;;
    public TextView diaryTextView,memotext;
    public EditText contextEditText;
    private ScheduleInfo scInfo;
    public String scID=null;
    public Date mdate; // 정해진 약속 날짜
    private FirebaseFirestore db;
    final FirebaseUser user= FirebaseAuth.getInstance().getCurrentUser();
    String userId = null; // 현재 user id
    String leaderId = null; // 모임의 방장 id

    Map<String, String> calendarMap = new HashMap<>(); // 날짜별 메모 저장
   // private final OneDayDecorator oneDayDecorator = new OneDayDecorator();
    MaterialCalendarView materialCalendarView;
    ArrayList<String> result = new ArrayList<>(); //점 표시할 날짜들
    ArrayList<String> selectedDay = new ArrayList<>(); //확정된 날짜
    SimpleDateFormat transDate = new  SimpleDateFormat("yyyy-MM-dd hh:mm:ss", java.util.Locale.getDefault());//String을 Date 형식으로 변경
    SimpleDateFormat transString = new SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault());


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        setToolbarTitle("캘린더") ;
        materialCalendarView = (MaterialCalendarView)findViewById(R.id.calendarView);

        materialCalendarView.state().edit()
                .setFirstDayOfWeek(Calendar.SUNDAY)
               // .setMinimumDate(CalendarDay.from(2017, 0, 1)) // 달력의 시작
                // .setMaximumDate(CalendarDay.from(2030, 11, 31)) // 달력의 끝
                .setCalendarDisplayMode(CalendarMode.MONTHS)
                .commit();

        materialCalendarView.addDecorators(
                new SundayDecorator(),  // 일요일 색칠
                new SaturdayDecorator(), // 토요일 색칠
                new OneDayDecorator(CalendarActivity.this)); // 오늘 날짜 색칠

        diaryTextView=findViewById(R.id.diaryTextView);
        save_Btn=findViewById(R.id.save_Btn);
        del_Btn=findViewById(R.id.del_Btn);
        cha_Btn=findViewById(R.id.cha_Btn);
        sel_Btn=findViewById(R.id.sel_Btn);
        memotext=findViewById(R.id.memotext);
        contextEditText=findViewById(R.id.contextEditText);

        db = FirebaseFirestore.getInstance();
        Intent intent = getIntent();
        scInfo = (ScheduleInfo) getIntent().getSerializableExtra("scheduleInfo");
        scID=scInfo.getId(); // schedule ID 가져오기
        userId = user.getUid(); // 현재 user id

        db.collection("meetings").document(scInfo.getMeetingID()).get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // 해당 문서가 존재하는 경우
                        leaderId=document.getData().get("leader").toString();
                        Log.d("Attend", "Data is : " + document.getId());
                    } else {
                        // 존재하지 않는 문서
                        Log.d("Attend", "No Document");
                    }
                } else {
                    Log.d("Attend", "Task Fail : " + task.getException());
                }
            }

        });

        DocumentReference docRef = db.collection("schedule").document(scID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() { //schedule에 저장된 calendarText 받아오기
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {// 해당 문서가 존재하는 경우
                        if(document.getData().get("calendarText")!=null){ // 저장된 메모가 있을 때
                            calendarMap = (Map<String, String>) document.getData().get("calendarText");
                            Set set = calendarMap.keySet();
                            Iterator iterator = set.iterator();
                            while(iterator.hasNext()){ // 점 표시할 리스트에 메모가 있는 날짜를 넣어준다
                                String key = (String)iterator.next();
                                result.add(key);
                            }
                            new ApiSimulator(result).executeOnExecutor(Executors.newSingleThreadExecutor()); // 데코 표시
                        }
                        if(document.getData().get("meetingDate")!=null){ // 확정된 날짜가 있으면
                            Date day = document.getTimestamp("meetingDate").toDate(); // 확정 날짜 받아오기
                            String sel_day = transString.format(day); // String으로 변환
                            selectedDay.add(sel_day);
                            selectDayDeco();
                        }
                        Log.d("Attend", "Data is : " + document.getId());
                    } else {// 존재하지 않는 문서
                        Log.d("Attend", "No Document");
                    }
                } else {
                    Log.d("Attend", "Task Fail : " + task.getException());
                }
            }
        });

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
                int Month = date.getMonth()+1;
                int Day = date.getDay();

                Log.i("Year test", Year + "");
                Log.i("Month test", Month + "");
                Log.i("Day test", Day + "");

                final String shot_Day = Year + "-" + Month + "-" + Day;

                Log.i("shot_Day test", shot_Day + "");
                materialCalendarView.clearSelection();

                diaryTextView.setText(String.format("%d / %d / %d",Year,Month,Day)); // 날짜를 보여주는 텍스트에 해당 날짜를 넣음
                contextEditText.setText(""); // EditTecx에 공백 넣음
                checkDay(shot_Day); // checkDay 호출

                sel_Btn.setOnClickListener(new View.OnClickListener() { //선택 버튼 누르면
                    @Override
                    public void onClick(View view) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(CalendarActivity.this);
                            builder.setTitle("날짜 확정")        // 제목
                                    .setMessage(shot_Day+"로 설정하시겠습니까?")        // 메세지
                                    // .setCancelable(false)        // 뒤로 버튼 클릭시 취소 가능 설정
                                    .setPositiveButton("확인", new DialogInterface.OnClickListener(){
                                        // 확인 버튼 클릭시 설정, 오른쪽 버튼입니다.
                                        public void onClick(DialogInterface dialog, int whichButton){//약속 날짜를 확정 //db로 해당 날짜 올리기

                                            int day[] = new int[2]; // 약속 달과 일
                                            day[0]=Month-1;
                                            day[1]=Day;
                                            Intent intent = new Intent(CalendarActivity.this, NoticeActivity.class);
                                            intent.putExtra("meetingdate", day);
                                            intent.putExtra("scInfo",  scInfo.getTitle());
                                            startActivityForResult(intent, 0); // 시간 받아오기

                                            selectedDay.clear();
                                            selectedDay.add(shot_Day);
                                            removeDeco();
                                            // selectDay(selectedDay); //확정 날짜 이벤트 표시

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
                        str=contextEditText.getText().toString(); // EditText 내용을 str에 저장
       
                        calendarMap.put(shot_Day, str);
                        db.collection("schedule").document(scID).update("calendarText", calendarMap); // calendarText에 새메모 업데이트

                        memotext.setText(str); // TextView에 str 출력
                        save_Btn.setVisibility(View.INVISIBLE); // 저장 버튼 Invisible
                        cha_Btn.setVisibility(View.VISIBLE); // 수정 버튼 Visible
                        del_Btn.setVisibility(View.VISIBLE); // 삭제 버튼 Visible
                        if(userId.equals(leaderId)) {
                            sel_Btn.setVisibility(View.VISIBLE); // 선택 버튼 Visible
                        }
                        contextEditText.setVisibility(View.INVISIBLE);
                        memotext.setVisibility(View.VISIBLE);

                        result.add(shot_Day);   // result에 메모가 저장된 날짜 추가
                        new ApiSimulator(result).executeOnExecutor(Executors.newSingleThreadExecutor()); // 데코 표시
                    }
                });
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 0:
                if (resultCode == Activity.RESULT_OK) {
                    mdate = (Date) data.getSerializableExtra("fulldate");
                    scInfo.setMeetingDate(mdate);
                    db.collection("schedule").document(scID).update("meetingDate", mdate); // 선택한 날짜로 db에 저장
                  //  Toast.makeText(getApplicationContext(), mdate+"로 약속날짜가 설정되었습니다.", Toast.LENGTH_SHORT).show();
                }
                break;
        }
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
            ArrayList<CalendarDay> dates = new ArrayList<>();
            /*특정날짜 달력에 점표시해주는곳*/
            /*월은 0이 1월 년,일은 그대로*/
            //string 문자열인 Time_Result 을 받아와서 ,를 기준으로짜르고 string을 int 로 변환
            for (int i = 0; i < Time_Result.size(); i++) {
                String[] time = Time_Result.get(i).split("-");
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
            materialCalendarView.addDecorator(new EventDecorator(Color.RED, calendarDays, CalendarActivity.this, 1));
        }
    }

    public void  checkDay(String shot_Day){
        try{
            str=new String(calendarMap.get(shot_Day)); // shot_Day로 저장된 메모를 str에 받음

            contextEditText.setVisibility(View.INVISIBLE);
            memotext.setVisibility(View.VISIBLE);
            memotext.setText(str);

            save_Btn.setVisibility(View.INVISIBLE);
            cha_Btn.setVisibility(View.VISIBLE);
            del_Btn.setVisibility(View.VISIBLE);
            if(userId.equals(leaderId)) {
                sel_Btn.setVisibility(View.VISIBLE); // 선택 버튼 Visible
            }
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
                public void onClick(View view) { // 삭제 버튼 클릭
                    memotext.setVisibility(View.INVISIBLE);
                    contextEditText.setText("");
                    contextEditText.setVisibility(View.VISIBLE);
                    save_Btn.setVisibility(View.VISIBLE);
                    cha_Btn.setVisibility(View.INVISIBLE);
                    del_Btn.setVisibility(View.INVISIBLE);
                    sel_Btn.setVisibility(View.INVISIBLE);

                    calendarMap.remove(shot_Day);
                    db.collection("schedule").document(scID).update("calendarText", calendarMap);

                  /*  DocumentReference textdel = db.collection("schedule").document(scID);
                      textdel.update("calendarText", FieldValue().delete());  필드 내용 전체 삭제 */

                    result.remove(shot_Day);   // 이벤트 표시에서 메모 삭제되는 날 지우기
                    if(selectedDay.contains(shot_Day)){ //삭제되는 날이 확정된 날이면
                        selectedDay.clear();
                        db.collection("schedule").document(scID).update("meetingDate", null); // db에 확정된 날짜를 지움
                    }
                    removeDeco();
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

    public void selectDayDeco() { //확정 날짜 표시
        Calendar calendar = Calendar.getInstance();
        ArrayList<CalendarDay> dates = new ArrayList<>();
        for (int i = 0; i < selectedDay.size(); i++) {
            String[] time = selectedDay.get(i).split("-");
            int year = Integer.parseInt(time[0]);
            int month = Integer.parseInt(time[1]);
            int dayy = Integer.parseInt(time[2]);

            calendar.set(year, month - 1, dayy);
            CalendarDay day = CalendarDay.from(calendar);
            dates.add(day);
        }
        materialCalendarView.addDecorator(new EventDecorator(Color.RED, dates, CalendarActivity.this, 0));
    }

    public void removeDeco(){
        materialCalendarView.removeDecorators();    // 모든 데코 지우기
        materialCalendarView.addDecorators( //모든 데코 표시
                new SundayDecorator(),
                new SaturdayDecorator(),
                new OneDayDecorator(CalendarActivity.this));
        selectDayDeco(); //확정 날짜 이벤트 표시
        new ApiSimulator(result).executeOnExecutor(Executors.newSingleThreadExecutor()); // 데코 표시
    }
}
