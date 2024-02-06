package com.example.pharma;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.content.ContextCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pharma.Adapter.ProductsRVAdapter;
import com.example.pharma.Model.ImageModel;
import com.example.pharma.Model.ProductModel;
import com.example.pharma.Model.UserModel;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class HomeActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener{
    DrawerLayout drawerLayout;
    ActionBarDrawerToggle toggle;
    NavigationView navigationView;
    TabLayout tabLayout;
    RecyclerView mRecyclerView;
    ArrayList<ProductModel> allproducts = new ArrayList<>();
    List<ProductModel> filteredProducts = new ArrayList<>();
    List<String> categories = new ArrayList<>();
    ArrayList<ImageModel> productImages = new ArrayList<>();
    ProductsRVAdapter adapter;
    String category;
    Menu navigationMenu;
    String userType;
    String fullname;
    String user_name;
    UserModel curruser;
    View headerView;
    FirebaseDatabase rootNode;
    DatabaseReference reference;
    SwipeListener swipeListener;
    CoordinatorLayout coordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        setContentView(R.layout.activity_home);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        tabLayout = findViewById(R.id.tablayout);
        drawerLayout = findViewById(R.id.drawer);
        navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(HomeActivity.this);
        mRecyclerView = findViewById(R.id.productsRV);
        coordinatorLayout = findViewById(R.id.coordinatorLayout);

        toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.open, R.string.close);
        drawerLayout.addDrawerListener(toggle);
        toggle.setDrawerIndicatorEnabled(true);
        toggle.syncState();
        swipeListener = new SwipeListener(mRecyclerView);

        Window window = HomeActivity.this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(ContextCompat.getColor(HomeActivity.this, R.color.black));

        //loadSampledata();

        rootNode = FirebaseDatabase.getInstance();
        reference = rootNode.getReference(ProductModel.class.getSimpleName());

        ValueEventListener vle = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                System.out.println("^^^^^^^^^^" + snapshot.getChildren());
                for(DataSnapshot ds: snapshot.getChildren()){
                    ProductModel singleProduct = new ProductModel();
                    singleProduct = ds.getValue(ProductModel.class);
                    allproducts.add(singleProduct);
                }
                categories = getAllCategories(allproducts);
                if (allproducts.size()>0)
                    filteredProducts = getFilteredProducts(categories.get(0));
                adapter = new ProductsRVAdapter(filteredProducts, HomeActivity.this);
                mRecyclerView.setHasFixedSize(true);
                mRecyclerView.setLayoutManager(new GridLayoutManager(HomeActivity.this, 2));
                mRecyclerView.setAdapter(adapter);
                setDynamicTabs(categories);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        SharedPreferences prefs = getSharedPreferences("Logged_in", MODE_PRIVATE);
        Gson gs = new Gson();
        String js = prefs.getString("Userobject", "");
        curruser = gs.fromJson(js, UserModel.class);

        reference.addValueEventListener(vle);
        fullname = curruser.getFullName();
        user_name = curruser.getUserName();

        userType = curruser.getUserType();
        navigationView.setBackgroundColor(getResources().getColor(R.color.lightgrey));
        navigationMenu = navigationView.getMenu();
        navigationMenu.findItem(R.id.search).setVisible(false);
        headerView = navigationView.getHeaderView(0);

        // Setting welcome message in navigation header
        TextView navUsername = (TextView) headerView.findViewById(R.id.welcomemessage);
        navUsername.setText("Hi " + fullname);

        // Code to enable admin features if logged in user is admin
        if(userType.equalsIgnoreCase("admin")){
            navigationMenu.findItem(R.id.addnewproduct).setVisible(true);
            navigationMenu.findItem(R.id.salesreports).setVisible(true);
        }


        // Event listener to filter the products based on selected tab
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                filteredProducts.clear();
                category = tab.getText().toString();
                filteredProducts = getFilteredProducts(category);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    private class SwipeListener implements View.OnTouchListener{
        GestureDetector gestureDetector;

        SwipeListener(View v){
            int threshold = 100;
            int velocity_threshold = 100;

            GestureDetector.SimpleOnGestureListener listener = new GestureDetector.SimpleOnGestureListener(){
                @Override
                public boolean onDown(MotionEvent e) {
                    return true;
                }

                @Override
                public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                    float xdiff = 0;
                    float ydiff = 0;
                    if(e2 != null && e1 != null) {
                        xdiff = e2.getX() - e1.getX();
                        ydiff = e2.getY()-e1.getY();
                    }
                    try {
                        if(Math.abs(xdiff) > Math.abs(ydiff)){
                            if(Math.abs(xdiff) > threshold && Math.abs(velocityX) > velocity_threshold){
                                if(xdiff>0){
                                    TabLayout.Tab tab = tabLayout.getTabAt(tabLayout.getSelectedTabPosition()-1);
                                    tab.select();
                                } else {
                                    TabLayout.Tab tab = tabLayout.getTabAt(tabLayout.getSelectedTabPosition()+1);
                                    tab.select();
                                }
                                return true;
                            }
                        } else {
                            if(Math.abs(ydiff) > threshold && Math.abs(velocityY) > velocity_threshold){
                                if(ydiff>0){

                                }else {

                                }
                                return true;
                            }
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    return false;
                }
            };

            gestureDetector = new GestureDetector(listener);
            v.setOnTouchListener(this);
        }

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            return gestureDetector.onTouchEvent(motionEvent);
        }
    }


    // Method to retrieve filtered products

    private List<ProductModel> getFilteredProducts(String category) {
        for(int i=0; i< allproducts.size(); i++){
            if(category.equalsIgnoreCase(allproducts.get(i).getCategory())) {
                filteredProducts.add(allproducts.get(i));
            }
        }
        return filteredProducts;
    }

    //Method to dynamically set the tabs based on product category

    private void setDynamicTabs(List<String> categories) {
        for (int i = 0; i < categories.size(); i++) {
            tabLayout.addTab(tabLayout.newTab().setText(categories.get(i)));
        }
    }

    private List<String> getAllCategories(List<ProductModel> products) {
        for (int i=0; i< products.size(); i++){
            categories.add(products.get(i).getCategory());
        }
        List<String> uniqueCat = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            uniqueCat = categories.stream().distinct().collect(Collectors.toList());
        }
        return uniqueCat;
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(HomeActivity.this)
                .setTitle("Are you sure, you want to logout?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        SharedPreferences prefs = getSharedPreferences("Logged_in", MODE_PRIVATE);
                        Gson gs = new Gson();
                        String js = prefs.getString("Userobject", "");
                        prefs.edit().remove("Userobject").apply();
                        Intent newIntent = new Intent(getApplicationContext(), MainActivity.class);
                        startActivity(newIntent);
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.nav_menu,menu);
        MenuItem m1 = menu.findItem(R.id.showcart);
        MenuItem m2 = menu.findItem(R.id.logout);
        MenuItem m3 = menu.findItem(R.id.myorders);
        MenuItem m4 = menu.findItem(R.id.myaccount);
        MenuItem m5 = menu.findItem(R.id.addnewproduct);
        MenuItem m6 = menu.findItem(R.id.salesreports);
        m1.setVisible(false);
        m2.setVisible(false);
        m3.setVisible(false);
        m4.setVisible(false);
        m5.setVisible(false);
        m6.setVisible(false);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
//            case R.id.cart:
//                Intent intent = new Intent(HomeActivity.this,CartActivity.class);
//                startActivity(intent);
//                return true;
            case R.id.search:
                SearchView searchView = (SearchView) item.getActionView();
                searchView.setQueryHint("Type here to search");
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        return false;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        System.out.println("Search Text: " + newText);
                        filter(newText);
                        return false;
                    }
                });
                return true;

//            case R.id.showcart:
//                Intent newIntent = new Intent(getApplicationContext(), CartActivity.class);
//                startActivity(newIntent);
//                return true;
            default:return super.onOptionsItemSelected(item);
        }
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        final int itemId = item.getItemId();

        if (itemId == R.id.showcart) {
            Intent newIntent = new Intent(getApplicationContext(), CartActivity.class);
            startActivity(newIntent);
            return true;
        }
        if (itemId == R.id.logout) {
            new AlertDialog.Builder(HomeActivity.this)
                    .setTitle("Are you sure, you want to logout?")
                    .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            SharedPreferences prefs = getSharedPreferences("Logged_in", MODE_PRIVATE);
                            Gson gs = new Gson();
                            String js = prefs.getString("Userobject", "");
                            prefs.edit().remove("Userobject").apply();
                            Intent newIntent = new Intent(getApplicationContext(), MainActivity.class);
                            startActivity(newIntent);
                        }
                    })
                    .setNegativeButton("No", null)
                    .show();
            return true;
        }
        if (itemId == R.id.myorders) {
            Intent newIntent = new Intent(getApplicationContext(), OrdersActivity.class);
            startActivity(newIntent);
            return true;
        }
        if (itemId == R.id.myaccount) {
            Intent newIntent = new Intent(getApplicationContext(), MyAccount.class);
            newIntent.putExtra("curruser", curruser);
            startActivity(newIntent);
            return true;
        }
        if (itemId == R.id.addnewproduct) {
            Intent newIntent = new Intent(getApplicationContext(), AddNewProduct.class);
            startActivity(newIntent);
            return true;
        }
        if (itemId == R.id.salesreports) {
            Intent newIntent = new Intent(getApplicationContext(), SalesReportsActivity.class);
            startActivity(newIntent);
            return true;
        }

        return super.onOptionsItemSelected(item);

    }

    //Method to filter the products on user search

    private void filter(String newText) {
        List<ProductModel> filteredList = new ArrayList<>();
        for (ProductModel item : allproducts){
            if(item.getName().toLowerCase().contains(newText.toLowerCase())){
                filteredList.add(item);
            }
        }
        adapter.filterList(filteredList);
    }

    private void makeSnackBar(String msg) {
        Snackbar.make(coordinatorLayout, msg, Snackbar.LENGTH_SHORT)
                .setAction("Go to Cart", new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        startActivity(new Intent(HomeActivity.this, CartActivity.class));
                    }
                }).show();
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