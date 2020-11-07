package com.example.mmmmeeting.fragment;

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

import com.example.mmmmeeting.Info.ScheduleInfo;
import com.example.mmmmeeting.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Text;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FragAlarm extends Fragment {
    private View view;
    private FirebaseFirestore db;
    private String meetingName;

    private List<Map.Entry<String,Integer>> list_entries;
    private HashMap<String,Integer> latemap;
    private ArrayList<ArrayList> latelist;

    LinearLayout latecomer_show;
    TextView best_title;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frag_alarm,container,false);
        latecomer_show = (LinearLayout)view.findViewById(R.id.latecomer_show);
        best_title = (TextView)view.findViewById(R.id.best_title);
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

                                    latelist = new ArrayList<>();
                                    latemap = new HashMap<>();
                                    latemap.clear();

                                    System.out.println("미팅 이름 : "+meetingName);
                                    System.out.println("여기 들어옴");
                                    setLateComer(meetingName);
                                    return;
                                }
                            }
                        }
                    }
                });

        return view;
    }

    public void setLateComer(String getid){

        db.collection("schedule").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                System.out.println("latecomer id : "+document.getId());
                                System.out.println("lateComer내의 meetingID : "+document.get("meetingID").toString());
                                if(getid.equals(document.get("meetingID").toString())){
                                    //latelist.add(Arrays.asList(document.get("lateComer")).toArray());
                                    System.out.println("리스트에 지각자 저장");
                                    latelist.add((ArrayList)document.get("lateComer"));
                                    System.out.println("latelist출력 : "+latelist);
                                }
                            }
                            setList();
                        }
                    }
                });
    }

    public void setList(){

        if(latelist.size()==0){
            TextView nultxt = new TextView(getContext());
            nultxt.setText("아직 지각자 없음");
            nultxt.setTextSize(20);
            latecomer_show.addView(nultxt);
        }

        for(int i=0;i<latelist.size();i++){
            if(latelist.get(i)!=null){
                System.out.println("latelist 출력 : "+latelist.get(i));
                //String[] mlatelist = new String[latelist.get(i).size()];
                //String[] mlatelist = Arrays.asList(latelist.get(i)).toArray(new String[latelist.get(i).size()]);
                ArrayList<String> mlatelist = latelist.get(i);
                System.out.println("mlatelist 출력 : "+mlatelist);
                System.out.println("mlatelist의 사이즈 : "+mlatelist.size());

                //지각자 점수 : 약속참여자들의 수만큼 점수 할당
                int late_score = mlatelist.size();
                System.out.println("late score : "+late_score);

                //map에 참여자들의 id와 latescore를 같이 저장
                for(int j=0;j<mlatelist.size();j++){
                    String user=mlatelist.get(j);//HashMap의key
                    System.out.println("uid출력 : "+user);

                    if(latemap.get(user)==null){
                        latemap.put(user,late_score);
                    }else {
                        latemap.put(user, latemap.get(user) + late_score);
                        System.out.println("지각자 점수 출력 : " + latemap.get(user));
                    }
                    late_score--;
                }
            }else{
                break;
            }
        }
        System.out.println("latemap 출력 :"+latemap);

        list_entries = new ArrayList<Map.Entry<String, Integer>>(latemap.entrySet());
        // 비교함수 Comparator를 사용하여 내림 차순으로 정렬
        Collections.sort(list_entries, new Comparator<Map.Entry<String, Integer>>() {
            @Override
            public int compare(Map.Entry<String, Integer> obj1, Map.Entry<String, Integer> obj2)
            {
                // 내림 차순으로 정렬
                return obj2.getValue().compareTo(obj1.getValue());
            }
        });

        int i=0;
        for (Map.Entry<String, Integer> entry : list_entries) {
            best_title.setVisibility(view.VISIBLE);
            setLayout(i+1,entry.getKey(),entry.getValue());
            i++;

        }
    }

    public void setLayout(int num,String user_id,int score){

        //db에서 모임원들 이름 가져오기
        db.collection("users").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if (user_id.toString().contains(document.getId())) {
                                    String user_name = document.get("name").toString();

                                    System.out.println("지각자 출력");
                                    //LinearLayout 정의
                                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                                            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

                                    //LinearLayout 생성
                                    LinearLayout ly = new LinearLayout(getContext());
                                    ly.setLayoutParams(params);
                                    ly.setOrientation(LinearLayout.HORIZONTAL);


                                    TextView rank = new TextView(getContext());
                                    rank.setLayoutParams(params);
                                    rank.setText(num + "등");
                                    rank.setTextSize(20);
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

                                    TextView lateInfo = new TextView(getContext());
                                    lateInfo.setLayoutParams(params);
                                    lateInfo.setText("( 점수 : " + score + " )");
                                    ly.addView(lateInfo);
                                    ly.setPadding(0, 0, 0, 30);

                                    latecomer_show.addView(ly);

                                }
                            }
                        }
                    }
                });

    }

}

