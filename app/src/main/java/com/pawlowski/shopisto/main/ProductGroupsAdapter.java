package com.pawlowski.shopisto.main;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.FirebaseDatabase;
import com.pawlowski.shopisto.R;
import com.pawlowski.shopisto.base.BaseActivity;
import com.pawlowski.shopisto.choose_group_activity.ChooseGroupActivity;
import com.pawlowski.shopisto.choose_products_from_group_activity.ChooseProductsFromGroupActivity;
import com.pawlowski.shopisto.database.DBHandler;
import com.pawlowski.shopisto.database.OnlineDBHandler;
import com.pawlowski.shopisto.group_activity.GroupActivity;
import com.pawlowski.shopisto.main.products_fragment.ProductsFragment;
import com.pawlowski.shopisto.models.GroupModel;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

public class ProductGroupsAdapter extends RecyclerView.Adapter<ProductGroupsAdapter.GroupHolder> implements ProductGroupsItemViewMvc.ProductGroupsItemButtonsClickListener {

    private ArrayList<GroupModel> groups = new ArrayList<>();
    private final BaseActivity activity;
    private final boolean choosing;
    private int listId = -1;
    private int positionEdited = -1;
    private final Fragment fragment;

    public ProductGroupsAdapter(BaseActivity activity, boolean choosing, int listIdIfChoosing, Fragment fragment/*If not choosing, else can be null*/)
    {
        this.activity = activity;
        this.choosing = choosing;
        this.fragment = fragment;
        if(choosing)
            listId = listIdIfChoosing;
    }

    @NonNull
    @Override
    public GroupHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new GroupHolder(new ProductGroupsItemViewMvc(LayoutInflater.from(parent.getContext()), parent));
    }

    @Override
    public void onBindViewHolder(@NonNull ProductGroupsAdapter.GroupHolder holder, int position) {
        GroupModel currentGroup = groups.get(position);
        holder.viewMvc.clearAllListeners();
        holder.viewMvc.bindGroup(currentGroup, choosing, position);
        holder.viewMvc.registerListener(this);
    }

    @Override
    public int getItemCount() {
        return groups.size();
    }

    public void setGroups(ArrayList<GroupModel>groups)
    {
        this.groups = groups;
        notifyDataSetChanged();
    }

    public void deleteGroup(int position, View view)
    {
        GroupModel deletedGroup = groups.get(position);
        groups.remove(position);
        notifyDataSetChanged();
        if(groups.size() == 0)
            ((ProductsFragment)fragment).showNoGroupsItems();

        DBHandler.getInstance(activity.getApplicationContext()).moveGroupToTrash(deletedGroup.getId());
        Snackbar.make(view, activity.getString(R.string.group_deleted), Snackbar.LENGTH_LONG).setAction(activity.getString(R.string.undo), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(groups.size() == 0)
                    ((ProductsFragment)fragment).hideNoGroupsItems();
                groups.add(position, deletedGroup);
                notifyDataSetChanged();
                DBHandler.getInstance(activity.getApplicationContext()).restoreGroupFromTrash(deletedGroup.getId());
            }
        }).setActionTextColor(activity.getApplicationContext().getResources().getColor(R.color.blue)).show();



    }

    @Override
    public void onConstraintClick(GroupModel currentGroup) {
        if(!choosing)
        {
            ((ProductsFragment)fragment).setChangingActivityTrue();
            GroupActivity.launch(activity, currentGroup.getId(), currentGroup.getTittle(), currentGroup.getKey());
            activity.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
        }
        else
        {
            if(currentGroup.getProducts().size() != 0)
            {
                ChooseProductsFromGroupActivity.launch(activity, listId, currentGroup.getId(), currentGroup.getKey(), ((ChooseGroupActivity)activity).getListKey());
                activity.finish();
                activity.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
            else
            {
                activity.showErrorSnackbar(activity.getString(R.string.no_products_in_chosen_template), true);
            }



        }
    }

    @Override
    public void onEditNamePopUpClick(GroupModel currentGroup, int currentPosition, ProductGroupsItemViewMvc viewMvc) {
        if(positionEdited != -1 && positionEdited != currentPosition)
        {
            notifyItemChanged(positionEdited);
            viewMvc.hideKeyboard();
        }
        else if (positionEdited == currentPosition)
        {
            viewMvc.hideKeyboard();
        }

        positionEdited = currentPosition;

        viewMvc.showTittleEditing();

    }

    @Override
    public void onFinishedEditingClick(GroupModel currentGroup, String tittleInput, int currentPosition, ProductGroupsItemViewMvc viewMvc) {
        if(tittleInput.length() > 0)
        {
            currentGroup.setTittle(tittleInput);
            if(!activity.isOfflineModeOn())
            {
                FirebaseDatabase.getInstance().goOnline();
                ((ProductsFragment)fragment).resetTimer();
                OnlineDBHandler.changeGroupTittle(currentGroup.getKey(), currentGroup.getTittle());
            }

            DBHandler.getInstance(activity.getApplicationContext()).updateGroupTittle(currentGroup.getId(), currentGroup.getTittle());
            DBHandler.getInstance(activity.getApplicationContext()).increaseGroupTimestamp(currentGroup.getKey());

            viewMvc.hideTittleEditing();
            positionEdited = -1;

            viewMvc.hideKeyboard();
            notifyItemChanged(currentPosition);
        }
        else
        {
            activity.showErrorSnackbar(activity.getString(R.string.short_tittle), true);
        }
    }

    @Override
    public void onCopyPopUpClick(GroupModel currentGroup, int currentPosition, ProductGroupsItemViewMvc viewMvc) {
        if(positionEdited != -1) {
            notifyItemChanged(positionEdited);
            viewMvc.hideKeyboard();
        }

        GroupModel groupCopy = groups.get(currentPosition).getCopy();
        if(!groupCopy.getTittle().endsWith(" " + activity.getString(R.string._copy)))
        {
            groupCopy.setTittle(groupCopy.getTittle() + " " + activity.getString(R.string._copy));
        }
        if(!activity.isOfflineModeOn())
            groupCopy.setKey(OnlineDBHandler.insertGroupWithProducts(groupCopy.getTittle(), groupCopy.getProducts()));
        DBHandler.getInstance(activity.getApplicationContext()).copyGroup(groupCopy, activity);

        int id = DBHandler.getInstance(activity.getApplicationContext()).getIdOfLastGroup();

        groupCopy.setId(id);

        groups.add(0, groupCopy);



        notifyItemInserted(0);
        notifyItemRangeChanged(0, groups.size());
        ((ProductsFragment)fragment).scrollToTheStarting();
    }

    @Override
    public void onDeletePopUpClick(GroupModel currentGroup, int currentPosition, ProductGroupsItemViewMvc viewMvc) {
        if(positionEdited != -1) {
            notifyItemChanged(positionEdited);
            viewMvc.hideKeyboard();
        }
        deleteGroup(currentPosition, viewMvc.getRootView());
    }

    class GroupHolder extends RecyclerView.ViewHolder
    {
        ProductGroupsItemViewMvc viewMvc;
        public GroupHolder(@NonNull ProductGroupsItemViewMvc viewMvc) {
            super(viewMvc.getRootView());
            this.viewMvc = viewMvc;
        }
    }
}
