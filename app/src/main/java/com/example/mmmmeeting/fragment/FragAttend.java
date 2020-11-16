package com.example.mmmmeeting.fragment;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mmmmeeting.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FragAttend extends Fragment {
    private View view;
    private FirebaseFirestore db;
    private String meetingName;
    private List<Map.Entry<String,Integer>> list_entries;

    LinearLayout attend_show;
    TextView best_title;

    String Tag = "attend test";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frag_attend,container,false);
        attend_show = view.findViewById(R.id.attend_show);
        best_title = view.findViewById(R.id.best_title);
        db = FirebaseFirestore.getInstance();

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
                                    setAttend(meetingName);
                                    return;
                                }
                            }
                        }
                    }
                });

        return view;
    }

    private void setAttend(String getId){

        db.collection("schedule").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            HashMap<String,Integer> attendMap = new HashMap<>();
                            attendMap.clear();
                            int count = 0;

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // 위에서 부터 5개 약속만 점수로
                                if(getId.equals(document.get("meetingID").toString())&&count++<5){
                                    countAttend((ArrayList<String>)document.get("lateComer"),attendMap);
                                }
                            }

                            mapSort(attendMap);
                        }
                    }
                });
    }

    private void mapSort(HashMap<String,Integer> attendMap) {

        Log.d(Tag, "attender map is " + attendMap.toString());

        if(attendMap.isEmpty()){
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

    }

    private void countAttend(ArrayList<String> attender, HashMap<String,Integer> attendMap) {
        if(attender==null){
            return;
        }

        for(int i=0; i<attender.size(); i++){
            if(attendMap.containsKey(attender.get(i))){
                attendMap.put(attender.get(i),attendMap.get(attender.get(i))+1);
            }
            else {
                attendMap.put(attender.get(i),1);
            }
        }

        Log.d(Tag, "attender map is " + attendMap.toString());
    }

    public void setLayout(int num,String user_id){

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
                                    params.setMargins(15,15,15,10);

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
                        }
                    }
                });

    }

}
