package com.pawlowski.shopisto;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.BounceInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.FirebaseDatabase;
import com.pawlowski.shopisto.database.DBHandler;
import com.pawlowski.shopisto.database.OnlineDBHandler;
import com.pawlowski.shopisto.models.ProductModel;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class ListAdapter extends RecyclerView.Adapter<ListAdapter.ProductHolder> {

    ArrayList<ProductModel>products = new ArrayList<>();

    private Activity activity;
    private int listId;
    int positionSelected = -1;
    String listKey;
    boolean offlineMode = false;
    ArrayList<Boolean> positionsSelected = new ArrayList<>();


    ListAdapter(Activity activity, int listId, String listKey, boolean offlineMode)
    {
        this.activity = activity;
        this.listId = listId;
        this.listKey = listKey;
        this.offlineMode = offlineMode;

    }

    @NonNull
    @Override
    public ProductHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_in_list_card,
                parent, false);
        return new ProductHolder(view);
    }



    @Override
    public void onBindViewHolder(@NonNull ListAdapter.ProductHolder holder, int position) {
        ProductModel currentProduct = products.get(position);
        holder.tittleText.setText(currentProduct.getTittle());
        /*holder.tittleText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("pozycja", position+"");
            }
        });*/


        holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if(!positionsSelected.get(position))
                {
                    selectProduct(position);
                }
                else
                {
                    unselectProduct(position);
                }

                return true;
            }
        });

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isSomethingSelected())
                {
                    if(positionsSelected.get(position))
                    {
                        unselectProduct(position);
                    }
                    else
                        selectProduct(position);
                }
            }
        });

        holder.constraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isSomethingSelected())
                {
                    if(positionsSelected.get(position))
                    {
                        unselectProduct(position);
                    }
                    else
                        selectProduct(position);
                }
            }
        });


        holder.descriptionText.setText(currentProduct.getDescription());
        holder.numberText.setText(currentProduct.getNumber()+"");
        holder.checkBox.setChecked(currentProduct.isSelected());
        if(currentProduct.isSelected())
        {
            holder.cardView.setCardBackgroundColor(activity.getResources().getColor(R.color.card_backgroubd_color2));
            holder.imageView.setAlpha(0.6f);
            holder.tittleText.setAlpha(0.7f);
            holder.numberText.setAlpha(0.7f);
            //holder.tittleText.set
        }
        else
        {
            holder.cardView.setCardBackgroundColor(activity.getResources().getColor(R.color.white));
            holder.imageView.setAlpha(1.0f);
            holder.tittleText.setAlpha(1.0f);
            holder.numberText.setAlpha(1.0f);
        }


        if(positionsSelected.get(position))
        {
            holder.cardView.setCardBackgroundColor(activity.getResources().getColor(R.color.selected_color));
            holder.checkBox.setEnabled(false);
        }
        else
        {
            holder.checkBox.setEnabled(true);
        }
        //Log.d("test", currentProduct.isSelected()+"");
        holder.checkBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {



                if(isSomethingSelected())
                {
                    unselectAllProducts();

                    holder.checkBox.setChecked(!holder.checkBox.isChecked());
                    return;
                }

                ScaleAnimation scaleAnimation;
                BounceInterpolator bounceInterpolator;
                scaleAnimation = new ScaleAnimation(0.7f, 1.0f, 0.7f, 1.0f, Animation.RELATIVE_TO_SELF, 0.7f, Animation.RELATIVE_TO_SELF, 0.7f);
                scaleAnimation.setDuration(500);
                bounceInterpolator = new BounceInterpolator();
                scaleAnimation.setInterpolator(bounceInterpolator);
                holder.checkBox.startAnimation(scaleAnimation);
                holder.cardView.startAnimation(scaleAnimation);

                boolean isSelected = holder.checkBox.isChecked();
                //Log.d("selected", isSelected+"");
                currentProduct.setSelected(isSelected);

                if(!offlineMode)
                {
                    doGoOnlineForAMoment();
                    OnlineDBHandler.setSelectionOfProduct(listKey, currentProduct, ((ListActivity)activity).getFriendsFromList());
                }



                DBHandler.getInstance(activity.getApplicationContext()).updateListNumberSelected(listId, isSelected);
                DBHandler.getInstance(activity.getApplicationContext()).updateProduct(currentProduct);
                int from;
                int to;
                if(products.size() > 1)
                {
                    /*if(isSelected)
                    {
                        //from = position;
                        from = products.indexOf(currentProduct);
                        //to = products.size()-1;
                        to = getNewPositionForProduct(currentProduct.getTittle(), true, currentProduct.getCategoryId());
                        products.add(currentProduct);
                        products.remove(currentProduct);
                    }
                    else
                    {
                        from = products.indexOf(currentProduct);
                        to = getNewPositionForProduct(currentProduct.getTittle(), false, currentProduct.getCategoryId());
                        products.remove(currentProduct);
                        products.add(0, currentProduct);
                    }*/
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




                //Log.d("checkboxy", currentProduct.getId()+"");
            }
        });

        ProductsInGroupAdapter.changeCategoryImageDependingOnCategory(holder.imageView, currentProduct.getCategoryId());


        holder.imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!isSomethingSelected())
                    ((ListActivity)activity).editAction(false, currentProduct);
            }
        });



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
            /*for(int i=products.size()-1;i>0;i--)
            {
                ProductModel p = products.get(i);
                if(!p.isSelected() && p.getCategoryId() <= categoryId)
                {
                    if(p.getCategoryId() == categoryId)
                    {
                        if(p.getTittle().compareTo(tittle) > 0)
                        {
                            return i;
                        }
                    }
                    else
                    {
                        ProductModel previousProduct = products.get(i-1);
                        int previousId = previousProduct.getCategoryId();
                        if(previousId != categoryId)
                            return i;
                        else if(previousProduct.getTittle().compareTo(tittle) < 0)
                        {
                            return i;
                        }
                    }

                }
            }
            return 0;*/
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
        positionsSelected.clear();
        for(int i=0;i<products.size();i++)
        {
            positionsSelected.add(false);
        }

        notifyDataSetChanged();
    }

    public ArrayList<ProductModel>getSelectedProducts()
    {
        ArrayList<ProductModel> selectedProducts = new ArrayList<>();
        for(int i=0;i<positionsSelected.size();i++)
        {
            if(positionsSelected.get(i))
            {
                selectedProducts.add(products.get(i));
            }
        }
        return selectedProducts;
    }

    public ArrayList<Integer>getSelectedPositions()
    {
        ArrayList<Integer> selectedPositions = new ArrayList<>();
        for(int i=0;i<positionsSelected.size();i++)
        {
            if(positionsSelected.get(i))
            {
                selectedPositions.add(i);
            }
        }
        return selectedPositions;
    }

    public int getNumberOfSelected()
    {
        int number = 0;
        for(int i=0;i<positionsSelected.size();i++)
        {
            if(positionsSelected.get(i))
            {
                number++;
            }
        }
        return number;
    }

    public boolean isSomethingSelected()
    {
        for(int i=0;i<positionsSelected.size();i++)
        {
            if(positionsSelected.get(i))
            {
                return true;
            }
        }
        return false;
    }





    public void selectProduct(int position)
    {
        int numberSelected = getNumberOfSelected();
        if(numberSelected == 0)
            ((ListActivity)activity).setMenuVisible();
        else if(numberSelected == 1)
            ((ListActivity)activity).showOnlyDeleteItem();

        positionsSelected.set(position, true);
        notifyItemChanged(position);


    }

    public void unselectProduct(int position)
    {
        int numberSelected = getNumberOfSelected();
        if(numberSelected == 1)
            ((ListActivity)activity).setMenuInvisible();
        else if(numberSelected == 2)
            ((ListActivity)activity).setMenuVisible();

        positionsSelected.set(position, false);
        notifyItemChanged(position);


    }

    public void unselectAllProducts()
    {
        for(int i=0;i<positionsSelected.size();i++)
        {
            positionsSelected.set(i, false);
        }
        notifyDataSetChanged();
        ((ListActivity)activity).setMenuInvisible();
    }



    public void deleteSelectedProducts(View view)
    {
        ArrayList<ProductModel> selectedProducts = getSelectedProducts();
        ArrayList<Integer> selectedProductsPositions = getSelectedPositions();

        for(int i=selectedProducts.size()-1;i>=0;i--)
        {
            ProductModel product = selectedProducts.get(i);
            if(!offlineMode)
                OnlineDBHandler.deleteProductWithNotifying(listKey, product.getTittle(), ((ListActivity)activity).getFriendsFromList());
            products.remove(product);
            positionsSelected.remove(selectedProductsPositions.get(i).intValue());

            DBHandler.getInstance(activity.getApplicationContext()).deleteProduct(product, listId);
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
                    positionsSelected.add(productPosition, false);
                    DBHandler.getInstance(activity.getApplicationContext()).insertProduct(product, listId);
                    notifyItemInserted(productPosition);
                }

                notifyItemRangeChanged(0, products.size());
            }
        }).setActionTextColor(activity.getApplicationContext().getResources().getColor(R.color.blue)).show();

    }

    public ProductModel getSelectedProdukt()
    {
        if(positionSelected != -1)
        {
            return products.get(positionSelected);
        }
        else
        {
            return null;
        }
    }

    public void doGoOnlineForAMoment()
    {
        if(!offlineMode)
        {
            FirebaseDatabase.getInstance().goOnline();
            ((ListActivity)activity).resetTimersAndStartStop();
        }

    }



    class ProductHolder extends RecyclerView.ViewHolder
    {
        TextView tittleText;
        TextView descriptionText;
        TextView numberText;
        ImageView imageView;
        CheckBox checkBox;
        CardView cardView;
        ConstraintLayout constraintLayout;
        public ProductHolder(@NonNull View itemView) {
            super(itemView);
            tittleText = itemView.findViewById(R.id.tittle_list_card);
            descriptionText = itemView.findViewById(R.id.description_list_card);
            numberText = itemView.findViewById(R.id.number_list_card);
            imageView = itemView.findViewById(R.id.image_list_card);
            checkBox = itemView.findViewById(R.id.check_box_list_card);
            cardView = itemView.findViewById(R.id.card_view_product_in_list_card);
            constraintLayout = itemView.findViewById(R.id.constaint_layout_product_in_list_card);
        }
    }
}
