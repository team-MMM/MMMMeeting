// ContentScheduleActivity 화면 (가운데 정렬)
package com.example.mmmmeeting.view;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.example.mmmmeeting.Info.PostInfo;
import com.example.mmmmeeting.Info.ScheduleInfo;
import com.example.mmmmeeting.R;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.VideoListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

public class ReadScheduleView_new extends LinearLayout {
    private Context context;
    private LayoutInflater layoutInflater;
    private ArrayList<SimpleExoPlayer> playerArrayList = new ArrayList<>();
    private int moreIndex = -1;

    public ReadScheduleView_new(Context context) {
        super(context);
        this.context = context;
        initView();
    }

    public ReadScheduleView_new(Context context, @Nullable AttributeSet attributeSet) {
        super(context, attributeSet);
        this.context = context;
        initView();
    }

    private void initView(){
        setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        setOrientation(LinearLayout.VERTICAL);
        layoutInflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        layoutInflater.inflate(R.layout.view_post, this, true);
    }

    public void setMoreIndex(int moreIndex){
        this.moreIndex = moreIndex;
    }

    // 약속 정보
    public void setScheduleInfo(ScheduleInfo postInfo){
        TextView createdAtTextView = findViewById(R.id.createAtTextView);
        createdAtTextView.setText(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(postInfo.getCreatedAt()));

        LinearLayout contentsLayout = findViewById(R.id.contentsLayout);
        ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        ArrayList<String> contentsList = postInfo.getContents();

        for (int i = 0; i < contentsList.size(); i++) {
            if (i == moreIndex) {
                TextView textView = new TextView(context);
                textView.setLayoutParams(layoutParams);
                textView.setText("더보기...");
                contentsLayout.addView(textView);
                break;
            }

            String contents = contentsList.get(i);

            TextView textView = (TextView) layoutInflater.inflate(R.layout.view_contents_text, this, false);
            textView.setText(contents);
            textView.setGravity(Gravity.CENTER);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18);
            contentsLayout.addView(textView);

        }
    }

    public ArrayList<SimpleExoPlayer> getPlayerArrayList() {
        return playerArrayList;
    }
}
