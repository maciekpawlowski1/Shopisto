package com.pawlowski.shopisto.edit_product_activity;

import android.app.Activity;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.pawlowski.shopisto.R;
import com.pawlowski.shopisto.models.CategoryModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryHolder> {

    int selectedCategory;
    Activity activity;
    List<CategoryModel> categories = new ArrayList<>();

    CategoryAdapter(Activity activity, int selectedCategory)
    {
        this.activity = activity;
        this.selectedCategory = selectedCategory;

        initCategories();


    }

    @NonNull
    @Override
    public CategoryHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.category_card,
                parent, false);
        return new CategoryHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CategoryAdapter.CategoryHolder holder, int position) {
        CategoryModel currentCategory = categories.get(position);

        holder.tittleText.setText(currentCategory.getTittle());
        holder.image.setImageResource(currentCategory.getImageDrawableId());

        if(currentCategory.getId() == selectedCategory)
            holder.cardView.setBackgroundColor(activity.getResources().getColor(R.color.selected_color));
        else
            holder.cardView.setBackgroundColor(activity.getResources().getColor(R.color.white));

        holder.cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int lastSelected = selectedCategory;
                selectedCategory = currentCategory.getId();
                notifyDataSetChanged();
            }
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public int getSelectedCategory()
    {
        return selectedCategory;
    }


    public void initCategories()
    {
        categories.add(new CategoryModel(0, activity.getString(R.string.others), R.drawable.food));
        categories.add(new CategoryModel(1, activity.getString(R.string.conserves), R.drawable.canned_food));
        categories.add(new CategoryModel(2, activity.getString(R.string.frozen), R.drawable.frozen));
        categories.add(new CategoryModel(3, activity.getString(R.string.alcohols), R.drawable.alcohol));
        categories.add(new CategoryModel(4, activity.getString(R.string.bakery), R.drawable.bread));
        categories.add(new CategoryModel(5, activity.getString(R.string.pastas), R.drawable.pasta));
        categories.add(new CategoryModel(6, activity.getString(R.string.sauces), R.drawable.sauces));
        categories.add(new CategoryModel(7, activity.getString(R.string.electronics), R.drawable.electronics_icon));
        categories.add(new CategoryModel(8, activity.getString(R.string.meat), R.drawable.meat_icon));
        categories.add(new CategoryModel(9, activity.getString(R.string.clothes), R.drawable.clothes_icon));
        categories.add(new CategoryModel(10, activity.getString(R.string.sweets), R.drawable.sweets_icon));
        categories.add(new CategoryModel(11, activity.getString(R.string.cereals), R.drawable.cereals_meals_icon));
        categories.add(new CategoryModel(12, activity.getString(R.string.fruits_and_vegetables), R.drawable.fruits_and_vegetables_icon));
        categories.add(new CategoryModel(13, activity.getString(R.string.drinks), R.drawable.drinks_icon));
        categories.add(new CategoryModel(14, activity.getString(R.string.dairy), R.drawable.dairy_icon));
        categories.add(new CategoryModel(15, activity.getString(R.string.ready_meals), R.drawable.ready_meals_icon));
        categories.add(new CategoryModel(16, activity.getString(R.string.stationary), R.drawable.stationary));



        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            categories.sort(new Comparator<CategoryModel>() {
                @Override
                public int compare(CategoryModel o1, CategoryModel o2) {
                    if(o1.getId() == 0)
                        return -1;
                    else if(o2.getId() == 0)
                        return 1;
                    return o1.getTittle().compareTo(o2.getTittle());
                }
            });
        }
        else
        {
            //Manual sorting
            for(int i=0;i<(categories.size()-1);i++)
            {
                for(int j = i+1;j<categories.size();j++)
                {
                    CategoryModel c1 = categories.get(i);
                    CategoryModel c2 = categories.get(j);
                    if(c1.getId() != 0 && (c1.getTittle().compareTo(c2.getTittle()) > 0))
                        Collections.swap(categories, i, j);


                }
            }
        }
    }

    class CategoryHolder extends RecyclerView.ViewHolder
    {
        ImageView image;
        TextView tittleText;
        CardView cardView;
        public CategoryHolder(@NonNull View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.image_category_card);
            tittleText = itemView.findViewById(R.id.tittle_category_card);
            cardView = itemView.findViewById(R.id.card_view_category_card);
        }
    }
}
