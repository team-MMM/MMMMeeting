package com.example.mmmmeeting.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.example.mmmmeeting.Info.CalUserItems;
import com.example.mmmmeeting.R;
import com.example.mmmmeeting.activity.MeetingActivity;
import com.example.mmmmeeting.adapter.CalUserAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

public class FragAccount extends Fragment {
    private View view;

    private String[] texts;
    private FirebaseFirestore db;
    private int sum = 0;
    private int[] money;
    private Button btn_calculate;
    private String meetingCode;
    ArrayList<String> user_name = new ArrayList<>();
    ArrayList<String> user_id = new ArrayList<>();
    CalUserAdapter adapter;
    ListView listView;

    public static FragAccount newInstance() {
        return new FragAccount();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.frag_account,container,false);
        db = FirebaseFirestore.getInstance();

        btn_calculate = view.findViewById(R.id.btn_calculate);
        listView = view.findViewById(R.id.caluser);
        adapter = new CalUserAdapter();

        if(view!=null){
            ViewGroup parentvg = (ViewGroup)view.getParent();
            if(null!=parentvg){
                parentvg.removeView(view);
            }
        }

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
                        setLayoutOfAccount((ArrayList<String>) document.getData().get("userID"));
                        Log.d("Attend", "Data is : " + document.getId());
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

    public void setLayoutOfAccount(ArrayList<String> userId){
        System.out.println("userID"+userId);
        Log.d("Account debug", "get userId" + userId);

        //db에서 모임원들 이름 가져오기
        db.collection("users").get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {

                            for (QueryDocumentSnapshot document : task.getResult()) {
                                if(userId.toString().contains(document.getId())) {
                                    String name = document.get("name").toString();
                                    user_name.add(name);
                                    user_id.add(document.getId());
                                    Log.d("Account debug", "get name" + name);
                                    CalUserItems item = new CalUserItems(name, document.getId());
                                    Log.d("Account debug", "get item" + item);
                                    adapter.addItem(item);
                                }
                            }


                            int user_num = user_id.size();
                            listView.setAdapter(adapter);

                            //버튼 클릭이벤트 : 정산결과로 넘어감
                            btn_calculate.setOnClickListener(new Button.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    money = new int[user_num];

                                    for (int i = 0; i < user_num; i++) {
                                        //EditText에 작성된 금액을 가져온다.
                                        try {
                                            if(adapter.getItem(i).getMoney()==null||adapter.getItem(i).getMoney().length()==0){
                                                adapter.getItem(i).setMoney("0");
                                                money[i] = Integer.parseInt(adapter.getItem(i).getMoney());
                                            } else{
                                                money[i] = Integer.parseInt(adapter.getItem(i).getMoney());
                                            }
                                            sum += money[i];
                                        }catch(NumberFormatException e){
                                            final Snackbar snackbar = Snackbar.make(view, "숫자만 입력해주세요.", Snackbar.LENGTH_INDEFINITE);
                                            snackbar.setAction("확인", new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    snackbar.dismiss();
                                                }
                                            });
                                            snackbar.show();
                                            return;
                                        }

                                    }

                                    if(sum==0){
                                        final Snackbar snackbar = Snackbar.make(view, "금액을 입력해주세요!", Snackbar.LENGTH_INDEFINITE);
                                        snackbar.setAction("확인", new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                snackbar.dismiss();
                                            }
                                        });
                                        snackbar.show();
                                    }else {

                                        FragAccount_Result fr = new FragAccount_Result();

                                        Bundle bundle = new Bundle();
                                        bundle.putIntArray("pay",money);
                                        bundle.putInt("total",sum);
                                        bundle.putSerializable("user_name",user_name);
                                        bundle.putSerializable("user_id",user_id);
                                        bundle.putInt("user_num",user_num);
                                        fr.setArguments(bundle);

                                        ((MeetingActivity)getActivity()).replaceFragment(fr,true);

                                    }
                                }
                            });

                            btn_calculate.setVisibility(View.VISIBLE);
                        } else {
                            Log.d("Document Read", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }
}
