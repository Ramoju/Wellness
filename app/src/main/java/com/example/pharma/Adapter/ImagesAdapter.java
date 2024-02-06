package com.example.pharma.Adapter;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.pharma.AddNewProduct;
import com.example.pharma.HomeActivity;
import com.example.pharma.Model.ImageModel;
import com.example.pharma.Model.ProductModel;
import com.example.pharma.R;

import java.util.List;

public class ImagesAdapter extends RecyclerView.Adapter<ImagesAdapter.ImageViewHolder> {
    private List<ImageModel> images;
    private AddNewProduct activity;

    public ImagesAdapter(List<ImageModel> images, AddNewProduct activity){
        this.activity = activity;
        this.images = images;
    }
    @NonNull
    @Override
    public ImageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_card, parent, false);
        return new ImageViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ImageViewHolder holder, int position) {
        final ImageModel item = images.get(position);
        holder.productImage.setImageURI(Uri.parse(item.getImageURI()));
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder{

        ImageView productImage;

        public ImageViewHolder(@NonNull View itemView) {
            super(itemView);
            productImage = itemView.findViewById(R.id.product_picture);

        }
    }
}
