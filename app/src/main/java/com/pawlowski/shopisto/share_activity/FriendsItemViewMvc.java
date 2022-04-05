package com.pawlowski.shopisto.share_activity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.BounceInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.pawlowski.shopisto.R;
import com.pawlowski.shopisto.base.BaseObservableViewMvc;
import com.pawlowski.shopisto.models.FriendModel;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class FriendsItemViewMvc extends BaseObservableViewMvc<FriendsItemViewMvc.FriendsItemButtonsClickListener> {

    private final ToggleButton toggleButton;
    private final TextView mailTextView;
    private final ImageView crownImage;

    private FriendModel currentFriend;

    FriendsItemViewMvc(@NonNull LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup)
    {
        rootView = layoutInflater.inflate(R.layout.share_to_friend_card, viewGroup, false);

        toggleButton = findViewById(R.id.toggle_friend_card);
        mailTextView = findViewById(R.id.mail_text_friend_card);
        crownImage = findViewById(R.id.crown_friend_card);

        toggleButton.setEnabled(false);
        toggleButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            for(FriendsItemButtonsClickListener l:listeners)
            {
                l.onToggleClicked(currentFriend, toggleButton.isChecked(), FriendsItemViewMvc.this); //variable isChecked always works opposite?
            }
            animateButton();
        });
    }

    public void bindFriend(FriendModel currentFriend, boolean amIOwner)
    {
        this.currentFriend = currentFriend;

        if(currentFriend.getNickname().length() == 0)
        {
            mailTextView.setText(currentFriend.getMail());
        }
        else
        {
            mailTextView.setText(currentFriend.getNickname());
        }

        if(currentFriend.isOwner())
        {
            crownImage.setVisibility(View.VISIBLE);
        }
        else
        {
            crownImage.setVisibility(View.INVISIBLE);
        }

        toggleButton.setChecked(currentFriend.isInList());

        toggleButton.setEnabled(amIOwner);
    }

    private void animateButton()
    {
        ScaleAnimation scaleAnimation;
        BounceInterpolator bounceInterpolator;
        scaleAnimation = new ScaleAnimation(0.7f, 1.0f, 0.7f, 1.0f, Animation.RELATIVE_TO_SELF, 0.7f, Animation.RELATIVE_TO_SELF, 0.7f);
        scaleAnimation.setDuration(500);
        bounceInterpolator = new BounceInterpolator();
        scaleAnimation.setInterpolator(bounceInterpolator);
        toggleButton.startAnimation(scaleAnimation);
    }

    interface FriendsItemButtonsClickListener {
        void onToggleClicked(FriendModel currentFriend, boolean isChecked, FriendsItemViewMvc viewMvc);
    }
}
