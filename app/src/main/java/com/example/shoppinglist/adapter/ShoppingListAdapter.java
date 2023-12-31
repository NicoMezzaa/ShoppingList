package com.example.shoppinglist.adapter;

import android.annotation.SuppressLint;
import android.content.res.ColorStateList;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.shoppinglist.listener.OnListClickListener;
import com.example.shoppinglist.R;
import com.example.shoppinglist.entities.ShoppingListEntity;

import java.util.List;

public class ShoppingListAdapter extends RecyclerView.Adapter<ShoppingListAdapter.ViewHolder> {

    private List<ShoppingListEntity> shoppingLists;
    private final OnListClickListener listener;

    public ShoppingListAdapter(List<ShoppingListEntity> shoppingLists, OnListClickListener listener) {
        this.shoppingLists = shoppingLists;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_layout, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ShoppingListEntity list = shoppingLists.get(position);
        holder.bind(list);
        holder.itemView.setOnClickListener(view -> {
            if (listener != null) {
                listener.onListClick(list.getListName());
            }
        });
    }

    @Override
    public int getItemCount() {
        return shoppingLists != null ? shoppingLists.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewListName;
        private final ColorStateList originalButtonDeleteColors;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewListName = itemView.findViewById(R.id.text_view_list_name);
            ImageButton buttonDelete = itemView.findViewById(R.id.button_delete);

            originalButtonDeleteColors = buttonDelete.getImageTintList();
            setButtonTouchListener(buttonDelete);
        }

        public void bind(ShoppingListEntity list) {
            textViewListName.setText(list.getListName());
        }

        @SuppressLint("ClickableViewAccessibility")
        private void setButtonTouchListener(ImageButton button) {
            button.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    int color = ContextCompat.getColor(button.getContext(), R.color.gray);
                    button.setImageTintList(ColorStateList.valueOf(color));
                } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    // ritorna al colore originale quando il pulsante viene rilasciato o l'azione viene annullata
                    button.setImageTintList(originalButtonDeleteColors);
                }
                return false;
            });
        }
    }

    public void setShoppingLists(List<ShoppingListEntity> shoppingLists) {
        this.shoppingLists = shoppingLists;
    }

    public List<ShoppingListEntity> getShoppingLists() {
        return shoppingLists;
    }
}