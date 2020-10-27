package com.example.mmmmeeting.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.mmmmeeting.Info.ChatItem;
import com.example.mmmmeeting.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatAdapter extends BaseAdapter {

    ArrayList<ChatItem> chatItems;
    LayoutInflater layoutInflater;
    final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

    public ChatAdapter(ArrayList<ChatItem> chatItems, LayoutInflater layoutInflater) {
        this.chatItems = chatItems;
        this.layoutInflater = layoutInflater;
    }

    @Override
    public int getCount() {
        return chatItems.size();
    }

    @Override
    public Object getItem(int position) {
        return chatItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup viewGroup) {

        ChatItem item=chatItems.get(position);
        View itemView=null;

        //메세지가 내 메세지인지
        if(item.getName().equals(user.getDisplayName())){
            itemView= layoutInflater.inflate(R.layout.list_mychatbox,viewGroup,false);
        }else{
            itemView= layoutInflater.inflate(R.layout.list_otherchatbox,viewGroup,false);
        }

        //만들어진 itemView에 값들 설정
        CircleImageView iv= itemView.findViewById(R.id.iv);
        TextView tvName= itemView.findViewById(R.id.tv_name);
        TextView tvMsg= itemView.findViewById(R.id.tv_msg);
        TextView tvTime= itemView.findViewById(R.id.tv_time);

        tvName.setText(item.getName());
        tvMsg.setText(item.getMessage());
        tvTime.setText(item.getTime());

        // 유저 프로필 이미지가 있으면 이미지 띄우게 할까 했는데 일단 보류..
        //Glide.with(itemView).load(item.getPofileUrl()).into(iv);

        return itemView;
    }
}