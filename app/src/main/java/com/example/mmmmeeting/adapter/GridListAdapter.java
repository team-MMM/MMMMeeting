package com.example.mmmmeeting.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.example.mmmmeeting.R;
import com.example.mmmmeeting.Info.GridItems;

import java.util.ArrayList;

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
        GridItems listItem = items.get(position);

        if(convertView==null){
            LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.my_meeting_grid_item,parent,false);
        }

        Button nameText = convertView.findViewById(R.id.roomName);
        TextView descriptText = convertView.findViewById(R.id.roomDescription);

        descriptText.setText(listItem.getDescription());
        nameText.setText(listItem.getName());
        return convertView;
    }

}