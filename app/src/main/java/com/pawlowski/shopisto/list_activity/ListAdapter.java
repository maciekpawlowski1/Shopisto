package com.pawlowski.shopisto.list_activity;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.FirebaseDatabase;
import com.pawlowski.shopisto.R;
import com.pawlowski.shopisto.base.BaseSelectableAdapter;
import com.pawlowski.shopisto.database.DBHandler;
import com.pawlowski.shopisto.database.OnlineDBHandler;
import com.pawlowski.shopisto.models.ProductModel;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ListAdapter extends BaseSelectableAdapter<ListAdapter.ProductHolder> implements ListItemViewMvc.ListItemButtonsClickListener {

    ArrayList<ProductModel>products = new ArrayList<>();

    private final Activity activity;
    private final int listId;
    String listKey;
    boolean offlineMode = false;
    private final DBHandler dbHandler;


    ListAdapter(Activity activity, int listId, String listKey, boolean offlineMode, DBHandler dbHandler)
    {
        this.activity = activity;
        this.listId = listId;
        this.listKey = listKey;
        this.offlineMode = offlineMode;

        this.dbHandler = dbHandler;
    }

    @NonNull
    @Override
    public ProductHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        return new ProductHolder(new ListItemViewMvc(LayoutInflater.from(parent.getContext()), parent));
    }



    @Override
    public void onBindViewHolder(@NonNull ListAdapter.ProductHolder holder, int position) {
        ProductModel currentProduct = products.get(position);
        holder.viewMvc.clearAllListeners();
        holder.viewMvc.bindProduct(currentProduct, position, isPositionSelected(position));
        holder.viewMvc.registerListener(this);
    }

    @Override
    public int getItemCount() {
        return products.size();
    }


    public int getNewPositionForProduct(String tittle, boolean newSelected, int categoryId)
    {
        if(newSelected)
        {
            for(int i=0;i<products.size();i++)
            {
                ProductModel p = products.get(i);
                if(p.isSelected() && p.getCategoryId() <= categoryId)
                {
                    if(p.getCategoryId() == categoryId)
                    {
                        if(p.getTittle().compareTo(tittle) > 0)
                            return i;
                    }
                    else
                    {
                        return i;
                    }

                }
            }
            return products.size(); //If every product is not selected
        }
        else
        {

            boolean everythingSelected = true;
            int border = products.size();
            for(int i=0;i<products.size();i++)
            {
                ProductModel p = products.get(i);
                if(!p.isSelected())
                {
                    everythingSelected = false;
                    if(p.getCategoryId() <= categoryId)
                    {
                        if(p.getCategoryId() == categoryId)
                        {
                            if(p.getTittle().compareTo(tittle) > 0)
                                return i;
                        }
                        else
                        {
                            return i;
                        }
                    }
                }
                else
                {
                    border = i;
                    break;
                }

            }
            if(everythingSelected)
                return 0;
            else
                return border;
        }

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

    public void selectProduct(int position)
    {
        int numberSelected = getNumberOfSelectedElements();
        if(numberSelected == 0)
            ((ListActivity)activity).setMenuVisible();
        else if(numberSelected == 1)
            ((ListActivity)activity).showOnlyDeleteItem();

        selectElement(position);
        notifyItemChanged(position);


    }

    public void unselectProduct(int position)
    {
        int numberSelected = getNumberOfSelectedElements();
        if(numberSelected == 1)
            ((ListActivity)activity).setMenuInvisible();
        else if(numberSelected == 2)
            ((ListActivity)activity).setMenuVisible();

        unselectElement(position);
        notifyItemChanged(position);


    }

    public void unselectAllProducts()
    {
        unselectAllElements();
        notifyDataSetChanged();
        ((ListActivity)activity).setMenuInvisible();
    }



    public void deleteSelectedProducts(View view)
    {
        List<ProductModel> selectedProducts = getSelectedElements(products);
        List<Integer> selectedProductsPositions = getSelectedPositions();

        for(int i=selectedProducts.size()-1;i>=0;i--)
        {
            ProductModel product = selectedProducts.get(i);
            if(!offlineMode)
                OnlineDBHandler.deleteProductWithNotifying(listKey, product.getTittle(), ((ListActivity)activity).getFriendsFromList());
            products.remove(product);
            deleteElement(selectedProductsPositions.get(i));

            dbHandler.deleteProduct(product, listId);
            notifyItemRemoved(selectedProductsPositions.get(i));
        }
        notifyItemRangeChanged(0, products.size());

        ((ListActivity)activity).setMenuInvisible();

        if(products.size() == 0)
        {
            ((ListActivity)activity).showNoProductsImage();
        }



        Snackbar.make(view, activity.getString(R.string.product_deleted), Snackbar.LENGTH_LONG).setAction(activity.getString(R.string.undo), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(products.size() == 0)
                {
                    ((ListActivity)activity).hideNoProductsImage();
                }
                ((ListActivity)activity).resetTimersAndStartStop();
                FirebaseDatabase.getInstance().goOnline();
                for(int i=0;i<selectedProducts.size();i++)
                {
                    ProductModel product = selectedProducts.get(i);
                    if(!offlineMode)
                        OnlineDBHandler.addProductWithDescription(listKey, product, ((ListActivity)activity).getFriendsFromList());
                    int productPosition = selectedProductsPositions.get(i);
                    products.add(productPosition, product);
                    addElement(productPosition, false);
                    dbHandler.insertProduct(product, listId);
                    notifyItemInserted(productPosition);
                }

                notifyItemRangeChanged(0, products.size());
            }
        }).setActionTextColor(activity.getApplicationContext().getResources().getColor(R.color.blue)).show();

    }

    public void doGoOnlineForAMoment()
    {
        if(!offlineMode)
        {
            FirebaseDatabase.getInstance().goOnline();
            ((ListActivity)activity).resetTimersAndStartStop();
        }

    }

    @Override
    public void onCardClick(int currentPosition) {
        if(isSomethingSelected())
        {
            if(isPositionSelected(currentPosition))
            {
                unselectProduct(currentPosition);
            }
            else
                selectProduct(currentPosition);
        }
    }

    @Override
    public void onCardLongClick(int currentPosition) {

        if(!isPositionSelected(currentPosition))
        {
            selectProduct(currentPosition);
        }
        else
        {
            unselectProduct(currentPosition);
        }
    }

    @Override
    public void onConstraintClick(int currentPosition) {
        if(isSomethingSelected())
        {
            if(isPositionSelected(currentPosition))
            {
                unselectProduct(currentPosition);
            }
            else
                selectProduct(currentPosition);
        }
    }

    @Override
    public void onImageClick(ProductModel currentProduct) {
        if(!isSomethingSelected())
            ((ListActivity)activity).editAction(false, currentProduct);
    }

    @Override
    public void onCheckBoxClick(ProductModel currentProduct, ListItemViewMvc viewMvc) {
        if(isSomethingSelected())
        {
            unselectAllProducts();
            viewMvc.changeCheckedOfCheckBox();

            return;
        }

        viewMvc.animateCheckBox();

        boolean isSelected = viewMvc.isCheckBoxChecked();
        currentProduct.setSelected(isSelected);

        if(!offlineMode)
        {
            doGoOnlineForAMoment();
            OnlineDBHandler.setSelectionOfProduct(listKey, currentProduct, ((ListActivity)activity).getFriendsFromList());
        }



        dbHandler.updateListNumberSelected(listId, isSelected);
        dbHandler.updateProduct(currentProduct);
        int from;
        int to;
        if(products.size() > 1)
        {

            from = products.indexOf(currentProduct);
            products.remove(from);
            to = getNewPositionForProduct(currentProduct.getTittle(), isSelected, currentProduct.getCategoryId());

            products.add(to, currentProduct);

            notifyItemMoved(from, to);
            if(from > to)
            {
                notifyItemRangeChanged(to, from-to+1);
            }
            else
            {
                notifyItemRangeChanged(from, to-from+1);
            }

        }
    }


    class ProductHolder extends RecyclerView.ViewHolder
    {
        ListItemViewMvc viewMvc;
        public ProductHolder(@NonNull ListItemViewMvc viewMvc) {
            super(viewMvc.getRootView());
            this.viewMvc = viewMvc;
        }
    }
}
