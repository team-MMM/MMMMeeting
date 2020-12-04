package com.example.mmmmeeting.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.mmmmeeting.Info.ChatItem;
import com.example.mmmmeeting.Info.MemberInfo;
import com.example.mmmmeeting.R;
import com.example.mmmmeeting.adapter.ChatAdapter;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class FragChat extends Fragment {
    FirebaseUser user;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference chatRef;
    private MemberInfo memberInfo;
    private String userName;

    private EditText et;
    private ListView listView;

    private ArrayList<ChatItem> messageItems=new ArrayList<>();
    private ChatAdapter adapter;
    private String meetingCode;
    private SharedPreferences sp;
    private String profilePath;

    public FragChat(){}


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.frag_chat, container, false);
        user = FirebaseAuth.getInstance().getCurrentUser();

        sp = getActivity().getSharedPreferences("sp", getActivity().MODE_PRIVATE);
        profilePath = sp.getString("profilePath","");

        et=view.findViewById(R.id.et);
        listView=view.findViewById(R.id.listview);
        listView.setVisibility(listView.INVISIBLE);

        Bundle bundle = this.getArguments();
        if(bundle != null) {
            bundle = getArguments();
            meetingCode = bundle.getString("Code");
            userName = bundle.getString("userName");
        }

        // meetingName 기준으로 분리함
        firebaseDatabase= FirebaseDatabase.getInstance();
        chatRef= (DatabaseReference) firebaseDatabase.getReference("chat").child(meetingCode);
        Query chatQuery = chatRef.orderByChild("timestamp");


        //RealtimeDB에서 채팅 메세지들 실시간 읽어오기..
        //'chat'노드에 저장되어 있는 데이터들을 읽어오기
        //chatRef에 데이터가 변경되는 것을 듣는 리스너 추가
        chatQuery.addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

                //새로 추가된 데이터(값 : MessageItem객체) 가져오기
                ChatItem messageItem= dataSnapshot.getValue(ChatItem.class);

                Calendar cal = Calendar.getInstance();
                Calendar temp = Calendar.getInstance();

                if(messageItems.size() == 0){
                    cal.setTimeInMillis(messageItem.getTimestamp());
                    int month = cal.get(Calendar.MONTH);
                    int day = cal.get(Calendar.DAY_OF_MONTH);

                    ChatItem date = new ChatItem();
                    int nyear = cal.get(Calendar.YEAR);
                    date.setId("date");
                    date.setTime(nyear + "년 " + (month + 1) + "월 " + day + "일");
                    messageItems.add(date);

                    //새로운 메세지를 리스뷰에 추가하기 위해 ArrayList에 추가
                    messageItems.add(messageItem);

                }else{
                    System.out.println(messageItems);
                    if(messageItems.get(messageItems.size() - 1).getTimestamp() == null){
                        //새로운 메세지를 리스뷰에 추가하기 위해 ArrayList에 추가
                        messageItems.add(messageItem);
                        adapter.notifyDataSetChanged();
                        listView.setSelection(messageItems.size()-1);

                    }else {

                        cal.setTimeInMillis(messageItems.get(messageItems.size() - 1).getTimestamp());
                        int year = cal.get(Calendar.YEAR);
                        int month = cal.get(Calendar.MONTH);
                        int day = cal.get(Calendar.DAY_OF_MONTH);

                        temp.setTimeInMillis(messageItem.getTimestamp());
                        int nyear = temp.get(Calendar.YEAR);
                        int nmonth = temp.get(Calendar.MONTH);
                        int nday = temp.get(Calendar.DAY_OF_MONTH);
                        System.out.println("date: " + month + " " + day);
                        System.out.println("now: " + nmonth + " " + nday);

                        // 마지막 메세지보다 날짜가 지난 경우
                        if ((year < nyear) || (month < nmonth) || (month == nmonth && day < nday)) {
                            ChatItem date = new ChatItem();
                            date.setId("date");
                            date.setTime(nyear + "년 " + (nmonth + 1) + "월 " + nday + "일");
                            messageItems.add(date);
                        }
                        //새로운 메세지를 리스뷰에 추가하기 위해 ArrayList에 추가
                        messageItems.add(messageItem);
                        adapter.notifyDataSetChanged();
                        listView.setSelection(messageItems.size() - 1);
                    }
                }

            }

            @Override
            public void onChildChanged(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onChildRemoved(@NonNull DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(@NonNull DataSnapshot dataSnapshot, @Nullable String s) {

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        adapter=new ChatAdapter(messageItems,getLayoutInflater());
        listView.setAdapter(adapter);
        listView.setVisibility(listView.VISIBLE);

        Button button = (Button) view.findViewById(R.id.msgBtn);
        button.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                clickSend(v);
            }
        });

        return view;
    }


    public void clickSend(View view) {
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm");

        String name= userName;
        String message= et.getText().toString();

        //메세지 작성 시간 문자열로
        Calendar calendar= Calendar.getInstance();
        String time = timeFormat.format(calendar.getTime());
        Date now = new Date();
        Long timestamp = now.getTime();

        //DB에 저장할 값들(닉네임, 메세지, 시간)
        ChatItem messageItem= new ChatItem(user.getUid(), name, message, time, timestamp, profilePath);
        chatRef.push().setValue(messageItem);

        //EditText에 있는 글씨 지우기
        et.setText("");

        //소프트키패드 안보이도록
        InputMethodManager imm=(InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(),0);

    }

}
