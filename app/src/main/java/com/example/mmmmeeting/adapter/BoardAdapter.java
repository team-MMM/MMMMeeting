// 게시판의 카드뷰 생성, 게시글 생성, 수정, 삭제와 연결됨

package com.example.mmmmeeting.adapter;

import android.app.Activity;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.mmmmeeting.BoardDeleter;
import com.example.mmmmeeting.Info.PostInfo;
import com.example.mmmmeeting.R;
import com.example.mmmmeeting.activity.ContentBoardActivity;
import com.example.mmmmeeting.activity.MakePostActivity;
import com.example.mmmmeeting.OnPostListener;
import com.example.mmmmeeting.view.ReadContentsView;
import com.google.android.exoplayer2.SimpleExoPlayer;

import java.util.ArrayList;

public class BoardAdapter extends RecyclerView.Adapter<BoardAdapter.MainViewHolder> {
    private ArrayList<PostInfo> mDataset;
    private Activity activity;
    private BoardDeleter boardDeleter; //Firestore db에서 삭제 되도록 연동
    private ArrayList<ArrayList<SimpleExoPlayer>> playerArrayListArrayList = new ArrayList<>();
    private final int MORE_INDEX = 2;

    static class MainViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        MainViewHolder(CardView v) {
            super(v);
            cardView = v;
        }
    }

    public BoardAdapter(Activity activity, ArrayList<PostInfo> myDataset) {
        this.mDataset = myDataset;
        this.activity = activity;

        boardDeleter = new BoardDeleter(activity);
    }



    public void setOnPostListener(OnPostListener onPostListener){
        boardDeleter.setOnPostListener(onPostListener);
    }

    @Override
    public int getItemViewType(int position){
        return position;
    }

    @NonNull
    @Override
    public BoardAdapter.MainViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        CardView cardView = (CardView) LayoutInflater.from(parent.getContext()).inflate(R.layout.list_board, parent, false);
        final MainViewHolder mainViewHolder = new MainViewHolder(cardView);
        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity, ContentBoardActivity.class);
                intent.putExtra("postInfo", mDataset.get(mainViewHolder.getAdapterPosition()));
                activity.startActivity(intent);
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

        PostInfo postInfo = mDataset.get(position);
        titleTextView.setText(postInfo.getTitle());

        ReadContentsView readContentsVIew = cardView.findViewById(R.id.readContentsView);
        LinearLayout contentsLayout = cardView.findViewById(R.id.contentsLayout);

        if (contentsLayout.getTag() == null || !contentsLayout.getTag().equals(postInfo)) {
            contentsLayout.setTag(postInfo);
            contentsLayout.removeAllViews();

            readContentsVIew.setMoreIndex(MORE_INDEX);
            readContentsVIew.setPostInfo(postInfo);

            ArrayList<SimpleExoPlayer> playerArrayList = readContentsVIew.getPlayerArrayList();
            if(playerArrayList != null){
                playerArrayListArrayList.add(playerArrayList);
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
                        myStartActivity(MakePostActivity.class, mDataset.get(position));
                        return true;
                    case R.id.delete:
                        boardDeleter.storageDelete(mDataset.get(position));
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

    private void myStartActivity(Class c, PostInfo postInfo) {
        Intent intent = new Intent(activity, c);
        intent.putExtra("postInfo", postInfo);
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