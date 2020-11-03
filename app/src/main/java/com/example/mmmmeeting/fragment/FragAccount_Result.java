package com.example.mmmmeeting.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.example.mmmmeeting.R;
import com.example.mmmmeeting.activity.MeetingActivity;

import java.text.SimpleDateFormat;
import java.util.Date;

public class FragAccount_Result extends Fragment {
    private View view;

    private int[] pay;
    private String[] user_name;
    private int total;
    private int user_num;
    LinearLayout calculate_main;
    Button btn_calculate_modify ;

    public static FragAccount_Result newInstance() {
        return new FragAccount_Result();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frag_account_result, container, false);


        //FragAccount.java에서 정보를 받아온다.
        Bundle args = getArguments();
        if(args!=null){
            System.out.println("번들로 받아오자!");
            pay = args.getIntArray("pay");
            user_name = args.getStringArray("user_name");
            total = args.getInt("total");
            user_num = args.getInt("user_num");
        }

        calculate_main = (LinearLayout)view.findViewById(R.id.calculate_main);
        btn_calculate_modify = view.findViewById(R.id.btn_calculate_modify);

        Date d = new Date(System.currentTimeMillis());
        SimpleDateFormat fm = new SimpleDateFormat("yyyy/MM/dd");
        String today = fm.format(d);

        TextView cal_title = view.findViewById(R.id.cal_title);
        //cal_title.setText(today+"정산결과");
        SpannableString s;
        s= new SpannableString(today+"  정산결과");
        s.setSpan(new RelativeSizeSpan(1.8f),0,s.length()-5,0);
        s.setSpan(new ForegroundColorSpan(Color.parseColor("#62ABD9")),0,s.length()-5,0);
        cal_title.setText(s);


        TextView cal_total = view.findViewById(R.id.cal_total);
        cal_total.setText("총 금액 : "+total);
        cal_total.setTextSize(15);

        //LinearLayout 정의
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);


        //여기서는 모임원의 수만큼 반복문을 돌면서 정산 결과를 동적 뷰로 생성한다.
        for(int i=0;i<user_num;i++){
            //LinearLayout 생성
            LinearLayout ly = new LinearLayout(getContext());
            LinearLayout.LayoutParams lyparams = params;
            lyparams.gravity= Gravity.LEFT;
            ly.setOrientation(LinearLayout.HORIZONTAL);

            //(아이콘 + 모임원이름)이 들어갈 layout
            LinearLayout ivly = new LinearLayout(getContext());
            ivly.setOrientation(LinearLayout.VERTICAL);
            ivly.setLayoutParams(params);

            //ImageView 생성(프로필 아이콘)
            ImageView siv = new ImageView(getContext());
            siv.setImageResource(R.drawable.user);
            LinearLayout.LayoutParams sivlp = new LinearLayout.LayoutParams(120,100);
            sivlp.gravity= Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL;
            siv.setLayoutParams(sivlp);
            ivly.addView(siv);

            //TextView (모임원 이름)
            TextView stiv = new TextView(getContext());
            stiv.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL);
            stiv.setText(user_name[i]);
            ivly.addView(stiv);

            ivly.setPadding(0,30,10,0);
            ly.addView(ivly);

            //TextView 생성(정산 결과)
            TextView tv = new TextView(getContext());
            int m = total/user_num-pay[i];
            System.out.println("m값은 :"+m);
            //SpannableString s;
            if(m>0){
                //tv.setText("은   "+m+"원을 더 내야함");
                s = new SpannableString("은  "+m+"원을 더 내야함");
                s.setSpan(new RelativeSizeSpan(1.2f),3,s.length()-8,0);
                s.setSpan(new ForegroundColorSpan(Color.parseColor("#62ABD9")),3,s.length()-8,0);
                tv.setText(s);
            } else if (m == 0) {
                tv.setText("은  정산완료");
            } else {
                //tv.setText("이름" + "은" + Math.abs(m) + "원을 받아야 함");
                s = new SpannableString("은  "+Math.abs(m) + "원을 받아야 함");
                s.setSpan(new RelativeSizeSpan(1.2f),3,s.length()-8,0);
                s.setSpan(new ForegroundColorSpan(Color.parseColor("#62ABD9")),3,s.length()-8,0);
                tv.setText(s);
            }
            tv.setTextSize(18);
            tv.setPadding(10,30,30,30);
            tv.setLayoutParams(params);
            ly.addView(tv);

            calculate_main.addView(ly);
        }

        btn_calculate_modify.setOnClickListener(new Button.OnClickListener() {

            @Override
            public void onClick(View v) {
                FragAccount fa = new FragAccount();
                //((MeetingActivity)getActivity()).replaceFragment(fa.newInstance(),false);
                ((MeetingActivity)getActivity()).replaceFragment(fa,false);
            }
        });
        return view;
    }


}
