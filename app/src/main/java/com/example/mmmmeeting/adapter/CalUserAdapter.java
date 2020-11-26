
package com.example.mmmmeeting.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.example.mmmmeeting.Info.CalUserItems;
import com.example.mmmmeeting.R;
import com.example.mmmmeeting.activity.MemberInitActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;

public class CalUserAdapter extends BaseAdapter{

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

    public void reset(){
        items.removeAll(items);
    }


    // 뷰 설정
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        context = parent.getContext(); // activity 정보 읽기
        CalUserItems listItem = items.get(position); //position 해당하는 listItem]
        System.out.println("caluser:"+listItem);
        System.out.println(items);

        //list_item inflate => convertView 참조
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.cal_user_item, parent, false);
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
        TextView username = convertView.findViewById(R.id.userName);
        EditText money = convertView.findViewById(R.id.calMoney);

        money.setOnKeyListener(new View.OnKeyListener() {

            @Override
            public boolean onKey(View view, int KeyCode, KeyEvent keyEvent) {
                if (KeyCode == keyEvent.KEYCODE_ENTER)
                    return true;
                return false;
            }
        });

        // 텍스트뷰에 글자 지정
        username.setText(listItem.getUserName());

        //예전 리스너를 삭제한다
        clearTextChangedListener(money);

        //값을 불려오고 해당 리스너를 적용한다
        money.setText(listItem.getMoney());
        money.addTextChangedListener(listItem.mTextWatcher);

        return convertView;
    }

    private void clearTextChangedListener(EditText editText) {
        //리스트 목록의 모든 리스너를 대상으로 검사하여 삭제해 준다
        int count = getCount();

        for (int i = 0 ; i < count ; i++)
            editText.removeTextChangedListener(items.get(i).mTextWatcher);
    }

}

