package com.example.mmmmeeting.view;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.LinearLayout;

import com.example.mmmmeeting.R;

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
        inflater.inflate(R.layout.activity_sub_address,this,true);
    }
}