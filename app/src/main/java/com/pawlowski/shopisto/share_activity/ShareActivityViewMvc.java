package com.pawlowski.shopisto.share_activity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.pawlowski.shopisto.R;
import com.pawlowski.shopisto.base.BaseObservableViewMvc;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class ShareActivityViewMvc extends BaseObservableViewMvc<ShareActivityViewMvc.ShareActivityButtonsClickListener> {

    private final ImageButton imageButton;
    private final CardView cardView;
    private final RecyclerView recycler;
    private final TextView textView;

    ShareActivityViewMvc(@NonNull LayoutInflater layoutInflater, @Nullable ViewGroup viewGroup)
    {
        rootView = layoutInflater.inflate(R.layout.activity_share, viewGroup, false);
        imageButton = findViewById(R.id.image_button_share);
        cardView = findViewById(R.id.card_view_share);
        recycler = findViewById(R.id.friends_recycler_share);
        textView = findViewById(R.id.text_view_share_activity);

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(ShareActivityButtonsClickListener l:listeners)
                {
                    l.onImageButtonClick();
                }
            }
        });

        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for(ShareActivityButtonsClickListener l:listeners)
                {
                    l.onCardClick();
                }

            }
        });
    }

    public void setTextViewText(String text)
    {
        textView.setText(text);
    }

    public void setAdapter(FriendsAdapter adapter)
    {
        recycler.setAdapter(adapter);
        recycler.setLayoutManager(new LinearLayoutManager(rootView.getContext()));
    }

    interface ShareActivityButtonsClickListener
    {
        void onImageButtonClick();
        void onCardClick();
    }
}
