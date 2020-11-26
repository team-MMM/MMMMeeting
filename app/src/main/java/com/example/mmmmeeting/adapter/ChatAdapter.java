package com.example.mmmmeeting.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.example.mmmmeeting.Info.ChatItem;
import com.example.mmmmeeting.R;
import com.example.mmmmeeting.view.ReadContentsView;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatAdapter extends BaseAdapter {

    ArrayList<ChatItem> chatItems;
    LayoutInflater layoutInflater;
    final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String profilePath;

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

        // 날짜만 받아왔을 때
        if(item.getId().equals("date")){
            itemView= layoutInflater.inflate(R.layout.list_date,viewGroup,false);
            TextView date = itemView.findViewById(R.id.date);
            System.out.println("date is: "+item.getTime());
            date.setText(item.getTime());
            itemView.setVisibility(itemView.VISIBLE);
            return itemView;
        }


        //메세지가 내 메세지인지 UID로 확인
        if(item.getId().equals(user.getUid())){
            itemView= layoutInflater.inflate(R.layout.list_mychatbox,viewGroup,false);
            itemView.setVisibility(itemView.INVISIBLE);
            //만들어진 itemView에 값들 설정
            TextView tvName= itemView.findViewById(R.id.tv_name);
            TextView tvMsg= itemView.findViewById(R.id.tv_msg);
            TextView tvTime= itemView.findViewById(R.id.tv_time);

            tvName.setText(item.getName());
            tvMsg.setText(item.getMessage());
            tvTime.setText(item.getTime());
            itemView.setVisibility(itemView.VISIBLE);
        }else{
            itemView= layoutInflater.inflate(R.layout.list_otherchatbox,viewGroup,false);

            View finalItemView = itemView;
            itemView.setVisibility(itemView.INVISIBLE);

            db.collection("users").document(item.getId()).
                    get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            CircleImageView iv= finalItemView.findViewById(R.id.iv);
                            TextView tvName= finalItemView.findViewById(R.id.tv_name);
                            TextView tvMsg= finalItemView.findViewById(R.id.tv_msg);
                            TextView tvTime= finalItemView.findViewById(R.id.tv_time);

                            tvName.setText(item.getName());
                            tvMsg.setText(item.getMessage());
                            tvTime.setText(item.getTime());
                            if(document.get("profilePath") != null){
                                profilePath = document.get("profilePath").toString();
                                Glide.with(finalItemView).load(profilePath).centerCrop().override(500).into(iv);
                            }
                            finalItemView.setVisibility(finalItemView.VISIBLE);

                        }
                    }
                }
            });;


        }

        return itemView;
    }
}