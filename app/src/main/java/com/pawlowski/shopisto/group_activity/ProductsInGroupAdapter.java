package com.pawlowski.shopisto.group_activity;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.google.android.material.snackbar.Snackbar;
import com.pawlowski.shopisto.R;
import com.pawlowski.shopisto.base.BaseActivity;
import com.pawlowski.shopisto.base.BaseSelectableAdapter;
import com.pawlowski.shopisto.database.DBHandler;
import com.pawlowski.shopisto.database.OnlineDBHandler;
import com.pawlowski.shopisto.edit_product_activity.EditProductActivity;
import com.pawlowski.shopisto.models.ProductModel;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ProductsInGroupAdapter extends BaseSelectableAdapter<ProductsInGroupAdapter.ProductHolder> implements ProductsInGroupItemViewMvc.ProductsInGroupItemButtonsClickListener {

    ArrayList<ProductModel>products = new ArrayList<>();

    private final BaseActivity activity;
    private final int groupId;
    boolean choosing = false;
    String groupKey;

    public ProductsInGroupAdapter(BaseActivity activity, int groupId, boolean choosing, String groupKey)
    {
        this.activity = activity;
        this.groupId = groupId;
        this.choosing = choosing;
        this.groupKey = groupKey;
    }

    @NonNull
    @Override
    public ProductHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ProductHolder(new ProductsInGroupItemViewMvc(LayoutInflater.from(parent.getContext()), parent));
    }

    @Override
    public void onBindViewHolder(@NonNull ProductsInGroupAdapter.ProductHolder holder, int position) {
        ProductModel currentProduct = products.get(position);
        holder.viewMvc.clearAllListeners();
        holder.viewMvc.bindProduct(currentProduct, isPositionSelected(position), choosing, position);
        holder.viewMvc.registerListener(this);
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public void setProducts(List<ProductModel> products)
    {
        this.products = new ArrayList<>(products);

        initNewSelections(products.size());

        notifyDataSetChanged();
    }

    public List<ProductModel>getSelectedProducts()
    {
        return getSelectedElements(products);
    }


    public void unselectProduct()
    {
        resetPositionsSelected();
    }

    public void deleteSelectedProducts(View view)
    {

        List<ProductModel> selectedProducts = getSelectedProducts();
        if(selectedProducts.size() > 0) {
            if(!activity.isOfflineModeOn())
                OnlineDBHandler.deleteProductsInGroup(selectedProducts, groupKey);
            DBHandler.getInstance(activity.getApplicationContext()).increaseGroupTimestamp(groupKey);
        }

        for(ProductModel p:selectedProducts)
        {

            DBHandler.getInstance(activity.getApplicationContext()).deleteProductFromGroup(p);
            products.remove(p);
            //positionsSelected.remove()
        }
        resetPositionsSelected();
        notifyDataSetChanged();

        if(products.size() == 0)
        {
            ((GroupActivity)activity).showNoProductsImage();
        }


        Snackbar.make(view, activity.getString(R.string.products_deleted_1)  + " " + selectedProducts.size() + " " + activity.getString(R.string.products_deleted_2), Snackbar.LENGTH_LONG).setAction(activity.getString(R.string.undo), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(products.size() == 0)
                {
                    ((GroupActivity)activity).hideNoProductsImage();
                }

                if(!activity.isOfflineModeOn())
                    OnlineDBHandler.addManyProductsInGroup(selectedProducts, groupKey);
                DBHandler.getInstance(activity.getApplicationContext()).increaseGroupTimestamp(groupKey);
                for(ProductModel p:selectedProducts)
                {

                    DBHandler.getInstance(activity.getApplicationContext()).insertProductToGroup(p, groupId);
                    products.add(p);
                }
                resetPositionsSelected();
                notifyDataSetChanged();
            }
        }).setActionTextColor(activity.getApplicationContext().getResources().getColor(R.color.blue)).show();


    }

    public ProductModel getSelectedProduct()
    {
        List<ProductModel> selectedProducts = getSelectedProducts();
        if(selectedProducts.size() > 0)
            return selectedProducts.get(0);
        else
        {
            return null;
        }
    }

    private void changeSelectionOfProduct(int index)
    {
        changeSelectionOfElement(index);
        notifyItemChanged(index);
        if(!choosing)
        {
            doSomethingWithMenuAfterSelecting();
        }
    }

    private void doSomethingWithMenuAfterSelecting()
    {
        int numberOfSelected = getNumberOfSelectedElements();
        if(numberOfSelected == 0)
        {
            ((GroupActivity)activity).setMenuInvisible();
        }
        else if (numberOfSelected == 1)
        {
            ((GroupActivity)activity).setMenuVisible();
        }
        else if (numberOfSelected == 2)
        {
            ((GroupActivity)activity).showOnlyDeleteMenuItem();
        }


    }

    private void resetPositionsSelected()
    {
        initNewSelections(products.size());
        ((GroupActivity)activity).setMenuInvisible();
    }

    public void unselectAllProducts()
    {
        unselectAllElementsAndNotify();
        ((GroupActivity)activity).setMenuInvisible();
    }


    public static void changeCategoryImageDependingOnCategory(ImageView imageView, int categoryId)
    {
        switch (categoryId)
        {
            case 0:
                imageView.setImageResource(R.drawable.food);
                break;
            case 1:
                imageView.setImageResource(R.drawable.canned_food);
                break;
            case 2:
                imageView.setImageResource(R.drawable.frozen);
                break;
            case 3:
                imageView.setImageResource(R.drawable.alcohol);
                break;
            case 4:
                imageView.setImageResource(R.drawable.bread);
                break;
            case 5:
                imageView.setImageResource(R.drawable.pasta);
                break;
            case 6:
                imageView.setImageResource(R.drawable.sauces);
                break;
            case 7:
                imageView.setImageResource(R.drawable.electronics_icon);
                break;
            case 8:
                imageView.setImageResource(R.drawable.meat_icon);
                break;
            case 9:
                imageView.setImageResource(R.drawable.clothes_icon);
                break;
            case 10:
                imageView.setImageResource(R.drawable.sweets_icon);
                break;
            case 11:
                imageView.setImageResource(R.drawable.cereals_meals_icon);
                break;
            case 12:
                imageView.setImageResource(R.drawable.fruits_and_vegetables_icon);
                break;
            case 13:
                imageView.setImageResource(R.drawable.drinks_icon);
                break;
            case 14:
                imageView.setImageResource(R.drawable.dairy_icon);
                break;
            case 15:
                imageView.setImageResource(R.drawable.ready_meals_icon);
                break;
            case 16:
                imageView.setImageResource(R.drawable.stationary);
                break;


            default:


        }
    }

    @Override
    public void onCardClick(ProductModel currentProduct, int currentPosition) {
        if(!choosing)
        {
            if(isSomethingSelected())
            {
                changeSelectionOfProduct(currentPosition);
            }
            else
            {
                EditProductActivity.launchFromGroups(activity, true, groupId, groupKey, currentProduct.getCategoryId(), currentProduct);
                activity.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
            }
        }
        else
        {
            changeSelectionOfProduct(currentPosition);
        }
    }

    @Override
    public void onCardLongClick(int currentPosition) {
        changeSelectionOfProduct(currentPosition);
    }


    class ProductHolder extends RecyclerView.ViewHolder
    {
        ProductsInGroupItemViewMvc viewMvc;

        public ProductHolder(@NonNull ProductsInGroupItemViewMvc viewMvc) {
            super(viewMvc.getRootView());

            this.viewMvc = viewMvc;
        }
    }
}
