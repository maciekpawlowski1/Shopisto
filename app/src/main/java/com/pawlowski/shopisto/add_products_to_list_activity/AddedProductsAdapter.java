package com.pawlowski.shopisto.add_products_to_list_activity;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.FirebaseDatabase;
import com.pawlowski.shopisto.R;
import com.pawlowski.shopisto.base.BaseObservableViewMvc;
import com.pawlowski.shopisto.database.DBHandler;
import com.pawlowski.shopisto.database.OnlineDBHandler;
import com.pawlowski.shopisto.list_activity.ListActivity;
import com.pawlowski.shopisto.models.ProductModel;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class AddedProductsAdapter extends RecyclerView.Adapter<AddedProductsAdapter.CardHolder> implements AddedProductsItemViewMvc.AddedProductsItemButtonsClickListener, AddedProductsNewItemViewMvc.AddedProductsNewItemButtonsClickListener {

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

    private final Activity activity;
    private final int listId;
    String listKey;
    boolean suggesting = false;
    String searched = "";

    @NonNull
    @Override
    public CardHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if(viewType == 1000)
            return new CardHolder(new AddedProductsItemViewMvc(LayoutInflater.from(parent.getContext()), parent));
        else
            return new CardHolder(new AddedProductsNewItemViewMvc(LayoutInflater.from(parent.getContext()), parent));
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

        if(holder.normalItemViewMvc != null)
        {
            holder.normalItemViewMvc.clearAllListeners();
        }
        if(holder.newItemViewMvc != null)
        {
            holder.newItemViewMvc.clearAllListeners();
        }
        ProductModel currentProduct;
        if(suggesting)
        {
            currentProduct = getProductByTittle(suggestedProducts.get(position));
            if(currentProduct == null) {
                String tittleString = suggestedProducts.get(position);
                holder.newItemViewMvc.bindItem(tittleString, searched);
                holder.newItemViewMvc.registerListener(this);
                return;
            }
        }
        else
        {
            currentProduct = addedProducts.get(position);
        }
        holder.normalItemViewMvc.bindProduct(currentProduct, position, suggesting, searched);
        holder.normalItemViewMvc.registerListener(this);
    }

    public ProductModel getProductByTittle(String tittle)
    {
        if(tittle.length()==0)
            return null;
        for(int i=0;i<addedProducts.size();i++)
        {
            if(addedProducts.get(i).getTittle().equalsIgnoreCase(tittle))
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

    public void addProductAtBeginning(ProductModel product)
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
            if(s.toLowerCase().contains(searchedText.toLowerCase()) && !s.equalsIgnoreCase(searchedText))
            {
                suggestedProducts.add(s);
            }

        }


    }

    public boolean isAnyTittleWithThisTittle(String tittle)
    {
        for(ProductModel p:addedProducts)
        {
            if(p.getTittle().equalsIgnoreCase(tittle))
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

    @Override
    public void onPlusButtonClick(ProductModel currentProduct, int currentPosition, AddedProductsItemViewMvc viewMvc) {
        ((AddProductsToListActivity)activity).resetTimer();
        FirebaseDatabase.getInstance().goOnline();
        int currentNumber = currentProduct.getNumber();
        currentProduct.setNumber(currentNumber + 1);
        if(!offlineMode)
            OnlineDBHandler.setNumberOfProduct(listKey, currentProduct, ((AddProductsToListActivity)activity).getFriendsFromList());
        notifyItemChanged(currentPosition);
        DBHandler.getInstance(activity.getApplicationContext()).updateProduct(currentProduct);
    }

    @Override
    public void onTrashOrMinusClick(ProductModel currentProduct, int currentPosition, AddedProductsItemViewMvc viewMvc) {
        int currentNumber = currentProduct.getNumber();
        if(currentNumber == 1)
        {
            if(!suggesting)
            {
                deleteProduct(currentPosition, currentProduct, viewMvc.getRootView());
            }
            else
            {
                //Method when suggesting is on
                deleteProductWhenSuggesting(currentPosition, currentProduct, viewMvc.getRootView());
            }
        }
        else
        {
            ((AddProductsToListActivity)activity).resetTimer();
            FirebaseDatabase.getInstance().goOnline();
            currentProduct.setNumber(currentNumber - 1);
            if(!offlineMode)
                OnlineDBHandler.setNumberOfProduct(listKey, currentProduct, ((AddProductsToListActivity)activity).getFriendsFromList());
            notifyItemChanged(currentPosition);
            DBHandler.getInstance(activity.getApplicationContext()).updateProduct(currentProduct);
        }
    }

    @Override
    public void onSuggestionItemClick(String tittleString) {
        ((AddProductsToListActivity)activity).addProduct(tittleString);
    }


    class CardHolder extends RecyclerView.ViewHolder
    {
        AddedProductsItemViewMvc normalItemViewMvc;
        AddedProductsNewItemViewMvc newItemViewMvc;

        public <ListenerType>CardHolder(@NonNull BaseObservableViewMvc<ListenerType> viewMvc) {
            super(viewMvc.getRootView());

            if(viewMvc instanceof AddedProductsItemViewMvc)
            {
                normalItemViewMvc = (AddedProductsItemViewMvc) viewMvc;
            }
            else if(viewMvc instanceof AddedProductsNewItemViewMvc)
            {
                newItemViewMvc = (AddedProductsNewItemViewMvc) viewMvc;
            }
            else
            {
                throw new RuntimeException("Wrong viewMvc class!");
            }
        }
    }
}
