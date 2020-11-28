package com.example.mmmmeeting.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.mmmmeeting.Info.AddressItems;
import com.example.mmmmeeting.R;
import com.example.mmmmeeting.activity.MemberInitActivity;

import java.util.ArrayList;

public class AddressAdapter extends BaseAdapter {
    ArrayList<AddressItems> items = new ArrayList<AddressItems>();
    Context context; // activity 정보 저장

    // 아이템 추가
    public void addItem(AddressItems item) {
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
        AddressItems listItem = items.get(position); //position 해당하는 listItem

        //list_item inflate => convertView 참조
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.sub_address, parent, false);
        }

        // 텍스트뷰 설정
        TextView road = convertView.findViewById(R.id.addr_road);
        TextView jibun = convertView.findViewById(R.id.addr_jibun);
        TextView post = convertView.findViewById(R.id.postno);

        // 텍스트뷰에 글자 지정
        road.setText(listItem.getRoad());
        jibun.setText(listItem.getJibun());
        post.setText(listItem.getPost());


        LinearLayout addressItem = (LinearLayout) convertView.findViewById(R.id.addressItem);
        addressItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // MemberInitActivity 주소 정보 전달
                Intent intent = new Intent(context, MemberInitActivity.class);
                intent.putExtra("road", listItem.getRoad());
//                intent.putExtra("post", listItem.getJibun());
//                intent.putExtra("jibun", listItem.getPost());
                context.startActivity(intent);
                ((Activity)context).finish();
            }
        });

        return convertView;
    }

}
