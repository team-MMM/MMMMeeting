package com.example.mmmmeeting.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.mmmmeeting.R;
import com.example.mmmmeeting.activity.MeetingActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class FragAccount extends Fragment {
    private View view;

    private EditText[] texts;
    private FirebaseFirestore db;
    private int sum = 0;
    private int[] money;
    private LinearLayout layout_input_money;
    private Button btn_calculate;
    private String meetingName;
    private boolean check_group=false;
    private RelativeLayout layout_account;
    FragmentManager fragmentManager;

    public static FragAccount newInstance() {
        return new FragAccount();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frag_account,container,false);
        db = FirebaseFirestore.getInstance();

        //layout_account = (RelativeLayout)view.findViewById(R.id.layout_account);
        layout_input_money = (LinearLayout)view.findViewById(R.id.layout_input_money);
        btn_calculate = view.findViewById(R.id.btn_calculate);

        if(view!=null){
            ViewGroup parentvg = (ViewGroup)view.getParent();
            if(null!=parentvg){
                parentvg.removeView(view);
            }
        }

        Bundle bundle = this.getArguments();

        if (bundle != null) {
            bundle = getArguments();
            meetingName = bundle.getString("Name");
        }

        db.collection("meetings").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if (document.get("name").toString().equals(meetingName)) {
                                    System.out.println("여기 들어옴");
                                    setLayoutOfAccount(document.getData().get("userID"));
                                    break;
                                }
                            }
                        }
                    }
                });


        return view;
    }

    public void setLayoutOfAccount(Object userId){

        //final String[] user_name;

        //LinearLayout 정의
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);
        //db에서 모임원들 이름 가져오기
        db.collection("users").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            int i = 0;
                            String[] user_name = new String[task.getResult().size()];
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if (userId.toString().contains(document.getId())) {
                                    user_name[i] = document.get("name").toString();
                                    Log.d("Document Read", document.getId() + " => " + document.getData());
                                    i++;
                                }
                            }
                            int user_num = i;

                            System.out.println("멤버 수 : " + user_num);
                            texts = new EditText[user_num];
                            for (int j = 0; j < user_num; j++) {
                                //LinearLayout 생성
                                LinearLayout ly = new LinearLayout(getContext());
                                ly.setLayoutParams(params);
                                ly.setOrientation(LinearLayout.HORIZONTAL);

                                //ImageView 생성
                                LinearLayout ivly = new LinearLayout(getContext());
                                ivly.setOrientation(LinearLayout.VERTICAL);
                                ivly.setLayoutParams(params);

                                ImageView iv = new ImageView(getContext());
                                iv.setImageResource(R.drawable.user);
                                LinearLayout.LayoutParams ivlp = new LinearLayout.LayoutParams(120, 100);
                                ivlp.gravity = Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL;
                                iv.setLayoutParams(ivlp);
                                ivly.addView(iv);

                                TextView tiv = new TextView(getContext());
                                tiv.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
                                tiv.setText(user_name[j]);
                                ivly.addView(tiv);

                                ly.addView(ivly);


                                //EditText 생성
                                EditText et = new EditText(getContext());
                                LinearLayout.LayoutParams etlp = new LinearLayout.LayoutParams(300, 120);
                                etlp.setMargins(30, 30, 30, 30);
                                et.setLayoutParams(etlp);
                                //엔터키 막기(엔터 누르면 입력한 값이 사라지는 것을 막기 위함)
                                et.setOnKeyListener(new View.OnKeyListener() {

                                    @Override
                                    public boolean onKey(View view, int KeyCode, KeyEvent keyEvent) {
                                        if (KeyCode == keyEvent.KEYCODE_ENTER)
                                            return true;
                                        return false;
                                    }
                                });

                                if(et.getText()==null||et.getText().toString().length()==0){
                                    et.setText("0");
                                }

                                //EditText저장
                                texts[j] = et;
                                ly.addView(et);

                                //TextView 생성
                                TextView tv = new TextView(getContext());
                                tv.setText("원");
                                LinearLayout.LayoutParams tvlp = params;
                                tv.setLayoutParams(tvlp);
                                ly.addView(tv);
                                ly.setPadding(0, 5, 0, 0);
                                layout_input_money.addView(ly);
                            }

                            //버튼 클릭이벤트 : 정산결과로 넘어감
                            btn_calculate.setOnClickListener(new Button.OnClickListener() {

                                @Override
                                public void onClick(View v) {
                                    money = new int[user_num];

                                    for (int i = 0; i < user_num; i++) {
                                        //EditText에 작성된 금액을 가져온다.
                                        if(texts[i].getText()==null||texts[i].getText().toString().length()==0){
                                            texts[i].setText("0");
                                            money[i] = Integer.parseInt(texts[i].getText().toString());
                                        } else{
                                            money[i] = Integer.parseInt(texts[i].getText().toString());
                                        }

                                        sum += money[i];
                                    }

                                    if(sum==0){
                                        final Snackbar snackbar = Snackbar.make(layout_account, "금액을 입력해주세요.^^", Snackbar.LENGTH_INDEFINITE);
                                        snackbar.setAction("확인", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                snackbar.dismiss();
                                            }
                                        });
                                        snackbar.show();
                                    }else {

                                        FragAccount_Result fr = new FragAccount_Result();
                                        //FragmentManager fm = getFragmentManager();
                                        //FragmentTransaction fmt = fm.beginTransaction();
                                        Bundle bundle = new Bundle();
                                        bundle.putIntArray("pay",money);
                                        bundle.putInt("total",sum);
                                        bundle.putStringArray("user_name",user_name);
                                        bundle.putInt("user_num",user_num);
                                        fr.setArguments(bundle);

                                        //((MeetingActivity)getActivity()).replaceFragment(fr,true);
                                        ((MeetingActivity)getActivity()).replaceFragment(fr,true);


                                    }
                                }
                            });
                        } else {
                            Log.d("Document Read", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }
}
