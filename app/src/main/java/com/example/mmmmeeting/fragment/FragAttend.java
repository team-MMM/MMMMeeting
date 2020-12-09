package com.example.mmmmeeting.fragment;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.mmmmeeting.Info.CalUserItems;
import com.example.mmmmeeting.R;
import com.example.mmmmeeting.adapter.CalUserResultAdapter;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FragAttend extends Fragment {
    private View view;
    private FirebaseFirestore db;
    private String meetingCode;
    private List<Map.Entry<String, Double>> list_entries;
    private HashMap<String, Double> attendMap = new HashMap<>();
    private ArrayList<String> userList = new ArrayList<>();
    private HashMap<String, Integer> userRank = new HashMap<>();

    ListView listView;
    CalUserResultAdapter adapter;

    LinearLayout attend_show;
    TextView best_title, resetDatetv, nullText;
    Button reset;

    int resetDate, resetTime;

    SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
    SimpleDateFormat time = new SimpleDateFormat("HHmm");

    String Tag = "attend test";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frag_attend, container, false);
        //attend_show = view.findViewById(R.id.attend_show);
        best_title = view.findViewById(R.id.best_title);
        resetDatetv = view.findViewById(R.id.resetDatetv);
        nullText = view.findViewById(R.id.nullText);
        listView = view.findViewById(R.id.attend_show);
        adapter = new CalUserResultAdapter();


        db = FirebaseFirestore.getInstance();
        reset = view.findViewById(R.id.resetBtn);
        String today = sdf.format(new Date());
        String thisTime = time.format(new Date());

        Bundle bundle = this.getArguments();

        if (bundle != null) {
            bundle = getArguments();
            meetingCode = bundle.getString("Code");
        }


        DocumentReference docRef = db.collection("meetings").document(meetingCode);

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        // 해당 문서가 존재하는 경우
                        Log.d(Tag, "meeting find : " + document.get("name"));
                        String getDate = document.get("resetDate").toString();
                        String getTime = document.get("resetTime").toString();
                        userList = (ArrayList<String>) document.get("userID");
                        System.out.println("userList: "+userList);

                        resetDate = Integer.valueOf(getDate);
                        resetTime = Integer.valueOf(getTime);

                        String year = getDate.substring(0, 4);
                        String month = getDate.substring(4, 6);
                        String date = getDate.substring(6, 8);
                        resetDatetv.setText(year + "년 " + month + "월 " + date + "일 ~ ");

                        Log.d(Tag, "get reset Date : " + resetDate);

                        FirebaseAuth mAuth = FirebaseAuth.getInstance();

                        if (document.get("leader").toString().equals(mAuth.getUid())) {
                            reset.setVisibility(View.VISIBLE);
                        }

                        setResetBtn(today, thisTime);
                        setAttend(meetingCode);
                        return;
                    } else {
                        // 존재하지 않는 문서
                        Log.d("Attend", "No Document");
                    }
                } else {
                    Log.d("Attend", "Task Fail : " + task.getException());
                }
            }

        });

        return view;
    }

    private void setResetBtn(String today, String thisTime) {
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(view.getContext());
                //제목
                alertDialogBuilder.setTitle("출석점수 초기화");

                //AlertDialog 세팅
                SpannableString s = new SpannableString("출석점수를 초기화하시겠습니까?\n" +
                        "초기화시 지금까지의 점수가 사라지고 되돌릴 수 없습니다.");
                s.setSpan(new RelativeSizeSpan(0.5f), 22, 22, 0);
                s.setSpan(new ForegroundColorSpan(Color.parseColor("#62ABD9")), 22, 22, 0);
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
                        db.collection("meetings").document(meetingCode).update("resetTime", thisTime);
                        db.collection("meetings").document(meetingCode).update("best", new HashMap<String, Integer>());

                        adapter = new CalUserResultAdapter();
                        listView.setAdapter(adapter);

                        Toast.makeText(view.getContext(), "출석점수를 초기화했습니다.", Toast.LENGTH_SHORT).show();

                        String year = today.substring(0, 4);
                        String month = today.substring(4, 6);
                        String date = today.substring(6, 8);

                        resetDatetv.setText(year + "년 " + month + "월 " + date + "일 ~ ");
                        nullText.setVisibility(view.VISIBLE);

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
                            attendMap.clear();

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                boolean meetingIDCheck = getId.equals(document.get("meetingID").toString());

                                Date meetingDate = document.getDate("meetingDate");
                                // 날짜 지난 것만 지각 포인트 체크 -> 리셋 날 기준으로 날짜 확인
                                boolean meetingTimeCheck = checkTime(meetingDate);

//                                Date now = new Date();// 날짜 지난 것만 지각 포인트 체크
//                                Date meetingDate = document.getDate("meetingDate");
//                                boolean meetingMonthCheck = now.compareTo(meetingDate) == 1 ? true : false;

                                Log.d(Tag, "check val : " + meetingIDCheck + "/" + meetingTimeCheck);
                                if (meetingIDCheck && meetingTimeCheck) {
                                    HashMap<String, Double> attender = (HashMap<String, Double>) document.get("timePoint");

                                    countAttend(attender, attendMap);
                                }
                            }
                            
                            mapSort(attendMap);

                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    if(attendMap.isEmpty()){
                                        nullText.setVisibility(view.VISIBLE);
                                    }
                                }
                            }, 300);
                        }
                    }
                });
    }

    // 날짜가 지났는지 확인
    private boolean checkTime(Date date) {
        int scheduleDate = 0;
        int scheduleTime = 0;
        if (date != null) {
            scheduleDate = Integer.parseInt(sdf.format(date));
            scheduleTime = Integer.parseInt(time.format(date));
        }

        if (scheduleDate == resetDate && scheduleTime >= resetTime) {
            Log.d(Tag, "scheduleTime : " + scheduleTime + "/ resetTime " + resetTime);
            return true;
        } else if (scheduleDate > resetDate) {
            Log.d(Tag, "scheduleDate : " + scheduleDate + "/ resetDate" + resetDate);
            return true;
        }

        return false;
    }

    // 누가 출석 했는지 확인, 이름과 점수로 array 생성, 출석 안 한 사람은 -12점
    private void countAttend(HashMap<String, Double> attender, HashMap<String, Double> attendMap) {
        if (attender == null) {
            return;
        }

        Set attenderSet = attender.keySet();

        HashMap<String, Double> tempMap = new HashMap<>();

        // 출첵 점수 저장
        for(int i=0;i<userList.size();i++) {
            String userName = userList.get(i);

            Iterator iter = attenderSet.iterator();
            while (iter.hasNext()) {
                String checkUser = (String) iter.next();
                if (userName.equals(checkUser)) {
                    Double point = Double.parseDouble(String.valueOf(attender.get(checkUser)));
                    tempMap.put(userName, point);
                    break;
                } else {
                    tempMap.put(userName, -12.0);
                }
            }
        }
        System.out.println(tempMap);

        // 기존 점수와 합침
        Set tempSet = tempMap.keySet();
        Iterator iter = tempSet.iterator();

        while (iter.hasNext()) {
            String user = (String) iter.next();
            Double now = tempMap.get(user).doubleValue();
            if(attendMap.get(user)!=null) {
                Double old = attendMap.get(user).doubleValue();
                now += old;
            }

            attendMap.put(user,now);
        }



        System.out.println("attendMap:" + attendMap);


    }

    private void mapSort(HashMap<String, Double> attendMap) {

        Log.d(Tag, "attender map is " + attendMap.toString());

        if (attendMap.isEmpty()) {
            return;
        }

        list_entries = new ArrayList<>(attendMap.entrySet());
        // 비교함수 Comparator를 사용하여 내림 차순으로 정렬
        Collections.sort(list_entries, new Comparator<Map.Entry<String, Double>>() {
            @Override
            public int compare(Map.Entry<String, Double> obj1, Map.Entry<String, Double> obj2) {
                // 오름 차순으로 정렬
                return obj1.getValue().compareTo(obj2.getValue());
            }
        });

        Log.d(Tag, "list_entries is " + list_entries.toString());

        int same = 1;
        int rank = 1;

        int size = list_entries.size() < 5 ? list_entries.size() : 5;

        for (int i = 0; i < size; i++) {
            if (i != 0) {
                if (list_entries.get(i - 1).getValue().equals(list_entries.get(i).getValue())) {
                    same++;
                } else {
                    rank += same;
                    same = 1;
                }
            }

            setLayout(rank, list_entries.get(i).getKey());
        }

        db.collection("meetings").document(meetingCode).update("best", attendMap);
    }



    public void setLayout(int num, String user_id) {
        Log.d(Tag, "Show layout");
        //db에서 모임원들 이름 가져오기
        db.collection("users").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            nullText.setVisibility(view.GONE);
                            SpannableString s;

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if (user_id.equals(document.getId())) {

                                    String user_name = document.get("name").toString();


                                    s = new SpannableString(num+"등 ("+attendMap.get(user_id).doubleValue()+"점)");
                                    s.setSpan(new RelativeSizeSpan(1.2f),0,s.length()-8,0);
                                    s.setSpan(new ForegroundColorSpan(Color.BLACK),0,s.length()-8,0);
                                    CalUserItems item = new CalUserItems(user_name,user_id);
                                    item.setMoney(s.toString());
                                    adapter.addItem(item);

                                }
                            }
                            listView.setAdapter(adapter);

                        }
                    }
                });
    }
}

