package com.example.pharma;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pharma.Adapter.CartItemsAdapter;
import com.example.pharma.Model.CartModel;
import com.example.pharma.Model.OrderModel;
import com.example.pharma.Model.SalesModel;
import com.example.pharma.Model.UserModel;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.paypal.android.sdk.payments.PayPalConfiguration;
import com.paypal.android.sdk.payments.PayPalPayment;
import com.paypal.android.sdk.payments.PayPalService;
import com.paypal.android.sdk.payments.PaymentActivity;
import com.paypal.android.sdk.payments.PaymentConfirmation;

import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;

public class CartActivity extends AppCompatActivity implements CartItemsAdapter.CartClickedListeners{

    RecyclerView cartProductsRV;
    ArrayList<CartModel> cartitems = new ArrayList<>();
    CartItemsAdapter adapter;
    Button checkout;
    TextView totalprice;
    Toolbar toolbar;
    FirebaseDatabase rootNode;
    DatabaseReference cartreference;
    DatabaseReference orderreference;
    DatabaseReference usercartref;
    UserModel curruser;
    Double totalamount = 0.0;
    ValueEventListener vle;
    Boolean initialLoad = false;
    private static final String YOUR_CLIENT_ID = "ASruBnnzMyiDboBclz5gqfXpwINKyYtc2fftdA12ttH24pQGRxCBYb3Sq0ZmYUKzjJ8ZN5U1RjVFnfAy";
    //private static final String YOUR_CLIENT_ID = "";
    public static final int PAYPAL_REQUEST_CODE = 123;
    // Paypal Configuration Object
    private static PayPalConfiguration config = new PayPalConfiguration()
            // Start with mock environment.  When ready,
            // switch to sandbox (ENVIRONMENT_SANDBOX)
            // or live (ENVIRONMENT_PRODUCTION)
            .environment(PayPalConfiguration.ENVIRONMENT_SANDBOX)
            // on below line we are passing a client id.
            .clientId(YOUR_CLIENT_ID);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        setContentView(R.layout.activity_cart);

        cartProductsRV = findViewById(R.id.cartitemsRV);
        checkout = findViewById(R.id.checkout);
        toolbar = findViewById(R.id.carttoolbar);
        totalprice = findViewById(R.id.cartActivityTotalPriceTv);
        adapter = new CartItemsAdapter(this, CartActivity.this);
        cartProductsRV.setHasFixedSize(true);
        cartProductsRV.setLayoutManager(new LinearLayoutManager(this));
        cartProductsRV.setAdapter(adapter);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        //Code to get logged in user's object from shared pref
        SharedPreferences prefs = getSharedPreferences("Logged_in", MODE_PRIVATE);
        Gson gs = new Gson();
        String js = prefs.getString("Userobject", "");
        curruser = gs.fromJson(js, UserModel.class);

        rootNode = FirebaseDatabase.getInstance();
        cartreference = rootNode.getReference(CartModel.class.getSimpleName());
        orderreference = rootNode.getReference(OrderModel.class.getSimpleName());
        System.out.println("Current user %%%%" + curruser.getUserName());
        //Query to filter user specific cart items
        Query query = cartreference.orderByChild("userid").equalTo(curruser.getUserName().toLowerCase());
        query.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                vle = new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for(DataSnapshot ds: snapshot.getChildren()){
                            System.out.println(ds.child("productName").getValue());
                            CartModel item = new CartModel();
                            item = ds.getValue(CartModel.class);
                            System.out.println(item.getProductName());
                            if(initialLoad && item.getUserid().equalsIgnoreCase(curruser.getUserName())) {
                                cartitems.add(item);
                                totalamount = totalamount + item.getTotalItemPrice();
                            }
                        }
                        adapter.setShoeCartList(cartitems);
                        adapter.notifyDataSetChanged();
                        totalprice.setText("$" + totalamount.toString());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                };
                if(initialLoad) {
                    cartreference.addValueEventListener(vle);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        Window window = CartActivity.this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(ContextCompat.getColor(CartActivity.this, R.color.black));

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(CartActivity.this, HomeActivity.class));
            }
        });

        checkout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(cartitems.size()>0) {
                    getPayment();
                } else {
                    Toast.makeText(CartActivity.this, "Please add atleast one item to Cart", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // Interface methods to update or delete cart items

    @Override
    public void onDeleteClicked(CartModel cart) {
        initialLoad = false;
        cartitems.remove(cart);
        cartreference.child(cart.getProductName()).removeValue();
        totalamount = totalamount - cart.getTotalItemPrice();
    }

    @Override
    public void onPlusClicked(CartModel cart) {
        initialLoad = false;
        cart.setQuantity(cart.getQuantity()+1);
        cartreference.child(cart.getProductName()).child("quantity").setValue(cart.getQuantity());
        cart.setTotalItemPrice(cart.getPrice()*cart.getQuantity());
        cartreference.child(cart.getProductName()).child("totalItemPrice").setValue(cart.getTotalItemPrice());
        totalamount = totalamount + cart.getPrice();
    }

    @Override
    public void onMinusClicked(CartModel cart) {
        initialLoad = false;
        if(cart.getQuantity()>0) {
            cart.setQuantity(cart.getQuantity() - 1);
            cartreference.child(cart.getProductName()).child("quantity").setValue(cart.getQuantity());
            cart.setTotalItemPrice(cart.getPrice() * cart.getQuantity());
            cartreference.child(cart.getProductName()).child("totalItemPrice").setValue(cart.getTotalItemPrice());
            totalamount = totalamount - cart.getPrice();
        } else {

        }
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

    @Override
    protected void onStart() {
        super.onStart();
        initialLoad = true;
    }

    private void getPayment() {

        // Getting the amount from editText
        String totalamount = totalprice.getText().toString();
        System.out.println(totalamount);

        // Creating a paypal payment on below line.
        PayPalPayment payment = new PayPalPayment(BigDecimal.valueOf(Double.parseDouble(totalamount.replace("$", ""))), "USD", "Course Fees",
                PayPalPayment.PAYMENT_INTENT_SALE);

        // Creating Paypal Payment activity intent
        Intent intent = new Intent(this, PaymentActivity.class);


        //putting the paypal configuration to the intent
        intent.putExtra(PayPalService.EXTRA_PAYPAL_CONFIGURATION, config);

        // Putting paypal payment to the intent
        intent.putExtra(PaymentActivity.EXTRA_PAYMENT, payment);

        // Starting the intent activity for result
        // the request code will be used on the method onActivityResult
        startActivityForResult(intent, PAYPAL_REQUEST_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PAYPAL_REQUEST_CODE) {

            // If the result is OK i.e. user has not canceled the payment
            if (resultCode == Activity.RESULT_OK) {

                // Getting the payment confirmation
                PaymentConfirmation confirm = data.getParcelableExtra(PaymentActivity.EXTRA_RESULT_CONFIRMATION);

                // if confirmation is not null
                if (confirm != null) {
                    try {
                        // Getting the payment details
                        String paymentDetails = confirm.toJSONObject().toString(4);
                        // on below line we are extracting json response and displaying it in a text view.
                        JSONObject payObj = new JSONObject(paymentDetails);
                        String payID = payObj.getJSONObject("response").getString("id");
                        String state = payObj.getJSONObject("response").getString("state");
                        Random rnd = new Random();
                        int number = rnd.nextInt(999999);
                        String ordernum = String.format("%06d", number);
                        OrderModel newOrder = new OrderModel();
                        newOrder.setOrdernum(ordernum);
                        newOrder.setOrderAmount(totalamount);
                        String productslist = "";
                        for(CartModel singleitem: cartitems){
                            productslist = productslist + "," + singleitem.getProductName();
                            SalesModel saleitem = new SalesModel();
                            saleitem.setProductName(singleitem.getProductName());
                            saleitem.setQuantitysold(singleitem.getQuantity());
                            rootNode.getReference().child(SalesModel.class.getSimpleName()).child(singleitem.getProductName()).setValue(saleitem);
                        }
                        newOrder.setProductsList(productslist);
                        newOrder.setPaymentID(payID);
                        newOrder.setUserName(curruser.getUserName());
                        System.out.println("Payment ID and state " + payID + state);
                        orderreference.child(ordernum).setValue(newOrder);
                        cartitems.clear();
                        cartreference.removeValue();
                        usercartref.removeValue();
                        AlertDialog.Builder mBuilder = new AlertDialog.Builder(CartActivity.this);
                        View mView = getLayoutInflater().inflate(R.layout.order_placed, null);
                        TextView order_num = findViewById(R.id.order_number);
                        TextView payment_id = findViewById(R.id.payment_id);
                        Button gotoorders = findViewById(R.id.gotoorders);
                        order_num.setText(ordernum);
                        payment_id.setText(payID);
                        mBuilder.setView(mView);
                        final AlertDialog dialog = mBuilder.create();
                        dialog.show();
                        gotoorders.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent ordersintent = new Intent(CartActivity.this, OrdersActivity.class);
                                startActivity(ordersintent);
                            }
                        });

                    } catch (JSONException e) {
                        // handling json exception on below line
                        e.printStackTrace();
                    }
                }
            } else if (resultCode == Activity.RESULT_CANCELED) {
                // on below line we are checking the payment status.

            } else if (resultCode == PaymentActivity.RESULT_EXTRAS_INVALID) {
                // on below line when the invalid paypal config is submitted.
            }
        }
    }
}