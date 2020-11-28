// 약속 목록의 카드뷰 생성, 게시글 생성, 수정, 삭제와 연결됨

package com.example.mmmmeeting.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.view.View.OnLongClickListener;
import android.content.Context;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.PopupMenu;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.example.mmmmeeting.activity.AlarmActivity;
import com.example.mmmmeeting.Info.ScheduleInfo;
import com.example.mmmmeeting.OnScheduleListener;
import com.example.mmmmeeting.R;
import com.example.mmmmeeting.ScheduleDeleter;
import com.example.mmmmeeting.activity.CalendarActivity;
import com.example.mmmmeeting.activity.EditScheduleActivity;
import com.example.mmmmeeting.activity.MakeScheduleActivity;
import com.example.mmmmeeting.activity.ContentScheduleActivity;
import com.example.mmmmeeting.activity.NoticeActivity;
import com.example.mmmmeeting.view.ReadScheduleView;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.api.Distribution;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.MainViewHolder> {
    private ArrayList<ScheduleInfo> mDataset;
    private Activity activity;
    private ScheduleDeleter scheduleDeleter; //Firestore db에서 삭제 되도록 연동
    private ArrayList<ArrayList<SimpleExoPlayer>> playerArrayListArrayList = new ArrayList<>();
    private final int MORE_INDEX = 2;

    static class MainViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        MainViewHolder(CardView v) {
            super(v);
            cardView = v;
        }
    }

    public ScheduleAdapter(Activity activity, ArrayList<ScheduleInfo> myDataset) {
        this.mDataset = myDataset;
        this.activity = activity;

        scheduleDeleter = new ScheduleDeleter(activity);
    }



    public void setOnPostListener(OnScheduleListener onPostListener){
        scheduleDeleter.setOnPostListener(onPostListener);
    }

    @Override
    public int getItemViewType(int position){
        return position;
    }

    @NonNull
    @Override
    public ScheduleAdapter.MainViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CardView cardView = (CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.list_schedule, parent, false);
        final MainViewHolder mainViewHolder = new MainViewHolder(cardView);
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String type = mDataset.get(mainViewHolder.getAdapterPosition()).getType();
                Intent intent;
                if(type.equals("online")){
                    intent = new Intent(activity, CalendarActivity.class);
                    intent.putExtra("scheduleInfo", mDataset.get(mainViewHolder.getAdapterPosition()));
                }else {
                    intent = new Intent(activity, ContentScheduleActivity.class);
                    intent.putExtra("scheduleInfo", mDataset.get(mainViewHolder.getAdapterPosition()));
                }
                activity.startActivity(intent);
            }
        });

        cardView.setOnLongClickListener(new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                ScheduleInfo scInfo = mDataset.get(mainViewHolder.getAdapterPosition());

                if(scInfo.getMeetingDate()!=null){
                    Date meetingDate =  scInfo.getMeetingDate();
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일 a hh시 mm분");

                    AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                    builder.setTitle("알림 설정")        // 제목
                            .setMessage("약속 날짜는 " + sdf.format(meetingDate)+ "입니다." + "\n" + "약속 미리 알림을 받으시겠습니까?")        // 메세지
                            .setPositiveButton("확인", new DialogInterface.OnClickListener(){
                                // 확인 버튼 클릭시 설정, 오른쪽 버튼입니다.
                                public void onClick(DialogInterface dialog, int whichButton){//약속 날짜를 확정 //db로 해당 날짜 올리기
                                    Calendar cal = Calendar.getInstance();
                                    cal.setTime(meetingDate);
                                    Intent intent = new Intent(activity, AlarmActivity.class);
                                    intent.putExtra("alarm", cal);
                                    intent.putExtra("date", scInfo.getTitle());
                                    activity.startActivityForResult(intent, 0);
                                    Toast.makeText(activity.getApplicationContext(), sdf.format(meetingDate) + "에 설정되었습니다.", Toast.LENGTH_SHORT).show();
                                }
                            })
                            .setNegativeButton("취소", new DialogInterface.OnClickListener(){// 취소 버튼 클릭시
                                public void onClick(DialogInterface dialog, int whichButton){//취소 이벤트...
                                }
                            });
                    AlertDialog dialog = builder.create();    // 알림창 객체 생성
                    dialog.show();    // 알림창 띄우기

                }else {
                    Toast.makeText(activity.getApplicationContext(), "아직 모임 날짜가 없습니다.", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });
        cardView.findViewById(R.id.menu).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopup(v, mainViewHolder.getAdapterPosition());
            }
        });

        return mainViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull final MainViewHolder holder, int position) {
        CardView cardView = holder.cardView;
        TextView titleTextView = cardView.findViewById(R.id.titleTextView);
        TextView meetingDateView = cardView.findViewById(R.id.meetingDate);
        TextView meetingPlaceView = cardView.findViewById(R.id.meetingPlace);

        ScheduleInfo postInfo = mDataset.get(position);
        String type = postInfo.getType();
        titleTextView.setText(postInfo.getTitle());

        if (postInfo.getMeetingDate() != null) {
            Date meetingDate = postInfo.getMeetingDate();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy년 MM월 dd일 a hh시 mm분");
            meetingDateView.setText("모임 날짜 : " + sdf.format(meetingDate));
        } else {
            meetingDateView.setText("모임 날짜 : 미정");
        }


        if(type.equals("offline")) {
            if (postInfo.getMeetingPlace() != null) {
                meetingPlaceView.setText("모임 장소 : " + postInfo.getMeetingPlace());
            } else {
                meetingPlaceView.setText("모임 장소 : 미정");
            }
        }else{
            meetingPlaceView.setText(postInfo.getContents().get(0));
            meetingPlaceView.setTextSize(18);
            TextView createdAtTextView = cardView.findViewById(R.id.createAtTextView);
            createdAtTextView.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(postInfo.getCreatedAt()));
        }


        FrameLayout frame =cardView.findViewById(R.id.frame);
        if(postInfo.getMeetingDate()!=null && postInfo.getMeetingPlace()!=null) {
            frame.setVisibility(View.VISIBLE);
        }
        else{
            frame.setVisibility(View.INVISIBLE);
        }

        if(type.equals("offline")) {
            ReadScheduleView readScheduleView = cardView.findViewById(R.id.readScheduleView);
            LinearLayout contentsLayout = cardView.findViewById(R.id.contentsLayout);

            if (contentsLayout.getTag() == null || !contentsLayout.getTag().equals(postInfo)) {
                contentsLayout.setTag(postInfo);
                contentsLayout.removeAllViews();

                readScheduleView.setMoreIndex(MORE_INDEX);
                readScheduleView.setScheduleInfo(postInfo);

                ArrayList<SimpleExoPlayer> playerArrayList = readScheduleView.getPlayerArrayList();
                if (playerArrayList != null) {
                    playerArrayListArrayList.add(playerArrayList);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    private void showPopup(View v, final int position) {
        PopupMenu popup = new PopupMenu(activity, v);
        popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.modify:
                        myStartActivity(EditScheduleActivity.class, mDataset.get(position));
                        return true;
                    case R.id.delete:
                        scheduleDeleter.storageDelete(mDataset.get(position));
                        return true;
                    default:
                        return false;
                }
            }
        });

        MenuInflater inflater = popup.getMenuInflater();
        inflater.inflate(R.menu.post, popup.getMenu());
        popup.show();
    }

    private void myStartActivity(Class c, ScheduleInfo postInfo) {
        Intent intent = new Intent(activity, c);
        intent.putExtra("scheduleInfo", postInfo);
        activity.startActivity(intent);
    }

    public void playerStop(){
        for(int i = 0; i < playerArrayListArrayList.size(); i++){
            ArrayList<SimpleExoPlayer> playerArrayList = playerArrayListArrayList.get(i);
            for(int ii = 0; ii < playerArrayList.size(); ii++){
                SimpleExoPlayer player = playerArrayList.get(ii);
                if(player.getPlayWhenReady()){
                    player.setPlayWhenReady(false);
                }
            }
        }
    }
}
