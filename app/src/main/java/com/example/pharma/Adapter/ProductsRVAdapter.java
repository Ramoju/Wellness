package com.example.pharma.Adapter;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.pharma.HomeActivity;
import com.example.pharma.Model.CartModel;
import com.example.pharma.Model.ImageModel;
import com.example.pharma.Model.ProductModel;
import com.example.pharma.Model.UserModel;
import com.example.pharma.ProductDetailActivity;
import com.example.pharma.R;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;

public class ProductsRVAdapter extends RecyclerView.Adapter<ProductsRVAdapter.MyViewHolder> {
    private List<ProductModel> products;
    private HomeActivity activity;
    ArrayList<ImageModel> productimages = new ArrayList<>();
    DatabaseReference reference;
    String imguri;
    UserModel currentuser;

    public ProductsRVAdapter(List<ProductModel> products, HomeActivity activity){
        this.activity = activity;
        this.products = products;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.product_card, parent, false);
        return new MyViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        final ProductModel item = products.get(position);
        holder.productName.setText(item.getName());
        holder.productPrice.setText("$ " + String.valueOf(item.getPrice()));
        holder.productDescription.setText(item.getDescription());
        Query query = FirebaseDatabase.getInstance().getReference(ProductModel.class.getSimpleName()).orderByChild("name").equalTo(item.getName());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                reference = FirebaseDatabase.getInstance().getReference(ProductModel.class.getSimpleName()).child(snapshot.getChildren().iterator().next().getKey()).child("ImageModel");
                reference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds: snapshot.getChildren()){
                            ImageModel singleImage = new ImageModel();
                            singleImage = ds.getValue(ImageModel.class);
                            productimages.add(singleImage);
                            Glide.with(activity).load(singleImage.getImageURI()).into(holder.productImage);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
                System.out.println("snapshot in product RV adapter" + snapshot.getValue(ImageModel.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(view.getContext(), ProductDetailActivity.class);
                intent.putExtra("name", item.getName());
                intent.putExtra("price", String.valueOf(item.getPrice()));
                intent.putExtra("description", item.getDescription());
                intent.putExtra("spanishdesc", item.getSpanishDescription());
                for(ImageModel picture: productimages){
                    if(picture.getProductName().equalsIgnoreCase(item.getName())){
                        imguri = picture.getImageURI();
                    }
                }
                intent.putExtra("imgpath", imguri);
                activity.startActivity(intent);
            }
        });
        holder.addtocart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseDatabase rootNode;
                DatabaseReference reference;
                CartModel cart = new CartModel();
                cart.setPrice(item.getPrice());
                for(ImageModel picture: productimages){
                    if(picture.getProductName().equalsIgnoreCase(item.getName())){
                        imguri = picture.getImageURI();
                    }
                }
                cart.setProductImage(imguri);
                cart.setProductName(item.getName());
                cart.setQuantity(1);
                cart.setTotalItemPrice(item.getPrice());
                SharedPreferences prefs = activity.getSharedPreferences("Logged_in", MODE_PRIVATE);
                Gson gs = new Gson();
                String js = prefs.getString("Userobject", "");
                currentuser = gs.fromJson(js, UserModel.class);
                cart.setUserid(currentuser.getUserName());
                rootNode = FirebaseDatabase.getInstance();
                reference = rootNode.getReference(CartModel.class.getSimpleName());
                reference.child(item.getName()).setValue(cart);
                Toast.makeText(activity, "Item added to cart", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return products.size();
    }

    public void setProducts(List<ProductModel> mList){
        this.products = mList;
        notifyDataSetChanged();
    }

    public void filterList(List<ProductModel> filteredList){
        this.products = filteredList;
        notifyDataSetChanged();
    }



    public static class MyViewHolder extends RecyclerView.ViewHolder{

        TextView productName;
        TextView productPrice;
        TextView productDescription;
        ImageView productImage;
        ImageView addtocart;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.product_name);
            productPrice = itemView.findViewById(R.id.product_price);
            productImage = itemView.findViewById(R.id.productimage);
            productDescription = itemView.findViewById(R.id.product_description);
            addtocart = itemView.findViewById(R.id.eachShoeAddToCartBtn);
        }
    }
}
