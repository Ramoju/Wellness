package com.example.pharma.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pharma.Model.ProductModel;
import com.example.pharma.CheckoutActivity;
import com.example.pharma.R;

import java.util.List;

public class BuyProductsAdapter extends RecyclerView.Adapter<BuyProductsAdapter.MyViewHolder>{
    private List<ProductModel> productstobuy;
    private CheckoutActivity activity;

    public BuyProductsAdapter(List<ProductModel> products, CheckoutActivity activity){
        this.activity = activity;
        this.productstobuy = products;
    }
    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_card, parent, false);
        return new BuyProductsAdapter.MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        final ProductModel item = productstobuy.get(position);
        holder.productName.setText(item.getName());
        holder.productPrice.setText(String.valueOf(item.getPrice()));

//        if (item.getImageFilePath()!=null) {
//            holder.productImage.setImageURI(Uri.parse(item.getImageFilePath()));
//        }
    }

    @Override
    public int getItemCount() {
        return productstobuy.size();
    }

    public void setProducts(List<ProductModel> mList){
        this.productstobuy = mList;
        notifyDataSetChanged();
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder{

        TextView productName;
        TextView productPrice;
        ImageView productImage;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.product_name);
            productPrice = itemView.findViewById(R.id.product_price);
            productImage = itemView.findViewById(R.id.productimage);
        }
    }
}
