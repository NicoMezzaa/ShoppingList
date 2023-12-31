package com.example.shoppinglist.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
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

import com.example.shoppinglist.listener.OnProductClickListener;
import com.example.shoppinglist.R;
import com.example.shoppinglist.entities.ProductEntity;

import java.util.List;


public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {

    private final List<ProductEntity> productList;
    private OnProductClickListener listener;
    private final boolean showButtons;      // indica se i bottoni devono essere mostrati

    public ProductAdapter(List<ProductEntity> productList, boolean showButtons) {
        this.productList = productList;
        this.showButtons = showButtons;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        View view = LayoutInflater.from(context).inflate(R.layout.item_shopping_list, parent, false);
        return new ViewHolder(view, showButtons, context);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ProductEntity product = productList.get(position);
        holder.bind(product);
        holder.itemView.setOnClickListener(view -> {
            if (listener != null) {
                listener.onProductClick(product);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (productList != null) {
            return productList.size();
        } else {
            return 0;
        }
    }

    public List<ProductEntity> getProductList() {
        return productList;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView textViewProductName;
        private final TextView textViewProductPrice;
        private final TextView textViewProductQuantity;
        private final TextView textViewProductCategory;
        private final Context context;

        public ViewHolder(@NonNull View itemView, boolean showButtons, Context context) {
            super(itemView);
            this.context = context;

            textViewProductName = itemView.findViewById(R.id.text_view_product_name);
            textViewProductPrice = itemView.findViewById(R.id.text_view_product_price);
            textViewProductQuantity = itemView.findViewById(R.id.text_view_product_quantity);
            textViewProductCategory = itemView.findViewById(R.id.text_view_product_category);

            ImageButton buttonDelete = itemView.findViewById(R.id.button_delete);
            ImageButton buttonEdit = itemView.findViewById(R.id.button_edit);

            if (showButtons) {
                buttonDelete.setVisibility(View.VISIBLE);
                buttonEdit.setVisibility(View.VISIBLE);

                ColorStateList originalButtonDeleteColors = buttonDelete.getImageTintList();
                ColorStateList originalButtonEditColors = buttonEdit.getImageTintList();

                setButtonTouchListener(buttonDelete, originalButtonDeleteColors);
                setButtonTouchListener(buttonEdit, originalButtonEditColors);
            } else {
                buttonDelete.setVisibility(View.GONE);
                buttonEdit.setVisibility(View.GONE);
            }
        }

        public void bind(ProductEntity product) {
            textViewProductName.setText(product.getName());
            textViewProductPrice.setText(product.getPrice());
            textViewProductQuantity.setText(product.getQuantity());
            textViewProductCategory.setText(product.getCategory());
        }

        @SuppressLint("ClickableViewAccessibility")
        private void setButtonTouchListener(ImageButton button, ColorStateList originalColors) {
            button.setOnTouchListener((v, event) -> {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    int color = ContextCompat.getColor(context, R.color.gray);
                    button.setImageTintList(ColorStateList.valueOf(color));
                } else if (event.getAction() == MotionEvent.ACTION_UP || event.getAction() == MotionEvent.ACTION_CANCEL) {
                    // ritorna al colore originale quando il pulsante viene rilasciato o l'azione viene annullata
                    button.setImageTintList(originalColors);
                }
                return false;
            });
        }
    }
}