package com.pawlowski.shopisto.main;

import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.pawlowski.shopisto.R;
import com.pawlowski.shopisto.database.DBHandler;
import com.pawlowski.shopisto.database.OnlineDBHandler;
import com.pawlowski.shopisto.models.FriendModel;
import com.pawlowski.shopisto.models.GroupModel;
import com.pawlowski.shopisto.models.ListModel;
import com.pawlowski.shopisto.models.Model;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RestoreAdapter extends RecyclerView.Adapter<RestoreAdapter.RestoreCardHolder> {

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
        View view;
        if(viewType == 1)
        {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.restore_list_card,
                    parent, false);
        }
        else
        {
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.restore_group_card,
                    parent, false);
        }
        return new RestoreCardHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RestoreAdapter.RestoreCardHolder holder, int position) {
        Model currentListOrGroup = listsAndGroups.get(position);
        if(currentListOrGroup.isItList())
        {
            ListModel currentList = (ListModel)currentListOrGroup;
            holder.listTittleText.setText(currentList.getTittle());

            if(currentList.getNumberAll() != 0)
            {
                holder.progressBar.setProgress(100*currentList.getNumberSelected()/currentList.getNumberAll());
            }
            else
            {
                holder.progressBar.setProgress(0);
            }
            holder.progressText.setText(currentList.getNumberSelected()+"/"+currentList.getNumberAll());

            holder.undoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String tittle = currentList.getTittle();
                    DBHandler.getInstance(activity.getApplicationContext()).restoreListFromTrash(currentList.getId());
                    listsAndGroups.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, listsAndGroups.size()-position+1);
                    Toast.makeText(activity.getApplicationContext(), activity.getString(R.string.list_has_been_succesfully_restored) +
                            " " + tittle, Toast.LENGTH_SHORT).show();


                    if(listsAndGroups.size() == 0)
                    {
                        restoreFragment.showNothingInTrashImage();
                    }
                }
            });

            holder.deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
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
                    listsAndGroups.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, listsAndGroups.size()-position+1);

                    Toast.makeText(activity.getApplicationContext(), activity.getString(R.string.list_has_been_deleted_1) +
                            " " + tittle + " " + activity.getString(R.string.list_has_been_deleted_2), Toast.LENGTH_SHORT).show();


                    if(listsAndGroups.size() == 0)
                    {
                        restoreFragment.showNothingInTrashImage();
                    }
                }
            });


        }
        else
        {
            GroupModel currentGroup = (GroupModel) currentListOrGroup;
            holder.groupTittleText.setText(currentGroup.getTittle());
            holder.groupNumberText.setText(currentGroup.getProducts().size()+" " + activity.getString(R.string.xx_products));


            holder.groupUndoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String tittle = currentGroup.getTittle();
                    DBHandler.getInstance(activity.getApplicationContext()).restoreGroupFromTrash(currentGroup.getId());
                    listsAndGroups.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, listsAndGroups.size()-position+1);
                    Toast.makeText(activity.getApplicationContext(), activity.getString(R.string.group_has_been_succesfully_restored) +
                            " " + tittle, Toast.LENGTH_SHORT).show();


                    if(listsAndGroups.size() == 0)
                    {
                        restoreFragment.showNothingInTrashImage();
                    }
                }
            });

            holder.groupDeleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String tittle = currentGroup.getTittle();
                    if(!offlineMode)
                        OnlineDBHandler.deleteGroup(currentGroup.getKey());

                    DBHandler.getInstance(activity.getApplicationContext()).deleteGroup(currentGroup.getId());
                    listsAndGroups.remove(position);
                    notifyItemRemoved(position);
                    notifyItemRangeChanged(position, listsAndGroups.size()-position+1);
                    Toast.makeText(activity.getApplicationContext(), activity.getString(R.string.group_has_been_deleted_1) +
                            " " + tittle + " " + activity.getString(R.string.group_has_been_deleted_2), Toast.LENGTH_SHORT).show();


                    if(listsAndGroups.size() == 0)
                    {
                        restoreFragment.showNothingInTrashImage();
                    }
                }
            });

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

    class RestoreCardHolder extends RecyclerView.ViewHolder
    {
        TextView listTittleText;
        TextView progressText;
        ProgressBar progressBar;
        Button undoButton;
        Button deleteButton;

        TextView groupTittleText;
        TextView groupNumberText;
        Button groupUndoButton;
        Button groupDeleteButton;

        public RestoreCardHolder(@NonNull View itemView) {
            super(itemView);

            listTittleText = itemView.findViewById(R.id.tittle_restore_list_card);
            progressText = itemView.findViewById(R.id.text_progress_restore_list_card);
            progressBar = itemView.findViewById(R.id.progress_bar_restore_list_card);
            undoButton = itemView.findViewById(R.id.undo_button_restore_list_card);
            deleteButton = itemView.findViewById(R.id.delete_button_restore_list_card);

            groupDeleteButton = itemView.findViewById(R.id.delete_button_restore_group_card);
            groupUndoButton = itemView.findViewById(R.id.undo_button_restore_group_card);
            groupTittleText = itemView.findViewById(R.id.tittle_restore_group_card);
            groupNumberText = itemView.findViewById(R.id.number_restore_group_card);


        }
    }
}
