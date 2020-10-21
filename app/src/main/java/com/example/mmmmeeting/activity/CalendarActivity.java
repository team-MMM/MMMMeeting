// 공유 캘린더 코드

package com.example.mmmmeeting.activity;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.TextView;

import java.io.FileInputStream;
import java.io.FileOutputStream;

import com.example.mmmmeeting.BoardDeleter;
import com.example.mmmmeeting.Info.PostInfo;
import com.example.mmmmeeting.R;


public class CalendarActivity extends AppCompatActivity {
    public String fname=null;
    public String str=null;
    public CalendarView calendarView;
    public Button cha_Btn,del_Btn,save_Btn;
    public TextView diaryTextView,textView2,textView3;
    public EditText contextEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        calendarView=findViewById(R.id.calendarView);
        diaryTextView=findViewById(R.id.diaryTextView);
        save_Btn=findViewById(R.id.save_Btn);
        del_Btn=findViewById(R.id.del_Btn);
        cha_Btn=findViewById(R.id.cha_Btn);
        textView2=findViewById(R.id.textView2);
        textView3=findViewById(R.id.textView3);
        contextEditText=findViewById(R.id.contextEditText);
        //로그인 및 회원가입 엑티비티에서 이름을 받아옴
        Intent intent=getIntent();
        //String name=intent.getStringExtra(""); 원래는 로그인에서 이름을 받아옴
        String name="(모임 이름)";
        final String userID=intent.getStringExtra("userID");
        textView3.setText(name+"의 캘린더");

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) { //달력 날짜가 선택되면
                diaryTextView.setVisibility(View.VISIBLE); // 해당 날짜가 뜨는 textView Visible
                save_Btn.setVisibility(View.VISIBLE); // 저장 버튼 Visivble
                contextEditText.setVisibility(View.VISIBLE); // EditText가 Visible
                textView2.setVisibility(View.INVISIBLE); // 저장된 내용이 Visible
                cha_Btn.setVisibility(View.INVISIBLE); // 수정 버튼 Invisible
                del_Btn.setVisibility(View.INVISIBLE); // 삭제 버튼 Invisible
                diaryTextView.setText(String.format("%d / %d / %d",year,month+1,dayOfMonth)); // 날짜를 보여주는 텍스트에 해당 날짜를 넣음
                contextEditText.setText(""); // EditTecx에 공백 넣음
                checkDay(year,month,dayOfMonth,userID); // checkDay 호출
            }
        });
        save_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { //저장 버튼 클릭
                saveDiary(fname);
                str=contextEditText.getText().toString(); // EditText 냐용을 str에 저장
                textView2.setText(str); // TextView에 str 출력
                save_Btn.setVisibility(View.INVISIBLE);
                cha_Btn.setVisibility(View.VISIBLE);
                del_Btn.setVisibility(View.VISIBLE);
                contextEditText.setVisibility(View.INVISIBLE);
                textView2.setVisibility(View.VISIBLE);

            }
        });
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
            textView2.setVisibility(View.VISIBLE);
            textView2.setText(str);

            save_Btn.setVisibility(View.INVISIBLE);
            cha_Btn.setVisibility(View.VISIBLE);
            del_Btn.setVisibility(View.VISIBLE);

            cha_Btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) { // 수정 버튼 클릭
                    contextEditText.setVisibility(View.VISIBLE);
                    textView2.setVisibility(View.INVISIBLE);
                    contextEditText.setText(str); // editText에 textView에 저장된 내용 출력

                    save_Btn.setVisibility(View.VISIBLE);
                    cha_Btn.setVisibility(View.INVISIBLE);
                    del_Btn.setVisibility(View.INVISIBLE);
                    textView2.setText(contextEditText.getText());
                }

            });
            del_Btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    textView2.setVisibility(View.INVISIBLE);
                    contextEditText.setText("");
                    contextEditText.setVisibility(View.VISIBLE);
                    save_Btn.setVisibility(View.VISIBLE);
                    cha_Btn.setVisibility(View.INVISIBLE);
                    del_Btn.setVisibility(View.INVISIBLE);
                    removeDiary(fname);
                }
            });
            if(textView2.getText()==null){
                textView2.setVisibility(View.INVISIBLE);
                diaryTextView.setVisibility(View.VISIBLE);
                save_Btn.setVisibility(View.VISIBLE);
                cha_Btn.setVisibility(View.INVISIBLE);
                del_Btn.setVisibility(View.INVISIBLE);
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
}
