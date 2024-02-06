package com.example.pharma;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.core.content.ContextCompat;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pharma.Model.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;

import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    EditText userName, password;
    Button login,register,forgotpwd;
    TextView engLang,spnLang;
    DatabaseReference reference;
    String user_type, fullname, user_name, email;
    UserModel currentUser;
    CheckBox rememberme;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        setContentView(R.layout.activity_main);
        userName = findViewById(R.id.loginusername);
        password = findViewById(R.id.loginpassword);
        login = findViewById(R.id.loginBtn);
        register = findViewById(R.id.loginregister);
        engLang = findViewById(R.id.englanguage);
        spnLang = findViewById(R.id.spanishlanguage);
        rememberme = findViewById(R.id.rememberme);
        forgotpwd = findViewById(R.id.forgotpassword);
        mAuth = FirebaseAuth.getInstance();

        currentUser = new UserModel();
        Window window = MainActivity.this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(ContextCompat.getColor(MainActivity.this, R.color.black));

        SharedPreferences prefs = getSharedPreferences("usercredentials", MODE_PRIVATE);
        String userid = prefs.getString("userid", "");
        String pwd = prefs.getString("password", "");
        if(userid != null && pwd != null){
            userName.setText(userid);
            password.setText(pwd);
            rememberme.setChecked(true);
        }

//        SharedPreferences prefs = getSharedPreferences("Logged_in", MODE_PRIVATE);
//        Gson gs = new Gson();
//        String js = prefs.getString("Userobject", "");
//        UserModel curruser = gs.fromJson(js, UserModel.class);
//        if(curruser != null){
//            Intent intent = new Intent(MainActivity.this, HomeActivity.class);
//            intent.putExtra("currentuser", curruser);
//            startActivity(intent);
//        }

        rememberme.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if(!b){
                    SharedPreferences prefs = getSharedPreferences("usercredentials", MODE_PRIVATE);
                    String userid = prefs.getString("userid", "");
                    String pwd = prefs.getString("password", "");
                    if(userid != null && pwd != null){
                        prefs.edit().remove("userid").apply();
                        prefs.edit().remove("password").apply();
                    }
                }
            }
        });
        forgotpwd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(MainActivity.this);
                View mView = getLayoutInflater().inflate(R.layout.password_reset, null);
                final EditText mEmail = (EditText) mView.findViewById(R.id.etEmail);
                Button mLogin = (Button) mView.findViewById(R.id.btnLogin);

                mBuilder.setView(mView);
                final AlertDialog dialog = mBuilder.create();
                dialog.show();
                mLogin.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        FirebaseAuth.getInstance().sendPasswordResetEmail(mEmail.getText().toString());
                        Toast.makeText(MainActivity.this, "Email sent successfully", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });


        engLang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setLocale("en");
                recreate();
            }
        });

        spnLang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setLocale("es");
                recreate();
            }
        });


        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Empty check validations in username and password fields
                if(userName.getText().toString().isEmpty()){
                    userName.setError(getResources().getString(R.string.username_empty_error));
                    userName.requestFocus();
                    return;
                }
                if(password.getText().toString().isEmpty()){
                    password.setError(getResources().getString(R.string.password_empty_error));
                    password.requestFocus();
                    return;
                }

                // Code to authenticate the user by retrieving username and password fields from firebase instance
                reference = FirebaseDatabase.getInstance().getReference("UserModel");
                Query query = reference.orderByChild("userName").equalTo(userName.getText().toString().toLowerCase());
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if(snapshot.exists()){
                            userName.setError(null);
                            //String pwdfromdb = snapshot.child(userName.getText().toString().toLowerCase()).child("password").getValue(String.class);
                                userName.setError(null);
                                user_name = snapshot.child(userName.getText().toString().toLowerCase()).child("userName").getValue(String.class);
                                user_type = snapshot.child(userName.getText().toString().toLowerCase()).child("userType").getValue(String.class);
                                fullname = snapshot.child(userName.getText().toString().toLowerCase()).child("fullName").getValue(String.class);
                                email = snapshot.child(userName.getText().toString().toLowerCase()).child("email").getValue(String.class);
                                Intent intent = new Intent(MainActivity.this, HomeActivity.class);
                                currentUser = snapshot.child(userName.getText().toString().toLowerCase()).getValue(UserModel.class);
                                mAuth.signInWithEmailAndPassword(email, password.getText().toString()).addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            SharedPreferences.Editor editor = getSharedPreferences("Logged_in", MODE_PRIVATE).edit();
                                            Gson gson = new Gson();
                                            String json = gson.toJson(currentUser);
                                            editor.putString("Userobject", json);
                                            editor.apply();

                                            if(rememberme.isChecked()) {
                                                SharedPreferences.Editor editor2 = getSharedPreferences("usercredentials", MODE_PRIVATE).edit();
                                                editor2.putString("userid", userName.getText().toString());
                                                editor2.putString("password", password.getText().toString());
                                                editor2.apply();
                                            }
                                            intent.putExtra("currentuser", currentUser);
                                            intent.putExtra("userType", user_type);
                                            intent.putExtra("username", user_name);
                                            intent.putExtra("userFullName", fullname);
                                            startActivity(intent);
                                        } else {
                                            Toast.makeText(MainActivity.this, "Login Failed!!", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });

                        }else {
                            userName.setError("No such user exists");
                            userName.requestFocus();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });


        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, UserRegistration.class);
                startActivity(intent);
            }
        });
    }


    // Method to display exit alert to the user when trying to close the app
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(MainActivity.this)
                .setTitle("Are you sure, you want to exit?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.addCategory(Intent.CATEGORY_HOME);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                    }
                })
                .setNegativeButton("No", null)
                .show();
    }

    // methods to set locale language

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