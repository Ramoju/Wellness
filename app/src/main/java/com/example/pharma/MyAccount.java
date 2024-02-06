package com.example.pharma;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.pharma.Model.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Locale;

public class MyAccount extends AppCompatActivity {

    Toolbar toolbar;
    EditText fullName,email,userName,phoneNo;
    UserModel curruser;
    Button update;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        setContentView(R.layout.activity_my_account);

        toolbar = findViewById(R.id.userprofiletoolbar);
        fullName = findViewById(R.id.user_fullname);
        email = findViewById(R.id.user_email_address);
        userName = findViewById(R.id.user_username);
        phoneNo = findViewById(R.id.user_phone_num);
        update = findViewById(R.id.updateuserdetails);
        curruser = getIntent().getParcelableExtra("curruser");

        fullName.setText(curruser.getFullName());
        email.setText(curruser.getEmail());
        phoneNo.setText(curruser.getPhoneNum());
        userName.setText(curruser.getUserName());


        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Window window = MyAccount.this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(ContextCompat.getColor(MyAccount.this, R.color.black));

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MyAccount.this, HomeActivity.class));
            }
        });

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                reference = FirebaseDatabase.getInstance().getReference().child(UserModel.class.getSimpleName()).child(curruser.getUserName());
                reference.child("fullName").setValue(fullName.getText().toString());
                reference.child("phoneNum").setValue(phoneNo.getText().toString());
                reference.child("email").setValue(email.getText().toString().trim());
                reference.child("userName").setValue(userName.getText().toString());

                Toast.makeText(MyAccount.this, "Details updated successfully", Toast.LENGTH_SHORT).show();

                if(!email.getText().toString().equalsIgnoreCase(curruser.getEmail())){
                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                    // Get auth credentials from the user for re-authentication
                    AuthCredential credential = EmailAuthProvider
                            .getCredential(curruser.getEmail(), curruser.getPassword()); // Current Login Credentials \\
                    // Prompt the user to re-provide their sign-in credentials
                    user.reauthenticate(credential)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    //Now change your email address \\
                                    //----------------Code for Changing Email Address----------\\
                                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                    user.updateEmail(email.getText().toString().trim())
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        Toast.makeText(MyAccount.this, "Email updated successfully", Toast.LENGTH_SHORT).show();
                                                    }
                                                }
                                            });
                                    //----------------------------------------------------------\\
                                }
                            });
                }
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