package com.example.mmmmeeting.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.mmmmeeting.R;
import com.example.mmmmeeting.activity.AccountResultActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class FragAccount extends Fragment {
    private View view;

    private EditText[] texts;
    private int sum = 0;
    private int[] money;
    private LinearLayout layout_input_money;
    private Button btn_calculate;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frag_account,container,false);

        layout_input_money = (LinearLayout)view.findViewById(R.id.layout_input_money);
        btn_calculate = view.findViewById(R.id.btn_calculate);

        //LinearLayout 정의
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,ViewGroup.LayoutParams.WRAP_CONTENT);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull com.google.android.gms.tasks.Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            int i = 0;
                            String[] user_name = new String[task.getResult().size()];
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // document에서 이름이 userID인 필드의 데이터 얻어옴
                                user_name[i++] = document.getData().get("name").toString();
                                document.getId(); // document 이름(id)
                                document.getData(); // document의 모든 데이터
                                System.out.println("멤버 "+i+"번째");
                            }

                            System.out.println("멤버 수 : "+user_name.length);
                            texts = new EditText[user_name.length];
                            for(int j=0;j<user_name.length;j++){
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
                                LinearLayout.LayoutParams ivlp = new LinearLayout.LayoutParams(100,100);
                                ivlp.gravity= Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL;
                                iv.setLayoutParams(ivlp);
                                ivly.addView(iv);

                                TextView tiv = new TextView(getContext());
                                tiv.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL);
                                tiv.setText(user_name[j]);
                                ivly.addView(tiv);

                                ly.addView(ivly);



                                //EditText 생성
                                EditText et = new EditText(getContext());
                                LinearLayout.LayoutParams etlp = new LinearLayout.LayoutParams(300,120);
                                etlp.setMargins(30,30,30,30);
                                et.setLayoutParams(etlp);
                                //et.setId(i);
                                //texts.add(i,et);
                                texts[j] = et;
                                ly.addView(et);

                                //TextView 생성
                                TextView tv = new TextView(getContext());
                                tv.setText("원");
                                LinearLayout.LayoutParams tvlp = params;
                                //tvlp.setMargins(30,30,30,30);
                                tv.setLayoutParams(tvlp);
                                ly.addView(tv);
                                ly.setPadding(0,5,0,0);
                                layout_input_money.addView(ly);

                                System.out.println("동적 뷰 생성 완료");
                            }

                            btn_calculate.setOnClickListener(new Button.OnClickListener(){

                                @Override
                                public void onClick(View v) {
                                    money = new int[5];

                                    for(int i=0;i<5;i++){
                                        money[i] = Integer.parseInt(texts[i].getText().toString());
                                        sum+=money[i];
                                    }

                                    Intent intent = new Intent(getContext(), AccountResultActivity.class);
                                    intent.putExtra("pay",money);
                                    intent.putExtra("total",sum);
                                    intent.putExtra("user_name",user_name);
                                    intent.putExtra("user_num",user_name.length);
                                    startActivity(intent);
                                }
                            });
                        }else{
                            Log.d("Document Read", "Error getting documents: ", task.getException());
                        }
                    }
                });

        return view;
    }
}
