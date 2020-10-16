package com.example.mmmmeeting.adapter;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.mmmmeeting.R;
import com.example.mmmmeeting.Info.GridItems;
import com.example.mmmmeeting.activity.MeetingActivity;

import java.util.ArrayList;

import static com.firebase.ui.auth.AuthUI.getApplicationContext;

public class GridListAdapter extends BaseAdapter {
    ArrayList<GridItems> items = new ArrayList<GridItems>();
    Context context; // app정보 저장

    public  void addItem(GridItems item) {
        items.add(item);

    }
    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        context = parent.getContext(); // activity 정보 읽기
        GridItems listItem = items.get(position); //position 해당하는 listItem

        //list_item inflate => convertView 참조
        if(convertView==null){
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.my_meeting_grid_item,parent,false);
        }

        TextView nameText = convertView.findViewById(R.id.roomName);
        TextView descriptText = convertView.findViewById(R.id.roomDescription);

        descriptText.setText(listItem.getDescription());
        nameText.setText(listItem.getName());

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