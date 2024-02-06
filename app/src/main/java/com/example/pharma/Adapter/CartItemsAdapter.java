package com.example.pharma.Adapter;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.pharma.CartActivity;
import com.example.pharma.HomeActivity;
import com.example.pharma.Model.CartModel;
import com.example.pharma.R;

import java.util.List;

public class CartItemsAdapter extends RecyclerView.Adapter<CartItemsAdapter.CartViewHodler>{
    private CartClickedListeners cartClickedListeners;
    private List<CartModel> cartList;
    private CartActivity activity;

    public CartItemsAdapter(CartClickedListeners cartClickedListeners, CartActivity activity) {
        this.cartClickedListeners = cartClickedListeners;
        this.activity = activity;
    }

    public void setShoeCartList(List<CartModel> shoeCartList) {
        this.cartList = shoeCartList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CartViewHodler onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.cart_item, parent, false);
        return new CartViewHodler(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHodler holder, int position) {

        CartModel item = cartList.get(position);
        holder.productImageView.setImageURI(Uri.parse(item.getProductImage()));
        holder.productNameTv.setText(item.getProductName());
        holder.productDescTv.setText(item.getProductDescription());
        holder.quantity.setText(item.getQuantity() + "");
        holder.productPriceTv.setText("$" + item.getPrice() + "");
        Glide.with(activity).load(item.getProductImage()).into(holder.productImageView);


        holder.deleteShoeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cartClickedListeners.onDeleteClicked(item);
            }
        });


        holder.addQuantityBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cartClickedListeners.onPlusClicked(item);
            }
        });

        holder.minusQuantityBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cartClickedListeners.onMinusClicked(item);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (cartList == null) {
            return 0;
        } else {
            return cartList.size();
        }
    }

    public class CartViewHodler extends RecyclerView.ViewHolder {

        private TextView productNameTv, productDescTv, productPriceTv, quantity;
        private ImageView deleteShoeBtn;
        private ImageView productImageView;
        private ImageButton addQuantityBtn, minusQuantityBtn;

        public CartViewHodler(@NonNull View itemView) {
            super(itemView);

            productNameTv = itemView.findViewById(R.id.eachCartItemName);
            productDescTv = itemView.findViewById(R.id.eachCartItemBrandNameTv);
            productPriceTv = itemView.findViewById(R.id.eachCartItemPriceTv);
            deleteShoeBtn = itemView.findViewById(R.id.eachCartItemDeleteBtn);
            productImageView = itemView.findViewById(R.id.eachCartItemIV);
            quantity = itemView.findViewById(R.id.eachCartItemQuantityTV);
            addQuantityBtn = itemView.findViewById(R.id.eachCartItemAddQuantityBtn);
            minusQuantityBtn = itemView.findViewById(R.id.eachCartItemMinusQuantityBtn);
        }
    }

    public interface CartClickedListeners {
        void onDeleteClicked(CartModel cart);

        void onPlusClicked(CartModel cart);

        void onMinusClicked(CartModel cart);
    }
}
