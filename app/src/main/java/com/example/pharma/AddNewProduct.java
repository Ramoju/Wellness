package com.example.pharma;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.READ_EXTERNAL_STORAGE;
import static android.Manifest.permission.WRITE_EXTERNAL_STORAGE;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.pharma.Adapter.ImagesAdapter;
import com.example.pharma.Model.ImageModel;
import com.example.pharma.Model.ProductModel;
import com.google.android.gms.common.util.CollectionUtils;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;


import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class AddNewProduct extends AppCompatActivity {
    private EditText productName;
    private EditText productDescription;
    private EditText productSpanishDescription;
    private EditText productPrice;
    private EditText productQuantity;
    private EditText productCategory;
    private ImageView productImage;
    private Button attachImage;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int PICK_IMAGES_CODE = 0;
    private static final int READ_EXTERNAL_STORAGE_REQUEST_CODE = 100;
    public static final int CAMERA_ACCESS_REQUEST_CODE = 101;
    private Button mSaveButton;
    private Toolbar toolbar;
    private ArrayList<ImageModel> newProductImages;
    private ArrayList<String> imageURIs;
    private RecyclerView imgsRV;
    ImagesAdapter adapter;
    FirebaseDatabase rootNode;
    DatabaseReference reference;
    StorageReference storageReference;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        loadLocale();
        setContentView(R.layout.activity_add_new_product);

        toolbar = findViewById(R.id.addproducttoolbar);
        productName = findViewById(R.id.productname);
        productDescription = findViewById(R.id.productdescription);
        productPrice = findViewById(R.id.productprice);
        productQuantity = findViewById(R.id.productquantity);
        productCategory = findViewById(R.id.productcategory);
        productImage = findViewById(R.id.productimage);
        attachImage = findViewById(R.id.attachimages);
        mSaveButton = findViewById(R.id.button_save);
        productSpanishDescription = findViewById(R.id.productspanishdesc);
        imgsRV = findViewById(R.id.imagesRV);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        imageURIs = new ArrayList<String>();
        newProductImages = new ArrayList<>();
        storageReference = FirebaseStorage.getInstance().getReference();
        adapter = new ImagesAdapter(newProductImages, AddNewProduct.this);
        imgsRV.setHasFixedSize(true);
        imgsRV.setLayoutManager(new LinearLayoutManager(this, RecyclerView.HORIZONTAL, false));
        imgsRV.setAdapter(adapter);

        Window window = AddNewProduct.this.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.setStatusBarColor(ContextCompat.getColor(AddNewProduct.this, R.color.skyblue));

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(AddNewProduct.this, HomeActivity.class));
            }
        });

        attachImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder ab = new AlertDialog.Builder(AddNewProduct.this);
                ab.setTitle("Choose image")
                        .setIcon(R.drawable.ic_image)
                        .setMessage("Select an option to upload image")
                        .setNegativeButton("Gallery",((dialog, which) -> {
                            if(ActivityCompat.checkSelfPermission(AddNewProduct.this, READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(AddNewProduct.this, WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                                imageURIs.clear();
                                pickImageIntent();
                            } else {
                                ActivityCompat.requestPermissions(
                                        AddNewProduct.this,
                                        new String[] {Manifest.permission.READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE},
                                        READ_EXTERNAL_STORAGE_REQUEST_CODE
                                );
                            }
                        } ))
                        .setNeutralButton("Cancel",null)
                        .setPositiveButton("Camera",(dialog, which) -> {
                            if(ActivityCompat.checkSelfPermission(AddNewProduct.this, READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(AddNewProduct.this, WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(AddNewProduct.this, CAMERA) == PackageManager.PERMISSION_GRANTED) {
                                imageURIs.clear();
                                dispatchTakePictureIntent();
                            } else {
                                ActivityCompat.requestPermissions(
                                        AddNewProduct.this,
                                        new String[] {Manifest.permission.READ_EXTERNAL_STORAGE, WRITE_EXTERNAL_STORAGE, CAMERA},
                                        CAMERA_ACCESS_REQUEST_CODE
                                );
                            }
                        });
                AlertDialog dialog = ab.create();

                dialog.show();
            }
        });

        mSaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (productName.getText().toString().isEmpty()) {
                    productName.setError("Product name cannot be empty");
                    productName.requestFocus();
                    return;
                }
                if (productDescription.getText().toString().isEmpty()) {
                    productDescription.setError("Description cannot be empty");
                    productDescription.requestFocus();
                    return;
                }
                if (productPrice.getText().toString().isEmpty()) {
                    productPrice.setError("Product Price cannot be empty");
                    productPrice.requestFocus();
                    return;
                }
                if (productQuantity.getText().toString().isEmpty()) {
                    productQuantity.setError("Product quantity cannot be empty");
                    productQuantity.requestFocus();
                    return;
                }
                if (productCategory.getText().toString().isEmpty()) {
                    productCategory.setError("Product Category cannot be empty");
                    productCategory.requestFocus();
                    return;
                }
                if(newProductImages.isEmpty()){
                    AlertDialog.Builder builder = new AlertDialog.Builder(AddNewProduct.this);
                    builder.setTitle("Alert")
                            .setMessage("Please attach atleast one photo to the product")
                            .setNeutralButton("Okay",null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    return;
                }
                ProductModel newProduct = new ProductModel();
                newProduct.setName(productName.getText().toString());
                newProduct.setPrice(Double.parseDouble(productPrice.getText().toString()));
                newProduct.setDescription(productDescription.getText().toString());
                newProduct.setSpanishDescription(productSpanishDescription.getText().toString());
                newProduct.setCategory(productCategory.getText().toString());
                newProduct.setQuantity(Double.parseDouble(productQuantity.getText().toString()));
                rootNode = FirebaseDatabase.getInstance();
                reference = rootNode.getReference(ProductModel.class.getSimpleName());
                String key = reference.push().getKey();
                reference.child(key).setValue(newProduct);
                if(newProductImages.size()>0){
                    for(ImageModel img: newProductImages){
                        uploadImagestoFirebase(img, productName.getText().toString(), key);
                    }
                }
                Toast.makeText(AddNewProduct.this, "New product added", Toast.LENGTH_SHORT).show();

                productName.setText("");
                productDescription.setText("");
                productCategory.setText("");
                productPrice.setText("");
                productQuantity.setText("");
            }
        });
    }
    //Method to upload images to Firebase storage
    private void uploadImagestoFirebase(ImageModel img, String productname, String productkey) {
        DatabaseReference imgreference = reference.child(productkey).child(ImageModel.class.getSimpleName());
        final StorageReference fileRef = storageReference.child(System.currentTimeMillis() + "." + getFileExtension(Uri.parse(img.getImageURI())));
                fileRef.putFile(Uri.parse(img.getImageURI())).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                img.setImageURI(uri.toString());
                                img.setProduct(productname);
                                String uniqueKey = imgreference.push().getKey();
                                imgreference.child(uniqueKey).setValue(img);
                            }
                        });
                    }
                }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(AddNewProduct.this, "Image uploading to firebase failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    //Method to get image file extension to store the image to Firebase storage

    private String getFileExtension(Uri parse) {
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return  mime.getExtensionFromMimeType(cr.getType(parse));
    }

    //Method to open camera to capture photos
    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        try {
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        } catch (ActivityNotFoundException e) {
            e.printStackTrace();
        }
    }
    //Method to open gallery to pick images
    private void pickImageIntent(){
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Picture"), PICK_IMAGES_CODE);
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGES_CODE) {
            if(resultCode == Activity.RESULT_OK) {
                if(data.getClipData() != null) {
                    int count = data.getClipData().getItemCount();
                    for(int i = 0; i < count; i++) {
                        Uri imageUri = data.getClipData().getItemAt(i).getUri();
                        ImageModel newImage = new ImageModel();
                        newImage.setImageURI(imageUri.toString());
                        newImage.setProduct(productName.getText().toString());
                        newProductImages.add(newImage);
                        adapter.notifyDataSetChanged();
                    }
                }else if(data.getData() != null) {
                    Uri imageUri=data.getData();
                    ImageModel newImage = new ImageModel();
                    newImage.setImageURI(imageUri.toString());
                    newImage.setProduct(productName.getText().toString());
                    newProductImages.add(newImage);
                    adapter.notifyDataSetChanged();
                }
            }
        }
        // -------------for camera capture -------
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bitmap photo = (Bitmap) data.getExtras().get("data");
            // CALL THIS METHOD TO GET THE URI FROM THE BITMAP
            Uri tempUri = getImageUri(getApplicationContext(), photo);
            ImageModel newImage = new ImageModel();
            newImage.setImageURI(tempUri.toString());
            newImage.setProduct(productName.getText().toString());
            newProductImages.add(newImage);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == READ_EXTERNAL_STORAGE_REQUEST_CODE){
            pickImageIntent();
        }else if (requestCode == CAMERA_ACCESS_REQUEST_CODE){
            dispatchTakePictureIntent();
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


}