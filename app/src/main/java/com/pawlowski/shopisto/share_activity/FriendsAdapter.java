package com.pawlowski.shopisto.share_activity;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.google.firebase.database.FirebaseDatabase;
import com.pawlowski.shopisto.database.DBHandler;
import com.pawlowski.shopisto.database.OnlineDBHandler;
import com.pawlowski.shopisto.models.FriendModel;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendCardHolder> implements FriendsItemViewMvc.FriendsItemButtonsClickListener {

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
        return new FriendCardHolder(new FriendsItemViewMvc(LayoutInflater.from(parent.getContext()), parent));
    }

    @Override
    public void onBindViewHolder(@NonNull FriendsAdapter.FriendCardHolder holder, int position) {
        holder.viewMvc.clearAllListeners();
        FriendModel currentFriend = friends.get(position);
        holder.viewMvc.bindFriend(currentFriend, amIOwner);
        holder.viewMvc.registerListener(this);
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

    @Override
    public void onToggleClicked(FriendModel currentFriend, boolean isChecked, FriendsItemViewMvc viewMvc) {
        FirebaseDatabase.getInstance().goOnline();
        activity.resetTimer();
        if(isChecked)
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

    }

    class FriendCardHolder extends RecyclerView.ViewHolder
    {
        FriendsItemViewMvc viewMvc;

        public FriendCardHolder(@NonNull FriendsItemViewMvc viewMvc) {
            super(viewMvc.getRootView());

            this.viewMvc = viewMvc;
        }
    }
}
