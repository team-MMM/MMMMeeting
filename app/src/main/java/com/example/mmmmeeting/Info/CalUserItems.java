package com.example.mmmmeeting.Info;

import android.text.Editable;
import android.text.TextWatcher;

public class CalUserItems {
    private String userName;
    private String money;
    public TextWatcher mTextWatcher;

    public CalUserItems(String userName){
        this.userName = userName;
        this.money = "0";

        //EditText 변경 리스너 생성
        mTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //변경된 값을 저장한다
                money = s.toString();
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        };

    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName){
        this.userName = userName;
    }

    public String getMoney() {
        return money;
    }

    public void setMoney(String money) {
        this.money = money;
    }

}
