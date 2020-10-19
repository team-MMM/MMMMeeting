package com.example.mmmmeeting.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.mmmmeeting.R;
import com.example.mmmmeeting.Info.GridItems;
import com.example.mmmmeeting.activity.MeetingActivity;

import java.util.ArrayList;

public class GridListAdapter extends BaseAdapter {
    ArrayList<GridItems> items = new ArrayList<GridItems>();
    Context context; // activity 정보 저장

    // 아이템 추가
    public  void addItem(GridItems item) {
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
        GridItems listItem = items.get(position); //position 해당하는 listItem

        //list_item inflate => convertView 참조
        if(convertView==null){
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.my_meeting_grid_item,parent,false);
        }

        // 텍스트뷰 설정
        TextView nameText = convertView.findViewById(R.id.roomName);
        TextView descriptText = convertView.findViewById(R.id.roomDescription);

        // 텍스트뷰에 글자 지정
        descriptText.setText(listItem.getDescription());
        nameText.setText(listItem.getName());

        // 이름 텍스트뷰 클릭시 동작 -> 다음 액티비티 넘어가게
        nameText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Toast.makeText(context, "동작 확인 : "+listItem.getName(), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(context, MeetingActivity.class);
                intent.putExtra("Name",listItem.getName());
                Log.d("getName",listItem.getName());
                intent.putExtra("Description",listItem.getDescription());
                context.startActivity(intent);
            }
        });

        return convertView;
    }

}