package com.example.ecommerce;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;

import javax.xml.validation.Validator;

public class AdminAddNewProductActivity extends AppCompatActivity {

    private  String categoryName,Discription,price,pName,saveCurentDate,SaveCurrentTime;
    private Button AddNewProductButton;
    private EditText InputProductName,InputProductDiscription,InputProductPrice;
    private ImageView InputProductImage;
    private static final int Gallarypic = 1;
    private Uri ImageUri;
    private String productRandomKey, downloadImageUrl;
    private StorageReference productImagesRef;
    private DatabaseReference productRef;
    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin_add_new_product);



        categoryName = getIntent().getExtras().get("category").toString();
        productImagesRef = FirebaseStorage.getInstance().getReference().child("product Images");
        productRef = FirebaseDatabase.getInstance().getReference().child("products");


        AddNewProductButton = (Button) findViewById(R.id.add_new_product);
        InputProductName = (EditText) findViewById(R.id.product_name);
        InputProductDiscription = (EditText) findViewById(R.id.product_description);
        InputProductPrice = (EditText) findViewById(R.id.product_price);
        InputProductImage =(ImageView) findViewById(R.id.select_product_image);
        loadingBar = new ProgressDialog(this);




        InputProductImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                OpenGallery();
            }
        });



        AddNewProductButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ValidateProductData();


            }
        });



    }




    private void OpenGallery() {
        Intent galleryIntent = new Intent();
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("image/*");
         startActivityForResult(galleryIntent, Gallarypic);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == Gallarypic && resultCode == RESULT_OK && data!= null){
            ImageUri = data.getData();
            InputProductImage.setImageURI(ImageUri);
        }
    }


    private void ValidateProductData() {


            Discription  = InputProductDiscription.getText().toString();
            price = InputProductPrice.getText().toString();
            pName = InputProductName.getText().toString();


            if(ImageUri == null){
                Toast.makeText(this,"product Image is required ", Toast.LENGTH_LONG ).show();
            }
            else if (TextUtils.isEmpty(Discription)){
                Toast.makeText(this,"Please enter product discription", Toast.LENGTH_SHORT).show();
            }
            else if (TextUtils.isEmpty(price)){
                Toast.makeText(this,"Please enter product price", Toast.LENGTH_SHORT).show();
            }
            else if (TextUtils.isEmpty(pName)){
                Toast.makeText(this,"Please enter product Name", Toast.LENGTH_SHORT).show();
            }
            else{
                storeProductInformaiton();

            }


    }

    private void storeProductInformaiton()
    {
        loadingBar.setTitle("Adding new product");
        loadingBar.setMessage("Please wait we are adding  the new Product ");

        loadingBar.setCanceledOnTouchOutside(false);
        loadingBar.show();



        Calendar calendar = Calendar.getInstance();

        SimpleDateFormat currentDate = new SimpleDateFormat("MMM dd, yyy");
        saveCurentDate = currentDate.format(calendar.getTime());

        SimpleDateFormat currentTime = new SimpleDateFormat("HH: mm:ss a");
        SaveCurrentTime = currentDate.format(calendar.getTime());

        productRandomKey = saveCurentDate +SaveCurrentTime;

        final StorageReference filePath= productImagesRef.child(ImageUri.getLastPathSegment() + productRandomKey+".jpg");

        final UploadTask uploadTask = filePath.putFile(ImageUri);


        //if any error coocur while uplopadinng image
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                String message = e.toString();
                Toast.makeText(AdminAddNewProductActivity.this,"Error "+ message ,Toast.LENGTH_SHORT).show();
                loadingBar.dismiss();

            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(AdminAddNewProductActivity.this,"Product image uploades sucessfully", Toast.LENGTH_SHORT ).show();

                Task<Uri> uriTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        if (!task.isSuccessful()){
                            throw task.getException();
                        }
                        downloadImageUrl = filePath.getDownloadUrl().toString();
                        return filePath.getDownloadUrl();
                    }

                }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                    @Override
                    public void onComplete(@NonNull Task<Uri> task) {
                        if (task.isSuccessful()){
                            downloadImageUrl = task.getResult().toString();
                            Toast.makeText(AdminAddNewProductActivity.this,"Product image saved to database saved to database sucessful",Toast.LENGTH_SHORT).show();

                            SaveProductInfoToDatabase();
                        }

                    }
                });

            }
        });


    }

    private void SaveProductInfoToDatabase() {
        HashMap<String,Object> productMap  = new HashMap<>();
        productMap.put("pdi", productRandomKey);
        productMap.put("date",saveCurentDate);
        productMap.put("time",SaveCurrentTime);
        productMap.put("description",Discription);
        productMap.put("image",downloadImageUrl);
        productMap.put("category",categoryName);
        productMap.put("price",price);
        productMap.put("pname",pName);
        productRef.child(productRandomKey).updateChildren(productMap).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){

                    Intent intent = new Intent(AdminAddNewProductActivity.this,AdminCategoryActivity.class);
                    startActivity(intent);

                    loadingBar.dismiss();
                    Toast.makeText(AdminAddNewProductActivity.this,"Produt is addes sucessfully",Toast.LENGTH_SHORT).show();
                }
                else {
                    loadingBar.dismiss();
                        String message =task.getException().toString();
                    Toast.makeText(AdminAddNewProductActivity.this,"Error" + message,Toast.LENGTH_SHORT).show();
                }

            }
        });



    }

}
