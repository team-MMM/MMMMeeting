package com.example.mmmmeeting.adapter;

import android.content.Context;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.example.mmmmeeting.Info.CalUserItems;
import com.example.mmmmeeting.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

public class CalUserResultAdapter extends BaseAdapter {
    ArrayList<CalUserItems> items = new ArrayList<CalUserItems>();
    Context context; // activity 정보 저장
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    String profilePath;

    // 아이템 추가
    public void addItem(CalUserItems item) {
        items.add(item);
    }

    // 아이템 크기
    @Override
    public int getCount() {
        return items.size();
    }

    // 위치의 아이템
    @Override
    public CalUserItems getItem(int position) {
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
        CalUserItems listItem = items.get(position); //position 해당하는 listItem
        System.out.println("caluser:"+listItem);
        System.out.println(items);


        //list_item inflate => convertView 참조
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.cal_user_result_item, parent, false);
        }

        ImageView profile = convertView.findViewById(R.id.profile);

        View finalConvertView = convertView;
        db.collection("users").document(listItem.getId()).
                get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        if(document.get("profilePath") != null){
                            System.out.println(listItem.getId());
                            profilePath = document.get("profilePath").toString();
                            Glide.with(finalConvertView).load(profilePath).centerCrop().override(500).into(profile);
                        }

                    }
                }
            }
        });;

        // 텍스트뷰 설정
        TextView username = convertView.findViewById(R.id.name);
        TextView money = convertView.findViewById(R.id.money);

        // 텍스트뷰에 글자 지정
        username.setText(listItem.getUserName());
        money.setText(listItem.getMoney());

        return convertView;
    }
}
