package com.pawlowski.shopisto.main.shopping_lists_fragment;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.FirebaseDatabase;
import com.pawlowski.shopisto.R;
import com.pawlowski.shopisto.base.BaseActivity;
import com.pawlowski.shopisto.database.DBHandler;
import com.pawlowski.shopisto.database.OnlineDBHandler;
import com.pawlowski.shopisto.list_activity.ListActivity;
import com.pawlowski.shopisto.models.ListModel;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

public class ShoppingListsAdapter extends RecyclerView.Adapter<ShoppingListsAdapter.ListHolder> implements ShoppingListsFragmentItemViewMvc.ShoppingListsFragmentItemButtonsClickListener {

    private ArrayList<ListModel>lists = new ArrayList<>();
    BaseActivity activity;
    int positionEdited = -1;
    Fragment fragment;
    private final DBHandler dbHandler;


    ShoppingListsAdapter(BaseActivity activity, Fragment fragment, DBHandler dbHandler)
    {
        this.activity = activity;
        this.fragment = fragment;
        this.dbHandler = dbHandler;
    }

    @NonNull
    @Override
    public ListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ListHolder(new ShoppingListsFragmentItemViewMvc(LayoutInflater.from(parent.getContext()), parent));
    }

    @Override
    public void onBindViewHolder(@NonNull ShoppingListsAdapter.ListHolder holder, int position) {
        ListModel currentList = lists.get(position);
        holder.viewMvc.clearAllListeners();
        holder.viewMvc.bindList(currentList, position);
        holder.viewMvc.registerListener(this);
    }


    public void deleteList(int position, View view)
    {
        ListModel deletedList = lists.get(position);
        lists.remove(position);
        if(lists.size() == 0)
        {
            ((ShoppingListsFragment)fragment).showNoListImage();
        }
        notifyDataSetChanged();
        dbHandler.moveListToTrash(deletedList.getId());
        Snackbar.make(view, activity.getString(R.string.list_deleted), Snackbar.LENGTH_LONG).setAction(activity.getString(R.string.undo), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(lists.size() == 0)
                {
                    ((ShoppingListsFragment)fragment).hideNoListImage();
                }
                lists.add(position, deletedList);
                dbHandler.restoreListFromTrash(deletedList.getId());
                notifyDataSetChanged();
            }
        }).setActionTextColor(activity.getApplicationContext().getResources().getColor(R.color.blue)).show();




    }

    public void setLists(List<ListModel> lists)
    {
        this.lists = new ArrayList<>(lists);
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return lists.size();
    }

    @Override
    public void onConstraintLayoutClick(ListModel currentList) {
        ((ShoppingListsFragment)fragment).setChangingActivityTrue();
        ListActivity.launch(activity, currentList.getId(), currentList.getTittle(), currentList.getFirebaseKey(), currentList.isAmIOwner());
        activity.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
    }



    @Override
    public void onTypingDone(ListModel currentList, int currentPosition, ShoppingListsFragmentItemViewMvc viewMvc) {
        String newTittle = viewMvc.getEditTittleInputText();
        if(newTittle.length() > 0)
        {
            viewMvc.changeClickableOfConstraint(true);


            currentList.setTittle(newTittle);
            FirebaseDatabase.getInstance().goOnline();
            ((ShoppingListsFragment)fragment).resetTimer();
            ShoppingListsFragment.increaseListTimestamp(activity);
            if(!activity.isOfflineModeOn())
                OnlineDBHandler.changeListTittle(currentList.getFirebaseKey(), currentList.getTittle());
            dbHandler.updateListTittle(currentList.getId(), currentList.getTittle());
            positionEdited = -1;
            viewMvc.bindList(currentList, currentPosition);
            notifyItemChanged(currentPosition);

            viewMvc.hideKeyboard();
        }
        else
        {
            activity.showErrorSnackbar(activity.getString(R.string.short_tittle), true);
        }
    }

    @Override
    public void onFinishEditingClick(ListModel currentList, int currentPosition, ShoppingListsFragmentItemViewMvc viewMvc) {
        String newTittle = viewMvc.getEditTittleInputText();
        if(newTittle.length() > 0)
        {
            currentList.setTittle(newTittle);
            FirebaseDatabase.getInstance().goOnline();
            ((ShoppingListsFragment)fragment).resetTimer();
            ShoppingListsFragment.increaseListTimestamp(activity);
            if(!activity.isOfflineModeOn())
                OnlineDBHandler.changeListTittle(currentList.getFirebaseKey(), currentList.getTittle());
            dbHandler.updateListTittle(currentList.getId(), currentList.getTittle());


            viewMvc.bindList(currentList, currentPosition);
            viewMvc.changeClickableOfConstraint(true);


            viewMvc.hideKeyboard();
            positionEdited = -1;
            notifyItemChanged(currentPosition);
        }
        else
        {
            activity.showErrorSnackbar(activity.getString(R.string.short_tittle), true);

        }
    }

    @Override
    public void onEditPopUpButtonClick(ListModel currentList, int currentPosition, ShoppingListsFragmentItemViewMvc viewMvc) {
        if(positionEdited != -1 && positionEdited != currentPosition)
        {
            notifyItemChanged(positionEdited);
            viewMvc.hideKeyboard();
        }
        else if (positionEdited == currentPosition)
        {
            viewMvc.hideKeyboard();
        }

        viewMvc.changeClickableOfConstraint(false);

        positionEdited = currentPosition;

        viewMvc.showEditTittleItems();
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    @Override
    public void onDeletePopUpButtonClick(ListModel currentList, int currentPosition, ShoppingListsFragmentItemViewMvc viewMvc) {
        if(positionEdited != -1) {
            notifyItemChanged(positionEdited);
            positionEdited = -1;
            viewMvc.hideKeyboard();
        }

        deleteList(currentPosition, viewMvc.getRootView());
    }

    @Override
    public void onCopyPopUpButtonClick(ListModel currentList, int currentPosition, ShoppingListsFragmentItemViewMvc viewMvc) {
        if(positionEdited != -1)
        {
            notifyItemChanged(positionEdited);
            positionEdited = -1;
            viewMvc.hideKeyboard();
        }

        ListModel listCopy = lists.get(currentPosition).getCopy();
        if(!listCopy.getTittle().endsWith(" " + activity.getString(R.string._copy)))
        {
            listCopy.setTittle(listCopy.getTittle() + " " + activity.getString(R.string._copy));
        }

        FirebaseDatabase.getInstance().goOnline();
        ((ShoppingListsFragment)fragment).resetTimer();
        ShoppingListsFragment.increaseListTimestamp(activity);
        if(!activity.isOfflineModeOn())
            listCopy.setFirebaseKey(OnlineDBHandler.copyList(listCopy));
        dbHandler.copyList(listCopy, activity);

        int id = dbHandler.getIdOfLastList();

        listCopy.setId(id);
        lists.add(0, listCopy);



        notifyItemInserted(0);
        notifyItemRangeChanged(0, lists.size());
        ((ShoppingListsFragment)fragment).scrollToTheStarting();
    }


    class ListHolder extends RecyclerView.ViewHolder
    {
        ShoppingListsFragmentItemViewMvc viewMvc;
        public ListHolder(@NonNull ShoppingListsFragmentItemViewMvc viewMvc) {
            super(viewMvc.getRootView());
            this.viewMvc = viewMvc;
        }
    }
}
