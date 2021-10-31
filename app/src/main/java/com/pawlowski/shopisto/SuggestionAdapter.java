package com.pawlowski.shopisto;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

public class SuggestionAdapter extends RecyclerView.Adapter<SuggestionAdapter.SuggestionCardHolder> {

    ArrayList<String>suggestionTittles = new ArrayList<>();

    Activity activity;
    SuggestionAdapter(Activity activity)
    {
        this.activity = activity;
    }

    @NonNull
    @Override
    public SuggestionCardHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.suggestion_card,
                parent, false);
        return new SuggestionCardHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SuggestionAdapter.SuggestionCardHolder holder, int position) {
        String currentSuggestion = suggestionTittles.get(position);
        holder.tittleText.setText(currentSuggestion);
        holder.constraintLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((AddProductsToListActivity)activity).addProduct(currentSuggestion);
                ((AddProductsToListActivity)activity).hidePopUp();
            }
        });
    }

    @Override
    public int getItemCount() {
        return suggestionTittles.size();
    }

    public void setSuggestions(List<String> suggestions)
    {
        suggestionTittles = new ArrayList<>(suggestions);
        notifyDataSetChanged();
    }



    class SuggestionCardHolder extends RecyclerView.ViewHolder
    {
        TextView tittleText;
        ConstraintLayout constraintLayout;
        public SuggestionCardHolder(@NonNull View itemView) {
            super(itemView);
            tittleText = itemView.findViewById(R.id.tittle_suggestion_card);
            constraintLayout = itemView.findViewById(R.id.constraint_layout_suggestion_pop_up);
        }
    }
}
