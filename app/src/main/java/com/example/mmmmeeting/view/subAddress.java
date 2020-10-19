package com.example.mmmmeeting.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.example.mmmmeeting.R;

// 주소 출력을 위한 클래스 : sub_Address를 사용 가능하게
public class subAddress extends LinearLayout {

    public subAddress(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public subAddress(Context context) {
        super(context);
        init(context);
    }
    private void init(Context context){
        LayoutInflater inflater =(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.sub_address,this,true);
    }
}