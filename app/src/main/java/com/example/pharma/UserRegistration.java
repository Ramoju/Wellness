package com.example.pharma;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.telephony.PhoneNumberUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.pharma.Model.UserModel;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.Locale;

public class UserRegistration extends AppCompatActivity {

    EditText fullName,email,password,userName,phoneNo;
    Button register,goToLogin;
    FirebaseDatabase rootNode;
    DatabaseReference reference;
    private int INTERNET_PERMISSION_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        setContentView(R.layout.activity_user_registration);

        fullName = findViewById(R.id.userfullname);
        email = findViewById(R.id.useremail);
        password = findViewById(R.id.userpassword);
        userName = findViewById(R.id.username);
        phoneNo = findViewById(R.id.phonenum);
        register = findViewById(R.id.registerbtn);
        goToLogin = findViewById(R.id.loginbtninregister);

        Window window = UserRegistration.this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(ContextCompat.getColor(UserRegistration.this, R.color.black));


        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ContextCompat.checkSelfPermission(UserRegistration.this, Manifest.permission.INTERNET) == PackageManager.PERMISSION_GRANTED) {

                    // Empty check validations for user registration fields
                    if (fullName.getText().toString().isEmpty()) {
                        fullName.setError("Full name cannot be empty");
                        fullName.requestFocus();
                        return;
                    }
                    if (userName.getText().toString().isEmpty()) {
                        userName.setError("Username cannot be empty");
                        userName.requestFocus();
                        return;
                    }
                    if (phoneNo.getText().toString().isEmpty()) {
                        phoneNo.setError("Phone number cannot be empty");
                        phoneNo.requestFocus();
                        return;
                    }
                    if (email.getText().toString().isEmpty()) {
                        email.setError("Email address cannot be empty");
                        email.requestFocus();
                        return;
                    }
                    if (password.getText().toString().isEmpty()) {
                        password.setError("Password cannot be empty");
                        password.requestFocus();
                        return;
                    }

                    if (!email.getText().toString().matches("[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+")) {
                        email.setError("Invalid email address");
                        email.requestFocus();
                        return;
                    }

                    if (!PhoneNumberUtils.isGlobalPhoneNumber(phoneNo.getText().toString())) {
                        phoneNo.setError("Invalid Phone number");
                        phoneNo.requestFocus();
                        return;
                    }

                    if (userName.getText().toString().length() < 6 || userName.getText().toString().length() > 15) {
                        userName.setError("Username must be minimum of 6 characters and maximum of 15 characters");
                        userName.requestFocus();
                        return;
                    }

                    if (password.getText().toString().length() < 6) {
                        password.setError("Password cannot be less than 6 characters");
                        password.requestFocus();
                        return;
                    }


                    UserModel newUser = new UserModel();
                    newUser.setFullName(fullName.getText().toString());
                    newUser.setUserName(userName.getText().toString().toLowerCase());
                    newUser.setPhoneNum(phoneNo.getText().toString());
                    newUser.setEmail(email.getText().toString());
                    newUser.setPassword(password.getText().toString());
                    newUser.setUserType("User");

                    // Code to save the registered user data into firebase instance
                    rootNode = FirebaseDatabase.getInstance();
                    reference = rootNode.getReference(UserModel.class.getSimpleName());

                    Query query = reference.orderByChild("userName").equalTo(userName.getText().toString().toLowerCase());

                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            // Code to maintain unique User ID
                            if(snapshot.exists()){
                                userName.setError("Same User ID already exists");
                            }
                            else {
                                reference.child(userName.getText().toString()).setValue(newUser).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        FirebaseAuth.getInstance().createUserWithEmailAndPassword(email.getText().toString(), password.getText().toString()).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                            @Override
                                            public void onComplete(@NonNull Task<AuthResult> task) {
                                                if(task.isSuccessful()){
                                                    Toast.makeText(UserRegistration.this, "User registered successfully", Toast.LENGTH_SHORT).show();
                                                    fullName.setText("");
                                                    userName.setText("");
                                                    email.setText("");
                                                    phoneNo.setText("");
                                                    password.setText("");
                                                } else {
                                                    Toast.makeText(UserRegistration.this, "Registration failed!!", Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }
                                });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });

                } else {
                    requestInternetPermission();
                }
            }

        });

        goToLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(UserRegistration.this, MainActivity.class);
                startActivity(intent);
            }
        });
    }

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

    private void requestInternetPermission() {
        if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.INTERNET)){
            new AlertDialog.Builder(this)
                    .setTitle("Permission Needed")
                    .setMessage("Internet is required for user registration, login and other operations in the app")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            ActivityCompat.requestPermissions(UserRegistration.this, new String[] {Manifest.permission.INTERNET}, INTERNET_PERMISSION_CODE);
                        }
                    })
                    .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .create().show();
        }else {
            ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.INTERNET}, INTERNET_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == INTERNET_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            } else {
                return;
            }
        }
    }
}