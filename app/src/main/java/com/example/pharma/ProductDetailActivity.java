package com.example.pharma;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.pharma.Adapter.CartItemsAdapter;
import com.example.pharma.Model.CartModel;
import com.example.pharma.Model.ProductModel;
import com.example.pharma.Model.UserModel;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ProductDetailActivity extends AppCompatActivity{
    TextView productName,productPrice,productDescription;
    ImageView productImage;
    AppCompatButton addtocart;
    private Toolbar toolbar;
    FirebaseDatabase rootNode;
    DatabaseReference reference;
    UserModel currentuser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        setContentView(R.layout.activity_product_detail);
        productName = findViewById(R.id.prod_name);
        productPrice = findViewById(R.id.prod_price);
        productDescription = findViewById(R.id.prod_description);
        productImage = findViewById(R.id.prod_image);
        toolbar = findViewById(R.id.product_detail_toolbar);

        addtocart = findViewById(R.id.detailActivityAddToCartBtn);

        productName.setText(getIntent().getStringExtra("name"));
        productPrice.setText("$ " + getIntent().getStringExtra("price"));
        SharedPreferences prefs = getSharedPreferences("selectedLanguage", MODE_PRIVATE);
        String language = prefs.getString("My_lang", "");
        if(language.equalsIgnoreCase("es")){
            productDescription.setText(getIntent().getStringExtra("spanishdesc"));
        } else {
            productDescription.setText(getIntent().getStringExtra("description"));
        }
        Glide.with(ProductDetailActivity.this).load(getIntent().getStringExtra("imgpath")).into(productImage);
        //productImage.setImageURI(Uri.parse(getIntent().getStringExtra("imgpath")));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Window window = ProductDetailActivity.this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(ContextCompat.getColor(ProductDetailActivity.this, R.color.black));

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(ProductDetailActivity.this, HomeActivity.class));
            }
        });

        addtocart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CartModel item = new CartModel();
                item.setPrice(Double.parseDouble(getIntent().getStringExtra("price")));
                item.setProductImage(getIntent().getStringExtra("imgpath"));
                item.setProductName(productName.getText().toString());
                item.setQuantity(1);
                item.setTotalItemPrice(Double.parseDouble(getIntent().getStringExtra("price")));
                SharedPreferences prefs = getSharedPreferences("Logged_in", MODE_PRIVATE);
                Gson gs = new Gson();
                String js = prefs.getString("Userobject", "");
                currentuser = gs.fromJson(js, UserModel.class);
                item.setUserid(currentuser.getUserName());
                rootNode = FirebaseDatabase.getInstance();
                reference = rootNode.getReference(CartModel.class.getSimpleName());
                reference.child(productName.getText().toString()).setValue(item);
                //Glide.with(ProductDetailActivity.this).load().into(productImage);
                //product.setImageFilePath(getIntent().getStringExtra("imgpath"));
                Toast.makeText(ProductDetailActivity.this, "Item added to cart", Toast.LENGTH_SHORT).show();
            }
        });

    }


    /*method to set the language locale of the app
     * In current app, we are using English and Spanish languages*/

    private void setLocale(String lang){
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration configuration = new Configuration();
        configuration.locale = locale;
        getBaseContext().getResources().updateConfiguration(configuration, getBaseContext().getResources().getDisplayMetrics());
        // Save data to shared preferences
        SharedPreferences.Editor editor = getSharedPreferences("selectedLanguage", MODE_PRIVATE).edit();
        editor.putString("My_lang", lang);
        editor.apply();
    }

    public void loadLocale(){
        SharedPreferences prefs = getSharedPreferences("selectedLanguage", MODE_PRIVATE);
        String language = prefs.getString("My_lang", "");
        setLocale(language);
    }
}