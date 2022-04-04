package com.pawlowski.shopisto.main.restore_fragment;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.pawlowski.shopisto.R;
import com.pawlowski.shopisto.base.BaseObservableViewMvc;
import com.pawlowski.shopisto.database.DBHandler;
import com.pawlowski.shopisto.database.OnlineDBHandler;
import com.pawlowski.shopisto.main.shopping_lists_fragment.ShoppingListsFragment;
import com.pawlowski.shopisto.models.FriendModel;
import com.pawlowski.shopisto.models.GroupModel;
import com.pawlowski.shopisto.models.ListModel;
import com.pawlowski.shopisto.models.Model;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RestoreAdapter extends RecyclerView.Adapter<RestoreAdapter.RestoreCardHolder> implements RestoreListItemViewMvc.RestoreListItemButtonsClickListener, RestoreGroupItemViewMvc.RestoreGroupItemButtonsClickListener {

    List<Model> listsAndGroups = new ArrayList<>();
    Activity activity;
    RestoreFragment restoreFragment;
    boolean offlineMode = false;

    RestoreAdapter(Activity activity, RestoreFragment fragment, boolean offlineMode)
    {
        this.activity = activity;
        this.restoreFragment = fragment;
        this.offlineMode = offlineMode;
    }

    @NonNull
    @Override
    public RestoreCardHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == 1)
        {
            return new RestoreCardHolder(new RestoreListItemViewMvc(LayoutInflater.from(parent.getContext()), parent));
        }
        else
        {
            return new RestoreCardHolder(new RestoreGroupItemViewMvc(LayoutInflater.from(parent.getContext()), parent));
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RestoreAdapter.RestoreCardHolder holder, int position) {
        Model currentListOrGroup = listsAndGroups.get(position);
        if(currentListOrGroup.isItList())
        {
            holder.listViewMvc.clearAllListeners();
            ListModel currentList = (ListModel)currentListOrGroup;
            holder.listViewMvc.bindItem(currentList, position);
            holder.listViewMvc.registerListener(this);
        }
        else
        {
            holder.groupViewMvc.clearAllListeners();
            GroupModel currentGroup = (GroupModel) currentListOrGroup;
            holder.groupViewMvc.bindItem(currentGroup, position);
            holder.groupViewMvc.registerListener(this);
        }

    }

    @Override
    public int getItemCount() {
        return listsAndGroups.size();
    }

    public void setListsAndGroups(List<Model> listsAndGroups)
    {
        this.listsAndGroups = listsAndGroups;

        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if(listsAndGroups.get(position).isItList())
            return 1;
        else
            return 2;
    }

    @Override
    public void onListUndoClick(ListModel currentList, int currentPosition) {
        String tittle = currentList.getTittle();
        DBHandler.getInstance(activity.getApplicationContext()).restoreListFromTrash(currentList.getId());
        listsAndGroups.remove(currentPosition);
        notifyItemRemoved(currentPosition);
        notifyItemRangeChanged(currentPosition, listsAndGroups.size()-currentPosition+1);
        Toast.makeText(activity.getApplicationContext(), activity.getString(R.string.list_has_been_succesfully_restored) +
                " " + tittle, Toast.LENGTH_SHORT).show();


        if(listsAndGroups.size() == 0)
        {
            restoreFragment.showNothingInTrashImage();
        }
    }

    @Override
    public void onListDeleteClick(ListModel currentList, int currentPosition) {
        List<FriendModel>friendsFromList = DBHandler.getInstance(activity.getApplicationContext())
                .getFriendsWithoutNicknamesFromThisList(currentList.getId());



        String tittle = currentList.getTittle();
        ShoppingListsFragment.increaseListTimestamp(activity);
        if(!offlineMode)
        {
            OnlineDBHandler.removeYourselfFromList(currentList.getFirebaseKey());
            OnlineDBHandler.makeChangesInListFriends(currentList.getFirebaseKey(), friendsFromList);
        }

        DBHandler.getInstance(activity.getApplicationContext()).deleteList(currentList.getId());
        listsAndGroups.remove(currentPosition);
        notifyItemRemoved(currentPosition);
        notifyItemRangeChanged(currentPosition, listsAndGroups.size()-currentPosition+1);

        Toast.makeText(activity.getApplicationContext(), activity.getString(R.string.list_has_been_deleted_1) +
                " " + tittle + " " + activity.getString(R.string.list_has_been_deleted_2), Toast.LENGTH_SHORT).show();


        if(listsAndGroups.size() == 0)
        {
            restoreFragment.showNothingInTrashImage();
        }
    }

    @Override
    public void onGroupUndoClick(GroupModel currentGroup, int currentPosition) {
        String tittle = currentGroup.getTittle();
        DBHandler.getInstance(activity.getApplicationContext()).restoreGroupFromTrash(currentGroup.getId());
        listsAndGroups.remove(currentPosition);
        notifyItemRemoved(currentPosition);
        notifyItemRangeChanged(currentPosition, listsAndGroups.size()-currentPosition+1);
        Toast.makeText(activity.getApplicationContext(), activity.getString(R.string.group_has_been_succesfully_restored) +
                " " + tittle, Toast.LENGTH_SHORT).show();


        if(listsAndGroups.size() == 0)
        {
            restoreFragment.showNothingInTrashImage();
        }
    }

    @Override
    public void onGroupDeleteClick(GroupModel currentGroup, int currentPosition) {
        String tittle = currentGroup.getTittle();
        if(!offlineMode)
            OnlineDBHandler.deleteGroup(currentGroup.getKey());

        DBHandler.getInstance(activity.getApplicationContext()).deleteGroup(currentGroup.getId());
        listsAndGroups.remove(currentPosition);
        notifyItemRemoved(currentPosition);
        notifyItemRangeChanged(currentPosition, listsAndGroups.size()-currentPosition+1);
        Toast.makeText(activity.getApplicationContext(), activity.getString(R.string.group_has_been_deleted_1) +
                " " + tittle + " " + activity.getString(R.string.group_has_been_deleted_2), Toast.LENGTH_SHORT).show();


        if(listsAndGroups.size() == 0)
        {
            restoreFragment.showNothingInTrashImage();
        }
    }

    class RestoreCardHolder extends RecyclerView.ViewHolder
    {
        RestoreListItemViewMvc listViewMvc;
        RestoreGroupItemViewMvc groupViewMvc;

        public <ListenerType, ModelType> RestoreCardHolder(@NonNull BaseRestoreItemViewMvc<ListenerType, ModelType> viewMvc)  {
            super(viewMvc.getRootView());

            if (viewMvc instanceof RestoreListItemViewMvc)
            {
                listViewMvc = (RestoreListItemViewMvc) viewMvc;
            }
            else if (viewMvc instanceof RestoreGroupItemViewMvc)
            {
                groupViewMvc = (RestoreGroupItemViewMvc) viewMvc;
            }


        }
    }
}
