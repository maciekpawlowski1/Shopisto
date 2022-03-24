package com.pawlowski.shopisto.main;

import android.content.Context;
import android.content.Intent;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.FirebaseDatabase;
import com.pawlowski.shopisto.base.BaseActivity;
import com.pawlowski.shopisto.database.DBHandler;
import com.pawlowski.shopisto.list_activity.ListActivity;
import com.pawlowski.shopisto.database.OnlineDBHandler;
import com.pawlowski.shopisto.models.ListModel;
import com.pawlowski.shopisto.R;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

public class ShoppingListsAdapter extends RecyclerView.Adapter<ShoppingListsAdapter.ListHolder> {

    private ArrayList<ListModel>lists = new ArrayList<>();
    BaseActivity activity;
    //EditText editTittle;
    //TextView tittleTextView;
    //boolean isEditing;
    int positionEdited = -1;
    Fragment fragment;


    ShoppingListsAdapter(BaseActivity activity, Fragment fragment)
    {
        this.activity = activity;
        this.fragment = fragment;
    }

    @NonNull
    @Override
    public ListHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.shopping_list_card,
                parent, false);
        return new ListHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ShoppingListsAdapter.ListHolder holder, int position) {
        ListModel currentList = lists.get(position);

        holder.editTittle.setVisibility(View.INVISIBLE);
        holder.editTittle.setText("");
        holder.finishEdittingButton.setVisibility(View.INVISIBLE);
        holder.listTittleText.setVisibility(View.VISIBLE);
        holder.listTittleText.setText(currentList.getTittle());
        holder.productsReadyText.setText(currentList.getNumberSelected() + "/" + currentList.getNumberAll());
        if(currentList.getNumberAll() == 0)
        {
            holder.progressBar.setProgress(0);

        }
        else
        {
            holder.progressBar.setProgress(currentList.getNumberSelected() * 100 / currentList.getNumberAll());

        }

        holder.constraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(activity, ListActivity.class);
                i.putExtra("listId", currentList.getId());
                i.putExtra("listTittle", currentList.getTittle());
                i.putExtra("listKey", currentList.getFirebaseKey());
                i.putExtra("amIOwner", currentList.isAmIOwner());
                ((ShoppingListsFragment)fragment).setChangingActivityTrue();
                activity.startActivity(i);
                activity.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);



            }
        });

        holder.moreButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                PopupMenu popupMenu = new PopupMenu(activity.getApplicationContext(), holder.moreButton);
                popupMenu.getMenuInflater().inflate(R.menu.list_modify_pop_up_menu, popupMenu.getMenu());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {
                        if(item.getItemId() == R.id.edit_name_pop_up_menu)
                        {
                            if(positionEdited != -1 && positionEdited != position)
                            {
                                notifyItemChanged(positionEdited);
                                InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(holder.editTittle.getWindowToken(), 0);
                            }
                            else if (positionEdited == position)
                            {
                                InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(holder.editTittle.getWindowToken(), 0);
                            }
                            //isEditing = true;
                            //editTittle = holder.editTittle;
                            //tittleTextView = holder.listTittleText;
                            holder.constraintLayout.setClickable(false);
                            holder.listTittleText.setVisibility(View.INVISIBLE);
                            holder.editTittle.setText(currentList.getTittle());
                            holder.editTittle.setVisibility(View.VISIBLE);
                            holder.finishEdittingButton.setVisibility(View.VISIBLE);

                            holder.editTittle.requestFocus();

                            positionEdited = position;
                            holder.editTittle.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                                @Override
                                public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                                    if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                                            actionId == EditorInfo.IME_ACTION_DONE ||
                                            event != null &&
                                                    event.getAction() == KeyEvent.ACTION_DOWN &&
                                                    event.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                                        if (event == null || !event.isShiftPressed()) {
                                            // the user is done typing.
                                            //isEditing = false;

                                            String newTittle = holder.editTittle.getText().toString();
                                            if(newTittle.length() > 0)
                                            {
                                                holder.listTittleText.setVisibility(View.VISIBLE);

                                                holder.editTittle.setVisibility(View.INVISIBLE);
                                                holder.finishEdittingButton.setVisibility(View.INVISIBLE);
                                                holder.constraintLayout.setClickable(true);


                                                currentList.setTittle(newTittle);
                                                FirebaseDatabase.getInstance().goOnline();
                                                ((ShoppingListsFragment)fragment).resetTimer();
                                                ShoppingListsFragment.increaseListTimestamp(activity);
                                                if(!activity.isOfflineModeOn())
                                                    OnlineDBHandler.changeListTittle(currentList.getFirebaseKey(), currentList.getTittle());
                                                DBHandler.getInstance(activity.getApplicationContext()).updateListTittle(currentList.getId(), currentList.getTittle());
                                                positionEdited = -1;
                                                notifyItemChanged(position);
                                                holder.editTittle.setText("");


                                                InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                                                imm.hideSoftInputFromWindow(holder.editTittle.getWindowToken(), 0);

                                            }
                                            else
                                            {
                                                ((BaseActivity)activity).showErrorSnackbar(activity.getString(R.string.short_tittle), true);
                                            }



                                            return true; // consume.
                                        }
                                    }
                                    return false; // pass on to other listeners.
                                }
                            });

                            holder.finishEdittingButton.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    String newTittle = holder.editTittle.getText().toString();
                                    if(newTittle.length() > 0)
                                    {
                                        holder.listTittleText.setVisibility(View.VISIBLE);
                                        currentList.setTittle(holder.editTittle.getText().toString());
                                        FirebaseDatabase.getInstance().goOnline();
                                        ((ShoppingListsFragment)fragment).resetTimer();
                                        ShoppingListsFragment.increaseListTimestamp(activity);
                                        if(!activity.isOfflineModeOn())
                                            OnlineDBHandler.changeListTittle(currentList.getFirebaseKey(), currentList.getTittle());
                                        DBHandler.getInstance(activity.getApplicationContext()).updateListTittle(currentList.getId(), currentList.getTittle());


                                        holder.editTittle.setText("");
                                        holder.editTittle.setVisibility(View.INVISIBLE);
                                        holder.finishEdittingButton.setVisibility(View.INVISIBLE);
                                        holder.constraintLayout.setClickable(true);


                                        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                                        imm.hideSoftInputFromWindow(holder.editTittle.getWindowToken(), 0);
                                        positionEdited = -1;
                                        notifyItemChanged(position);
                                    }
                                    else
                                    {
                                        ((BaseActivity)activity).showErrorSnackbar(activity.getString(R.string.short_tittle), true);

                                    }

                                }
                            });


                            holder.editTittle.setSelection(currentList.getTittle().length());
                            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

                        }
                        else if(item.getItemId() == R.id.copy_pop_up_menu)
                        {
                            if(positionEdited != -1)
                            {
                                notifyItemChanged(positionEdited);
                                positionEdited = -1;
                                InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(holder.editTittle.getWindowToken(), 0);
                            }

                            ListModel listCopy = lists.get(position).getCopy();
                            if(!listCopy.getTittle().endsWith(" " + activity.getString(R.string._copy)))
                            {
                                listCopy.setTittle(listCopy.getTittle() + " " + activity.getString(R.string._copy));
                            }

                            FirebaseDatabase.getInstance().goOnline();
                            ((ShoppingListsFragment)fragment).resetTimer();
                            ShoppingListsFragment.increaseListTimestamp(activity);
                            if(!activity.isOfflineModeOn())
                                listCopy.setFirebaseKey(OnlineDBHandler.copyList(listCopy));
                            DBHandler.getInstance(activity.getApplicationContext()).copyList(listCopy, activity);

                            int id = DBHandler.getInstance(activity.getApplicationContext()).getIdOfLastList();

                            listCopy.setId(id);
                            lists.add(0, listCopy);



                            notifyItemInserted(0);
                            notifyItemRangeChanged(0, lists.size());
                            ((ShoppingListsFragment)fragment).scrollToTheStarting();
                            //notifyDataSetChanged();
                        }
                        else if(item.getItemId() == R.id.delete_pop_up_menu)
                        {
                            if(positionEdited != -1) {
                                notifyItemChanged(positionEdited);
                                positionEdited = -1;
                                InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.hideSoftInputFromWindow(holder.editTittle.getWindowToken(), 0);
                            }

                            deleteList(position, holder.moreButton);


                        }

                        return true;
                    }
                });
                popupMenu.show();
            }
        });
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
        DBHandler.getInstance(activity.getApplicationContext()).moveListToTrash(deletedList.getId());
        Snackbar.make(view, activity.getString(R.string.list_deleted), Snackbar.LENGTH_LONG).setAction(activity.getString(R.string.undo), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(lists.size() == 0)
                {
                    ((ShoppingListsFragment)fragment).hideNoListImage();
                }
                lists.add(position, deletedList);
                DBHandler.getInstance(activity.getApplicationContext()).restoreListFromTrash(deletedList.getId());
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

    /*public void stopEditingTittle()
    {
        if(isEditing && editTittle != null && tittleTextView != null)
        {
            tittleTextView.setVisibility(View.VISIBLE);
            editTittle.setText("");
            editTittle.setVisibility(View.INVISIBLE);


        }

    }*/




    class ListHolder extends RecyclerView.ViewHolder
    {
        TextView listTittleText;
        TextView productsReadyText;
        ProgressBar progressBar;
        ConstraintLayout constraintLayout;
        ImageButton moreButton;
        EditText editTittle;
        ImageButton finishEdittingButton;
        public ListHolder(@NonNull View itemView) {
            super(itemView);
            listTittleText = itemView.findViewById(R.id.list_tittle_card);
            productsReadyText = itemView.findViewById(R.id.products_ready_card);
            progressBar = itemView.findViewById(R.id.progress_bar_card);
            constraintLayout = itemView.findViewById(R.id.layout_shopping_list_card);
            moreButton = itemView.findViewById(R.id.more_button_shopping_list_card);
            editTittle = itemView.findViewById(R.id.edit_tittle_shopping_list_card);
            finishEdittingButton = itemView.findViewById(R.id.stop_editting_button_shopping_lists);
        }
    }
}
