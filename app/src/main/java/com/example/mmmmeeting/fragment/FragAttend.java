package com.example.mmmmeeting.fragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.mmmmeeting.R;
import com.example.mmmmeeting.activity.MiddlePlaceActivity;
import com.example.mmmmeeting.activity.PlaceListActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FragAttend extends Fragment {
    private View view;
    private FirebaseFirestore db;
    private String meetingName;
    private List<Map.Entry<String, Integer>> list_entries;

    String meetingCode;
    LinearLayout attend_show;
    TextView best_title,resetDatetv;
    Button reset;

    int resetDate;
    boolean isLeader=false;

    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

    String Tag = "attend test";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frag_attend, container, false);
        attend_show = view.findViewById(R.id.attend_show);
        best_title = view.findViewById(R.id.best_title);
        resetDatetv = view.findViewById(R.id.resetDatetv);
        db = FirebaseFirestore.getInstance();
        reset = view.findViewById(R.id.resetBtn);
        String today = sdf.format(new Date());

        best_title.setText("출석 Rank");

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
                                    Log.d(Tag, "meeting find : " + document.get("name"));
                                    meetingCode = document.getId();
                                    String getDate = document.get("resetDate").toString();
                                    resetDate = Integer.valueOf(getDate);

                                    String year = getDate.substring(0,4);
                                    String month = getDate.substring(4,6);
                                    String date = getDate.substring(6,8);
                                    resetDatetv.setText(year+"년 "+month + "월 " +date + "일 ~ ");

                                    Log.d(Tag,"get reset Date : " + resetDate);

                                    FirebaseAuth mAuth = FirebaseAuth.getInstance();
                                    if (document.get("leader").toString().equals(mAuth.getUid())){
                                        isLeader=true;
                                    }

                                    setResetBtn(today);
                                    setAttend(meetingName);
                                    return;
                                }
                            }
                        }
                    }
                });

        return view;
    }

    private void setResetBtn(String today) {
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(view.getContext());
                //제목
                alertDialogBuilder.setTitle("출석점수 초기화");

                //AlertDialog 세팅
                SpannableString s = new SpannableString("출석점수를 초기화하시겠습니까?\n" +
                                                "초기화시 지금까지의 점수가 사라지고 되돌릴 수 없습니다.");
                s.setSpan(new RelativeSizeSpan(0.5f),22,22,0);
                s.setSpan(new ForegroundColorSpan(Color.parseColor("#62ABD9")),22,22,0);
                alertDialogBuilder.setMessage(s)
                        .setCancelable(false)
                        .setPositiveButton("아니오", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                            }
                        }).setNegativeButton("네", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        db.collection("meetings").document(meetingCode).update("resetDate", today);
                        db.collection("meetings").document(meetingCode).update("best", new HashMap<String, Integer>());

                        attend_show.removeAllViews();
                        reset.setVisibility(View.INVISIBLE);

                        Toast.makeText(view.getContext(), "출석점수를 초기화했습니다.", Toast.LENGTH_SHORT).show();

                        String year = today.substring(0,4);
                        String month = today.substring(4,6);
                        String date = today.substring(6,8);

                        resetDatetv.setText(year+"년 "+month + "월 " +date + "일 ~ ");

                        TextView nulltxt = new TextView(getContext());
                        nulltxt.setText("출석 기록이 없습니다.");
                        nulltxt.setTextSize(30);
                        nulltxt.setTextColor(Color.BLACK);
                        attend_show.addView(nulltxt);
                    }
                });

                //다이얼로그 생성
                AlertDialog alertDialog = alertDialogBuilder.create();

                //다이얼로그 보여주기
                alertDialog.show();
            }
        });
    }

    private void setAttend(String getId) {

        db.collection("schedule").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            HashMap<String, Integer> attendMap = new HashMap<>();
                            attendMap.clear();

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                boolean meetingIDCheck = getId.equals(document.get("meetingID").toString());
                                boolean meetingMonthCheck = getMonth(document.getDate("meetingDate"));
                                Log.d(Tag, "check val : " + meetingIDCheck + "/" +meetingMonthCheck);
                                if (meetingIDCheck && meetingMonthCheck) {
                                    countAttend((ArrayList<String>) document.get("lateComer"), attendMap);
                                }
                            }

                            mapSort(attendMap);
                        }
                    }
                });
    }

    private boolean getMonth(Date month) {
        int scheduleDate=0;
        if (month != null) {
            scheduleDate = Integer.parseInt(sdf.format(month));
        }

        if (scheduleDate > resetDate) {
            Log.d(Tag, "scheduleDate : " + scheduleDate + "/ resetDate" +resetDate);

            return true;
        }

        return false;
    }

    private void mapSort(HashMap<String, Integer> attendMap) {

        Log.d(Tag, "attender map is " + attendMap.toString());

        if (attendMap.isEmpty()) {
            TextView nulltxt = new TextView(getContext());
            nulltxt.setText("출석 기록이 없습니다.");
            nulltxt.setTextSize(30);
            nulltxt.setTextColor(Color.BLACK);
            attend_show.addView(nulltxt);
            return;
        }

        list_entries = new ArrayList<>(attendMap.entrySet());
        // 비교함수 Comparator를 사용하여 내림 차순으로 정렬
        Collections.sort(list_entries, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> obj1, Map.Entry<String, Integer> obj2) {
                // 내림 차순으로 정렬
                return obj2.getValue().compareTo(obj1.getValue());
            }
        });

        Log.d(Tag, "list_entries is " + list_entries.toString());

        int i = 0;
        for (Map.Entry<String, Integer> entry : list_entries) {
            setLayout(i + 1, entry.getKey());
            i++;
        }
        db.collection("meetings").document(meetingCode).update("best", attendMap);
    }

    private void countAttend(ArrayList<String> attender, HashMap<String, Integer> attendMap) {
        if (attender == null) {
            return;
        }

        for (int i = 0; i < attender.size(); i++) {
            if (attendMap.containsKey(attender.get(i))) {
                attendMap.put(attender.get(i), attendMap.get(attender.get(i)) + 1);
            } else {
                attendMap.put(attender.get(i), 1);
            }
        }

        Log.d(Tag, "attender map is " + attendMap.toString());
    }

    public void setLayout(int num, String user_id) {
        //db에서 모임원들 이름 가져오기
        db.collection("users").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if (user_id.equals(document.getId())) {
                                    String user_name = document.get("name").toString();

                                    //LinearLayout 정의
                                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                    params.setMargins(15, 15, 15, 10);

                                    //LinearLayout 생성
                                    LinearLayout ly = new LinearLayout(getContext());
                                    ly.setLayoutParams(params);
                                    ly.setOrientation(LinearLayout.HORIZONTAL);


                                    TextView rank = new TextView(getContext());
                                    rank.setLayoutParams(params);
                                    rank.setText(num + "등");
                                    rank.setTextSize(25);
                                    rank.setTextColor(Color.BLACK);
                                    ly.addView(rank);

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
                                    tiv.setText(user_name);
                                    ivly.addView(tiv);

                                    ly.addView(ivly);

                                    attend_show.addView(ly);

                                }
                            }

                            if(isLeader){
                                reset.setVisibility(View.VISIBLE);
                            }
                        }
                    }
                });
    }
}

