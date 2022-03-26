package com.pawlowski.shopisto.add_friend_activity;

import android.text.Editable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.pawlowski.shopisto.R;
import com.pawlowski.shopisto.base.BaseObservableViewMvc;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

public class AddFriendViewMvc extends BaseObservableViewMvc<AddFriendViewMvc.AddFriendButtonsClickListener> {

    private final TextInputEditText mailInput;
    private final FloatingActionButton searchFriendButton;

    private final TextView userFoundTextView;
    private final CardView userFoundCard;
    private final FloatingActionButton addUserButton;
    private final TextView userMailTextView;

    private final TextView userNotFoundTextView;
    private final ImageView userNotFoundImage;

    AddFriendViewMvc(@NonNull LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup)
    {
        rootView = layoutInflater.inflate(R.layout.activity_add_friend, viewGroup, false);
        mailInput = findViewById(R.id.mail_input_add_friend);
        searchFriendButton = findViewById(R.id.search_button_add_friend);
        userFoundTextView = findViewById(R.id.user_found_text_add_friend);
        userFoundCard = findViewById(R.id.user_found_card_view_add_friend);
        addUserButton = findViewById(R.id.add_friend_button_add_friend);
        userMailTextView = findViewById(R.id.mail_text_add_friend);
        userNotFoundTextView = findViewById(R.id.not_found_text_add_friend);
        userNotFoundImage = findViewById(R.id.not_found_image_add_friend);

        userNotFoundTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(AddFriendButtonsClickListener l:listeners)
                {
                    l.onUserNotFoundTextClick();
                }
            }
        });

        searchFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(AddFriendButtonsClickListener l:listeners)
                {
                    l.onSearchFriendButtonClick();
                }
            }
        });

        addUserButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(AddFriendButtonsClickListener l:listeners)
                {
                    l.onAddFriendClick();
                }
            }
        });
    }

    public void showNotFoundImage()
    {
        userNotFoundTextView.setVisibility(View.VISIBLE);
        userNotFoundImage.setVisibility(View.VISIBLE);
    }

    public void hideNotFoundImage()
    {
        userNotFoundTextView.setVisibility(View.GONE);
        userNotFoundImage.setVisibility(View.GONE);
    }

    public void showUserFound(String mail)
    {

        userFoundCard.setVisibility(View.VISIBLE);
        userMailTextView.setText(mail);
        userFoundTextView.setVisibility(View.VISIBLE);
    }

    public void hideUserFound()
    {
        userFoundCard.setVisibility(View.GONE);
        userFoundTextView.setVisibility(View.GONE);
    }

    public void resetMailInput()
    {
        mailInput.setText("");
    }

    public String getMailInputText()
    {
        Editable editable = mailInput.getText();
        if(editable != null)
        {
            return editable.toString();
        }
        else
            return "";
    }

    public void changeClickableOfSearchFriendButton(boolean isClickable)
    {
        searchFriendButton.setClickable(isClickable);
    }

    interface AddFriendButtonsClickListener
    {
        void onSearchFriendButtonClick();
        void onAddFriendClick();
        void onUserNotFoundTextClick();
    }
}
