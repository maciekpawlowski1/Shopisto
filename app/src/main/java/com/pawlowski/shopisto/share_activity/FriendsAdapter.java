package com.pawlowski.shopisto.share_activity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.BounceInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.firebase.database.FirebaseDatabase;
import com.pawlowski.shopisto.R;
import com.pawlowski.shopisto.database.DBHandler;
import com.pawlowski.shopisto.database.OnlineDBHandler;
import com.pawlowski.shopisto.models.FriendModel;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendCardHolder> {

    List<FriendModel> friends = new ArrayList<>();
    ShareActivity activity;
    String listKey;
    boolean amIOwner;

    FriendsAdapter(ShareActivity activity, String listKey, boolean amIOwner)
    {
        this.activity = activity;
        this.listKey = listKey;
        this.amIOwner = amIOwner;
    }

    @NonNull
    @Override
    public FriendCardHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.share_to_friend_card,
                parent, false);
        return new FriendCardHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendsAdapter.FriendCardHolder holder, int position) {

        FriendModel currentFriend = friends.get(position);

        if(currentFriend.getNickname().length() == 0)
        {
            holder.mailTextView.setText(currentFriend.getMail());
        }
        else
        {
            holder.mailTextView.setText(currentFriend.getNickname());
        }

        if(currentFriend.isOwner())
        {
            holder.crownImage.setVisibility(View.VISIBLE);
        }
        else
        {
            holder.crownImage.setVisibility(View.INVISIBLE);
        }

        holder.toggleButton.setChecked(currentFriend.isInList());

        if(amIOwner)
        {
            holder.toggleButton.setEnabled(true);

            holder.toggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    FirebaseDatabase.getInstance().goOnline();
                    activity.resetTimer();
                    if(holder.toggleButton.isChecked()) //variable isChecked always works opposite?
                    {
                        OnlineDBHandler.addFriendToList(currentFriend, listKey, activity.getListTittle());
                        DBHandler.getInstance(activity.getApplicationContext()).addFriendToList(currentFriend, activity.getListId());
                        currentFriend.setInList(true);
                        OnlineDBHandler.makeChangesInListFriends(listKey,
                                DBHandler.getInstance(activity.getApplicationContext()).getFriendsFromThisList(activity.getListId()));


                    }
                    else
                    {
                        OnlineDBHandler.removeFriendFromList(currentFriend, listKey);
                        DBHandler.getInstance(activity.getApplicationContext()).removeFriendFromList(currentFriend, activity.getListId());
                        currentFriend.setInList(false);

                    }

                    ScaleAnimation scaleAnimation;
                    BounceInterpolator bounceInterpolator;
                    scaleAnimation = new ScaleAnimation(0.7f, 1.0f, 0.7f, 1.0f, Animation.RELATIVE_TO_SELF, 0.7f, Animation.RELATIVE_TO_SELF, 0.7f);
                    scaleAnimation.setDuration(500);
                    bounceInterpolator = new BounceInterpolator();
                    scaleAnimation.setInterpolator(bounceInterpolator);
                    holder.toggleButton.startAnimation(scaleAnimation);



                }
            });
        }
        else
        {
            holder.toggleButton.setEnabled(false);
        }


    }

    @Override
    public int getItemCount() {
        return friends.size();
    }


    public void setFriends(List<FriendModel> friends)
    {
        this.friends = friends;
        notifyDataSetChanged();
    }

    class FriendCardHolder extends RecyclerView.ViewHolder
    {
        ToggleButton toggleButton;
        TextView mailTextView;
        ImageView crownImage;

        public FriendCardHolder(@NonNull View itemView) {
            super(itemView);

            toggleButton = itemView.findViewById(R.id.toggle_friend_card);
            mailTextView = itemView.findViewById(R.id.mail_text_friend_card);
            crownImage = itemView.findViewById(R.id.crown_friend_card);
        }
    }
}
