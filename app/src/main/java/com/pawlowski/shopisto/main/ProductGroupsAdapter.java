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
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.FirebaseDatabase;
import com.pawlowski.shopisto.base.BaseActivity;
import com.pawlowski.shopisto.choose_group_activity.ChooseGroupActivity;
import com.pawlowski.shopisto.ChooseProductsFromGroupActivity;
import com.pawlowski.shopisto.group_activity.GroupActivity;
import com.pawlowski.shopisto.database.DBHandler;
import com.pawlowski.shopisto.database.OnlineDBHandler;
import com.pawlowski.shopisto.models.GroupModel;
import com.pawlowski.shopisto.R;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

public class ProductGroupsAdapter extends RecyclerView.Adapter<ProductGroupsAdapter.GroupHolder> {

    ArrayList<GroupModel> groups = new ArrayList<>();
    BaseActivity activity;
    boolean choosing;
    int listId = -1;
    int positionEdited = -1;
    Fragment fragment;

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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_group_card,
                parent, false);
        return new GroupHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductGroupsAdapter.GroupHolder holder, int position) {
        GroupModel currentGroup = groups.get(position);

        holder.editTittle.setVisibility(View.INVISIBLE);
        holder.tittleText.setVisibility(View.VISIBLE);
        holder.finishEdittingButton.setVisibility(View.INVISIBLE);
        holder.editTittle.setText("");
        holder.tittleText.setText(currentGroup.getTittle());
        holder.numberText.setText(currentGroup.getProducts().size()+" " + activity.getString(R.string.xx_products));
        holder.constraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!choosing)
                {
                    Intent i = new Intent(activity, GroupActivity.class);
                    i.putExtra("groupTittle", currentGroup.getTittle());
                    i.putExtra("groupId", currentGroup.getId());
                    i.putExtra("groupKey", currentGroup.getKey());
                    ((ProductsFragment)fragment).setChangingActivityTrue();
                    activity.startActivity(i);
                    activity.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                }
                else
                {
                    if(currentGroup.getProducts().size() != 0)
                    {
                        Intent i = new Intent(activity, ChooseProductsFromGroupActivity.class);
                        i.putExtra("listId", listId);
                        i.putExtra("groupTittle", currentGroup.getTittle());
                        i.putExtra("groupId", currentGroup.getId());
                        i.putExtra("groupKey", currentGroup.getKey());
                        i.putExtra("listKey", ((ChooseGroupActivity)activity).getListKey());
                        activity.startActivity(i);
                        activity.finish();
                        activity.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                    }
                    else
                    {
                        ((ChooseGroupActivity)activity).showErrorSnackbar(activity.getString(R.string.no_products_in_chosen_template), true);
                    }



                }

            }
        });

        if(choosing)
        {
            holder.moreButton.setVisibility(View.INVISIBLE);
        }
        else
        {
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
                                holder.tittleText.setVisibility(View.INVISIBLE);
                                holder.editTittle.setText(currentGroup.getTittle());
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
                                                    holder.tittleText.setVisibility(View.VISIBLE);

                                                    holder.editTittle.setVisibility(View.INVISIBLE);
                                                    holder.finishEdittingButton.setVisibility(View.INVISIBLE);
                                                    holder.constraintLayout.setClickable(true);


                                                    currentGroup.setTittle(newTittle);
                                                    if(!activity.isOfflineModeOn())
                                                    {
                                                        FirebaseDatabase.getInstance().goOnline();
                                                        ((ProductsFragment)fragment).resetTimer();
                                                        OnlineDBHandler.changeGroupTittle(currentGroup.getKey(), currentGroup.getTittle());
                                                    }

                                                    DBHandler.getInstance(activity.getApplicationContext()).updateGroupTittle(currentGroup.getId(), currentGroup.getTittle());
                                                    DBHandler.getInstance(activity.getApplicationContext()).increaseGroupTimestamp(currentGroup.getKey());

                                                    notifyItemChanged(position);
                                                    holder.editTittle.setText("");
                                                    positionEdited = -1;


                                                    InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                                                    imm.hideSoftInputFromWindow(holder.editTittle.getWindowToken(), 0);
                                                }
                                                else
                                                {
                                                    /*InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                                                    imm.hideSoftInputFromWindow(holder.editTittle.getWindowToken(), 0);*/
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
                                            holder.tittleText.setVisibility(View.VISIBLE);
                                            currentGroup.setTittle(holder.editTittle.getText().toString());
                                            if(!activity.isOfflineModeOn())
                                            {
                                                FirebaseDatabase.getInstance().goOnline();
                                                ((ProductsFragment)fragment).resetTimer();
                                                OnlineDBHandler.changeGroupTittle(currentGroup.getKey(), currentGroup.getTittle());
                                            }

                                            DBHandler.getInstance(activity.getApplicationContext()).updateGroupTittle(currentGroup.getId(), currentGroup.getTittle());
                                            DBHandler.getInstance(activity.getApplicationContext()).increaseGroupTimestamp(currentGroup.getKey());

                                            holder.editTittle.setText("");
                                            holder.editTittle.setVisibility(View.INVISIBLE);
                                            holder.finishEdittingButton.setVisibility(View.INVISIBLE);
                                            holder.constraintLayout.setClickable(true);
                                            positionEdited = -1;


                                            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                                            imm.hideSoftInputFromWindow(holder.editTittle.getWindowToken(), 0);
                                            notifyItemChanged(position);
                                        }
                                        else
                                        {
                                            ((BaseActivity)activity).showErrorSnackbar(activity.getString(R.string.short_tittle), true);
                                        }

                                    }
                                });


                                holder.editTittle.setSelection(currentGroup.getTittle().length());
                                InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
                                //imm.hideSoftInputFromWindow(holder.editTittle.getWindowToken(), 0);

                            }
                            else if(item.getItemId() == R.id.copy_pop_up_menu)
                            {
                                if(positionEdited != -1) {
                                    notifyItemChanged(positionEdited);
                                    InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                                    imm.hideSoftInputFromWindow(holder.editTittle.getWindowToken(), 0);
                                }

                                GroupModel groupCopy = groups.get(position).getCopy();
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
                            else if(item.getItemId() == R.id.delete_pop_up_menu)
                            {
                                if(positionEdited != -1) {
                                    notifyItemChanged(positionEdited);
                                    InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                                    imm.hideSoftInputFromWindow(holder.editTittle.getWindowToken(), 0);
                                }
                                deleteGroup(position, holder.moreButton);


                            }

                            return true;
                        }
                    });
                    popupMenu.show();

                }
            });
        }

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
            ((ProductsFragment)fragment).showNoGroupsImage();

        DBHandler.getInstance(activity.getApplicationContext()).moveGroupToTrash(deletedGroup.getId());
        Snackbar.make(view, activity.getString(R.string.group_deleted), Snackbar.LENGTH_LONG).setAction(activity.getString(R.string.undo), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(groups.size() == 0)
                    ((ProductsFragment)fragment).hideNoGroupsImage();
                groups.add(position, deletedGroup);
                notifyDataSetChanged();
                DBHandler.getInstance(activity.getApplicationContext()).restoreGroupFromTrash(deletedGroup.getId());
            }
        }).setActionTextColor(activity.getApplicationContext().getResources().getColor(R.color.blue)).show();



    }

    class GroupHolder extends RecyclerView.ViewHolder
    {
        TextView tittleText;
        TextView numberText;
        ConstraintLayout constraintLayout;
        ImageButton moreButton;

        EditText editTittle;
        ImageButton finishEdittingButton;

        public GroupHolder(@NonNull View itemView) {
            super(itemView);

            tittleText = itemView.findViewById(R.id.tittle_group_card);
            numberText = itemView.findViewById(R.id.number_group_card);
            constraintLayout = itemView.findViewById(R.id.constraint_group_card);
            moreButton = itemView.findViewById(R.id.more_button_group_card);

            editTittle = itemView.findViewById(R.id.edit_tittle_group_card);
            finishEdittingButton = itemView.findViewById(R.id.stop_editting_button_group_card);
        }
    }
}
