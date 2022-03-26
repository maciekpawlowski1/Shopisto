package com.pawlowski.shopisto.group_activity;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.pawlowski.shopisto.EditProductActivity;
import com.pawlowski.shopisto.R;
import com.pawlowski.shopisto.base.BaseActivity;
import com.pawlowski.shopisto.database.DBHandler;
import com.pawlowski.shopisto.database.OnlineDBHandler;
import com.pawlowski.shopisto.models.ProductModel;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class ProductsInGroupAdapter extends RecyclerView.Adapter<ProductsInGroupAdapter.ProductHolder> {

    ArrayList<ProductModel>products = new ArrayList<>();

    private BaseActivity activity;
    private int groupId;
    int positionSelected = -1;
    boolean choosing = false;
    String groupKey;
    ArrayList<Boolean>positionsSelected = new ArrayList<>();

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
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_in_group_card,
                parent, false);
        return new ProductHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductsInGroupAdapter.ProductHolder holder, int position) {
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
                if(!choosing)
                {
                    changeSelectionOfProduct(position);
                    /*if(getHowManySelected() == 0)
                    {

                    }
                    else
                    {

                    }*/
                    /*if(positionSelected != position)
                    {
                        selectProduct(position);
                    }
                    else
                    {
                        unselectProduct();
                    }*/



                    return true;
                }
                else
                {
                    return false;
                }

            }
        });

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!choosing)
                {
                    if(getHowManySelected() > 0)
                    {
                        changeSelectionOfProduct(position);
                    }
                    else
                    {
                        Intent i = new Intent(activity, EditProductActivity.class);
                        i.putExtra("group_id", groupId);
                        i.putExtra("product", currentProduct);
                        i.putExtra("groups", true);
                        i.putExtra("groupKey", groupKey);
                        i.putExtra("category_id", currentProduct.getCategoryId());
                        activity.startActivity(i);
                        activity.overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
                    }
                }
                else
                {
                    changeSelectionOfProduct(position);


                }

            }
        });

        /*holder.constraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!choosing)
                {
                    if(positionSelected != -1)
                    {
                        unselectProduct();
                    }
                }
                else
                {

                }

            }
        });*/


        holder.descriptionText.setText(currentProduct.getDescription());
        holder.numberText.setText(currentProduct.getNumber()+"");

        holder.cardView.setCardBackgroundColor(activity.getResources().getColor(R.color.white));



        if(positionsSelected.get(position).booleanValue())
        {
            holder.cardView.setCardBackgroundColor(activity.getResources().getColor(R.color.selected_color));
        }

        changeCategoryImageDependingOnCategory(holder.imageView, currentProduct.getCategoryId());


        //Log.d("test", currentProduct.isSelected()+"");






    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public void setProducts(List<ProductModel> products)
    {
        this.products = new ArrayList<>(products);


        positionsSelected.clear();
        for(int i=0;i<products.size();i++)
        {
            positionsSelected.add(choosing);
        }



        notifyDataSetChanged();
    }

    public ArrayList<ProductModel>getSelectedProducts()
    {
        ArrayList<ProductModel>selectedProducts = new ArrayList<>();
        for(int i=0;i<products.size();i++)
        {
            if(positionsSelected.get(i))
                selectedProducts.add(products.get(i));
        }
        return selectedProducts;
    }

    public int getHowManySelected()
    {
        int numberOfSelected = 0;
        for(Boolean b:positionsSelected)
        {
            if(b.booleanValue())
                numberOfSelected++;
        }
        return numberOfSelected;
    }

    private void selectProduct(int position)
    {
        if(positionSelected != -1)
            notifyItemChanged(positionSelected);
        positionSelected = position;
        notifyItemChanged(position);
        //((GroupActivity)activity).setMenuVisible();

    }

    public void unselectProduct()
    {
        resetPositionsSelected();


        //((GroupActivity)activity).setMenuInvisible();
    }

    /*public boolean isProductSelected()
    {
        return positionSelected != -1;
    }*/

    public void deleteSelectedProducts(View view)
    {

        List<ProductModel> selectedPoducts = getSelectedProducts();
        if(selectedPoducts.size() > 0) {
            if(!activity.isOfflineModeOn())
                OnlineDBHandler.deleteProductsInGroup(selectedPoducts, groupKey);
            DBHandler.getInstance(activity.getApplicationContext()).increaseGroupTimestamp(groupKey);
        }

        for(ProductModel p:selectedPoducts)
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


        Snackbar.make(view, activity.getString(R.string.products_deleted_1)  + " " + selectedPoducts.size() + " " + activity.getString(R.string.products_deleted_2), Snackbar.LENGTH_LONG).setAction(activity.getString(R.string.undo), new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(products.size() == 0)
                {
                    ((GroupActivity)activity).hideNoProductsImage();
                }

                if(!activity.isOfflineModeOn())
                    OnlineDBHandler.addManyProductsInGroup(selectedPoducts, groupKey);
                DBHandler.getInstance(activity.getApplicationContext()).increaseGroupTimestamp(groupKey);
                for(ProductModel p:selectedPoducts)
                {

                    DBHandler.getInstance(activity.getApplicationContext()).insertProductToGroup(p, groupId);
                    products.add(p);
                    //positionsSelected.remove()
                }
                resetPositionsSelected();
                notifyDataSetChanged();
            }
        }).setActionTextColor(activity.getApplicationContext().getResources().getColor(R.color.blue)).show();

        /*if(positionSelected != -1)
        {
            ProductModel product = products.get(positionSelected);
            products.remove(positionSelected);
            notifyItemRemoved(positionSelected);
            DBHandler.getInstance(activity.getApplicationContext()).deleteProduct(product, productId);
            int positionOfProduct = positionSelected;
            positionSelected = -1;
            notifyItemRangeChanged(positionOfProduct, products.size());



            Snackbar.make(view, activity.getString(R.string.product_deleted), Snackbar.LENGTH_LONG).setAction(activity.getString(R.string.undo), new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    products.add(positionOfProduct, product);
                    DBHandler.getInstance(activity.getApplicationContext()).insertProduct(product, listId);
                    notifyItemInserted(positionOfProduct);
                    notifyItemRangeChanged(positionOfProduct, products.size());
                }
            }).setActionTextColor(activity.getApplicationContext().getResources().getColor(R.color.blue)).show();

        }*/
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
        if(positionsSelected.get(index))
        {
            positionsSelected.set(index, false);
        }
        else
        {
            positionsSelected.set(index, true);
        }

        notifyItemChanged(index);

        if(!choosing)
        {
            doSomethingWithMenuAfterSelecting();
        }
    }

    private void doSomethingWithMenuAfterSelecting()
    {
        int numberOfSelected = getHowManySelected();
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
        positionsSelected.clear();
        for(int i=0;i<products.size();i++)
        {
            positionsSelected.add(false);
        }
        ((GroupActivity)activity).setMenuInvisible();
    }

    public void unselectAllProducts()
    {
        for(int i=0;i<positionsSelected.size();i++)
        {
            if(positionsSelected.get(i))
            {
                positionsSelected.set(i, false);
                notifyItemChanged(i);
            }

        }
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



    class ProductHolder extends RecyclerView.ViewHolder
    {
        TextView tittleText;
        TextView descriptionText;
        TextView numberText;
        ImageView imageView;
        CardView cardView;
        ConstraintLayout constraintLayout;
        public ProductHolder(@NonNull View itemView) {
            super(itemView);
            tittleText = itemView.findViewById(R.id.tittle_group_product_card);
            descriptionText = itemView.findViewById(R.id.description_group_product_card);
            numberText = itemView.findViewById(R.id.number_group_product_card);
            imageView = itemView.findViewById(R.id.image_group_product_card);
            cardView = itemView.findViewById(R.id.card_view_product_in_group_card);
            constraintLayout = itemView.findViewById(R.id.constraint_layout_product_in_group_card2);
        }
    }
}
