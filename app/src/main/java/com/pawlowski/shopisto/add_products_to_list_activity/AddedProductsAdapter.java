package com.pawlowski.shopisto.add_products_to_list_activity;

import android.app.Activity;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.FirebaseDatabase;
import com.pawlowski.shopisto.R;
import com.pawlowski.shopisto.database.DBHandler;
import com.pawlowski.shopisto.database.OnlineDBHandler;
import com.pawlowski.shopisto.list_activity.ListActivity;
import com.pawlowski.shopisto.models.ProductModel;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class AddedProductsAdapter extends RecyclerView.Adapter<AddedProductsAdapter.CardHolder> {

    ArrayList<ProductModel>addedProducts = new ArrayList<>();
    ArrayList<String>suggestedProducts = new ArrayList<>();
    boolean offlineMode = false;

    AddedProductsAdapter(Activity activity, int listId, String listKey, boolean offlineMode)
    {
        this.activity = activity;
        this.listId = listId;
        this.listKey = listKey;
        this.offlineMode = offlineMode;
    }

    private Activity activity;
    private int listId;
    String listKey;
    boolean suggesting = false;
    String searched = "";

    @NonNull
    @Override
    public CardHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if(viewType == 1000)
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.added_product_card,
                parent, false);
        else
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.added_product_new_card,
                    parent, false);
        return new CardHolder(view);
    }

    @Override
    public int getItemViewType(int position) {
        if(!suggesting)
            return 1000;
        else
        {
            if(getProductByTittle(suggestedProducts.get(position)) != null)
                return 1000;

        }


        return super.getItemViewType(position);
    }

    @Override
    public void onBindViewHolder(@NonNull AddedProductsAdapter.CardHolder holder, int position) {
        ProductModel currentProduct;
        if(suggesting)
        {
            currentProduct = getProductByTittle(suggestedProducts.get(position));
            if(currentProduct == null) {
                String tittleString = suggestedProducts.get(position);
                SpannableStringBuilder s = new SpannableStringBuilder(tittleString);
                int fragmentStart = tittleString.toLowerCase().indexOf(searched.toLowerCase());
                if(fragmentStart != -1)
                    s.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), fragmentStart, fragmentStart+searched.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                holder.tittleSugestionText.setText(s);
                holder.constraintSuggestions.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((AddProductsToListActivity)activity).addProduct(holder.tittleSugestionText.getText().toString());
                    }
                });
                holder.addButtonSuggestions.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ((AddProductsToListActivity)activity).addProduct(holder.tittleSugestionText.getText().toString());
                    }
                });
                return;
            }



        }
        else
        {
            currentProduct = addedProducts.get(position);
        }


        if(!suggesting)
            holder.tittleText.setText(currentProduct.getTittle());
        else
        {
            String tittleString = currentProduct.getTittle();
            SpannableStringBuilder s = new SpannableStringBuilder(tittleString);
            int fragmentStart = tittleString.toLowerCase().indexOf(searched.toLowerCase());
            if(fragmentStart != -1)
                s.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), fragmentStart, fragmentStart+searched.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            holder.tittleText.setText(s);
        }


        holder.numberText.setText(currentProduct.getNumber()+"");
        if(currentProduct.getNumber() == 1)
        {
            holder.trashOrMinusButton.setImageResource(R.drawable.delete_icon);
        }
        else
        {
            holder.trashOrMinusButton.setImageResource(R.drawable.minus_icon);
        }

        /*holder.tittleText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("Position", position+"");
            }
        });*/

        holder.plusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                ((AddProductsToListActivity)activity).resetTimer();
                FirebaseDatabase.getInstance().goOnline();
                int currentNumber = currentProduct.getNumber();
                currentProduct.setNumber(currentNumber + 1);
                if(!offlineMode)
                    OnlineDBHandler.setNumberOfProduct(listKey, currentProduct, ((AddProductsToListActivity)activity).getFriendsFromList());
                notifyItemChanged(position);
                DBHandler.getInstance(activity.getApplicationContext()).updateProduct(currentProduct);
            }
        });

        holder.trashOrMinusButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int currentNumber = currentProduct.getNumber();
                if(currentNumber == 1)
                {
                    if(!suggesting)
                    {
                        deleteProduct(position, currentProduct, holder.trashOrMinusButton);
                    }
                    else
                    {
                        //Method when suggesting is on
                        deleteProductWhenSuggesting(position, currentProduct, holder.trashOrMinusButton);
                    }
                }
                else
                {
                    ((AddProductsToListActivity)activity).resetTimer();
                    FirebaseDatabase.getInstance().goOnline();
                    currentProduct.setNumber(currentNumber - 1);
                    if(!offlineMode)
                        OnlineDBHandler.setNumberOfProduct(listKey, currentProduct, ((AddProductsToListActivity)activity).getFriendsFromList());
                    notifyItemChanged(position);
                    DBHandler.getInstance(activity.getApplicationContext()).updateProduct(currentProduct);
                }

            }
        });

    }

    public ProductModel getProductByTittle(String tittle)
    {
        if(tittle.length()==0)
            return null;
        for(int i=0;i<addedProducts.size();i++)
        {
            if(addedProducts.get(i).getTittle().toLowerCase().equals(tittle.toLowerCase()))
                return addedProducts.get(i);
        }
        return null;
    }

    private void deleteProduct(final int position, ProductModel product, View view)
    {
        ((AddProductsToListActivity)activity).resetTimer();
        FirebaseDatabase.getInstance().goOnline();
        addedProducts.remove(position);
        DBHandler.getInstance(activity.getApplicationContext()).deleteProduct(product, listId);
        if(!offlineMode)
            OnlineDBHandler.deleteProductWithNotifying(listKey, product.getTittle(), ((AddProductsToListActivity)activity).getFriendsFromList());


        notifyItemRemoved(position);
        notifyItemRangeChanged(0, addedProducts.size());
        Snackbar.make(view, activity.getString(R.string.product_deleted), Snackbar.LENGTH_LONG).setAction(activity.getString(R.string.undo), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setClickable(false);
                if(addedProducts.size() > position && addedProducts.get(position).getId()==product.getId()) //To avoid double action
                {
                    return;
                }
                addedProducts.add(position, product);
                if(!offlineMode)
                    OnlineDBHandler.addProductWithDescription(listKey, product, ((ListActivity)activity).getFriendsFromList());
                DBHandler.getInstance(activity.getApplicationContext()).insertProduct(product, listId);

                notifyItemInserted(position);
                notifyItemRangeChanged(0, addedProducts.size());
            }
        }).setActionTextColor(activity.getApplicationContext().getResources().getColor(R.color.blue)).show();
    }

    private void deleteProductWhenSuggesting(final int position, ProductModel product, View view)
    {
        ((AddProductsToListActivity)activity).resetTimer();
        FirebaseDatabase.getInstance().goOnline();

        addedProducts.remove(product);
        if(!offlineMode)
            OnlineDBHandler.deleteProductWithNotifying(listKey, product.getTittle(), ((AddProductsToListActivity)activity).getFriendsFromList());
        DBHandler.getInstance(activity.getApplicationContext()).deleteProduct(product, listId);
        notifyItemChanged(position);
        //notifyItemRangeChanged(0, addedProducts.size());


    }



    public void setProducts(List<ProductModel> products)
    {
        addedProducts = new ArrayList<>(products);
        notifyDataSetChanged();
    }

    public void addProduct(ProductModel product)
    {
        addedProducts.add(product);
        notifyItemInserted(addedProducts.size()-1);
        notifyItemRangeChanged(0, addedProducts.size());
    }

    public void addProductAtBegining(ProductModel product)
    {
        addedProducts.add(0, product);
        notifyItemInserted(0);
        notifyItemRangeChanged(0, addedProducts.size());
    }


    public void turnOnSuggesting(String searchedText)
    {
        searched = searchedText;

        suggesting = true;
        prepareSuggestions(searchedText);
        notifyDataSetChanged();
    }

    public void turnOffSuggesting()
    {
        suggesting = false;
        notifyDataSetChanged();
    }

    public void prepareSuggestions(String searchedText)
    {
        suggestedProducts = new ArrayList<>();
        suggestedProducts.add(0, searchedText);
        ArrayList<String> allSuggestions = DBHandler.getInstance(activity.getApplicationContext()).getAllTittlesOfProducts();
        for(int i=0;i<allSuggestions.size();i++)
        {
            String s = allSuggestions.get(i);
            if(s.toLowerCase().contains(searchedText.toLowerCase()) && !s.toLowerCase().equals(searchedText.toLowerCase()))
            {
                suggestedProducts.add(s);
            }

        }


    }

    public boolean isAnyTittleWithThisTittle(String tittle)
    {
        for(ProductModel p:addedProducts)
        {
            if(p.getTittle().toLowerCase().equals(tittle.toLowerCase()))
                return true;
        }
        return false;
    }



    @Override
    public int getItemCount() {
        if(!suggesting)
            return addedProducts.size();
        else return suggestedProducts.size();
    }


    class CardHolder extends RecyclerView.ViewHolder
    {
        ImageButton plusButton;
        ImageButton trashOrMinusButton;
        TextView tittleText;
        TextView numberText;
        TextView tittleSugestionText;
        ConstraintLayout constraintSuggestions;
        Button addButtonSuggestions;
        public CardHolder(@NonNull View itemView) {
            super(itemView);

            plusButton = itemView.findViewById(R.id.plus_image_button_added_card);
            trashOrMinusButton = itemView.findViewById(R.id.trash_or_minus_image_button_added_card);
            tittleText = itemView.findViewById(R.id.tittle_added_card);
            numberText = itemView.findViewById(R.id.number_added_card);

            //Suggestions
            tittleSugestionText = itemView.findViewById(R.id.tittle_added_new_card);
            constraintSuggestions = itemView.findViewById(R.id.constraint_added_new_card);
            addButtonSuggestions = itemView.findViewById(R.id.add_added_new_card);
        }
    }
}
