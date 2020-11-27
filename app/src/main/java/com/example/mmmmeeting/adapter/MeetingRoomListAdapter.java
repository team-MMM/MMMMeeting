package com.example.mmmmeeting.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.mmmmeeting.R;
import com.example.mmmmeeting.Info.MeetingRoomItems;
import com.example.mmmmeeting.activity.MeetingActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;

import androidx.annotation.NonNull;

public class MeetingRoomListAdapter extends BaseAdapter {
    ArrayList<MeetingRoomItems> items = new ArrayList<MeetingRoomItems>();
    Context context; // activity 정보 저장
    String code;

    // 아이템 추가
    public  void addItem(MeetingRoomItems item) {
        items.add(item);
    }

    // 아이템 크기
    @Override
    public int getCount() {
        return items.size();
    }

    // 위치의 아이템
    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    // 위치 (아이템ID)
    @Override
    public long getItemId(int position) {
        return position;
    }


    // 뷰 설정
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        context = parent.getContext(); // activity 정보 읽기
        MeetingRoomItems listItem = items.get(position); //position 해당하는 listItem

        //list_item inflate => convertView 참조
        if(convertView==null){
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.meeting_room_item,parent,false);
        }

        // 텍스트뷰 설정
        TextView nameText = convertView.findViewById(R.id.roomName);
        TextView descriptText = convertView.findViewById(R.id.roomDescription);

        // 텍스트뷰에 글자 지정
        descriptText.setText(listItem.getDescription());
        nameText.setText(listItem.getName());

        RelativeLayout gridItem = (RelativeLayout) convertView.findViewById(R.id.meetingRoomItem);

        // 다음 액티비티 넘어가게
        gridItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(context, "동작 확인 : " + listItem.getRoad(), Toast.LENGTH_SHORT).show();
                // MemberInitActivity 주소 정보 전달
                Intent intent = new Intent(context, MeetingActivity.class);
                intent.putExtra("Name",listItem.getName());
                intent.putExtra("Description",listItem.getDescription());

                FirebaseFirestore db = FirebaseFirestore.getInstance();

                db.collection("meetings").get()
                        .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                if (task.isSuccessful()) {
                                    //모든 document 출력 (dou id + data arr { : , ... ,  })
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        // 모임 이름이 같은 경우 해당 모임의 코드 출력
                                        if (document.get("name").toString().equals(listItem.getName()) && document.get("description").toString().equals(listItem.getDescription())) {
                                            intent.putExtra("Code",document.getId());
                                            Log.d("Grid send", document.getId() );
                                            context.startActivity(intent);
                                            return;
                                        } else {
                                            Log.d("Document Snapshot", "No Document");
                                        }
                                    }
                                } else {
                                    Log.d("Document Read", "Error getting documents: ", task.getException());
                                }
                            }
                        });
            }
        });

        return convertView;
    }

}