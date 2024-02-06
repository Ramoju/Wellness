package com.example.pharma;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.example.pharma.Adapter.CartItemsAdapter;
import com.example.pharma.Adapter.OrdersAdapter;
import com.example.pharma.Model.CartModel;
import com.example.pharma.Model.OrderModel;
import com.example.pharma.Model.ProductModel;
import com.example.pharma.Model.UserModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OrdersActivity extends AppCompatActivity {

    RecyclerView ordersRV;
    List<OrderModel> orders = new ArrayList<>();
    OrdersAdapter adapter;
    Button home;
    Toolbar toolbar;
    FirebaseDatabase rootNode;
    DatabaseReference orderreference;
    UserModel curruser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        setContentView(R.layout.activity_orders);

        SharedPreferences prefs = getSharedPreferences("Logged_in", MODE_PRIVATE);
        Gson gs = new Gson();
        String js = prefs.getString("Userobject", "");
        curruser = gs.fromJson(js, UserModel.class);

        ordersRV = findViewById(R.id.ordersRV);
        home = findViewById(R.id.ordershome);
        toolbar = findViewById(R.id.myorderstoolbar);

        adapter = new OrdersAdapter(orders, OrdersActivity.this);
        ordersRV.setHasFixedSize(true);
        ordersRV.setLayoutManager(new LinearLayoutManager(this));
        ordersRV.setAdapter(adapter);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Window window = OrdersActivity.this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(ContextCompat.getColor(OrdersActivity.this, R.color.black));

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(OrdersActivity.this, HomeActivity.class));
            }
        });

        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(OrdersActivity.this, HomeActivity.class);
                startActivity(intent);
            }
        });

        rootNode = FirebaseDatabase.getInstance();
        orderreference = rootNode.getReference(OrderModel.class.getSimpleName());
        Query query = orderreference.orderByChild("userName").equalTo(curruser.getUserName().toLowerCase());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                orderreference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds: snapshot.getChildren()) {
                            System.out.println(ds.child("productName").getValue());
                            OrderModel order = new OrderModel();
                            order = ds.getValue(OrderModel.class);
                            if(order.getUserName().equalsIgnoreCase(curruser.getUserName())) {
                                orders.add(order);
                            }
                        }
                        adapter.setOrders(orders);
                        adapter.notifyDataSetChanged();

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

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