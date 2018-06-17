package com.o1.ucsdeats;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

public class UploadRestaurantPhotoActivity extends AppCompatActivity {

    private static final int CAMERA_PIC_REQUEST = 1;
    private static final int GALLERY_PIC_REQUEST = 2;

    private String mRestaurantName = null;
    private String mRestaurantFolder = null;
    private Bitmap mImage = null;
    private TextView mDescription = null;
    private FirebaseAuth mAuth;
    private FirebaseStorage mStorage;
    private FirebaseDatabase mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_upload_restaurant_photo);

        Intent intent = getIntent();
        mRestaurantName = intent.getStringExtra("restaurant_name");
        mRestaurantFolder = intent.getStringExtra("restaurant_folder");
        mAuth = FirebaseAuth.getInstance();
        mStorage = FirebaseStorage.getInstance();
        mDatabase = FirebaseDatabase.getInstance();
        mDescription = findViewById(R.id.AddDescription);

        Button mTakePhoto = (Button) findViewById(R.id.TakePhoto);
        mTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, CAMERA_PIC_REQUEST);
            }
        });

        Button mChoosePhoto = (Button) findViewById(R.id.ChoosePhoto);
        mChoosePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent,GALLERY_PIC_REQUEST);
            }
        });

        Button mSubmit = findViewById(R.id.submit_button);
        mSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(mAuth.getCurrentUser() == null) {
                    System.err.println("User not logged in");
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "Must be logged in to upload a photo", Toast.LENGTH_LONG);
                    toast.show();
                    return;
                }
                if(mImage == null) {
                    System.err.println("No image selected");
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "An image must be selected to upload", Toast.LENGTH_LONG);
                    toast.show();
                    return;
                }

                final String name = Utils.createDateStamp();
                final StorageReference ref = mStorage.getReference().child(Constant.foodSTR)
                        .child(mRestaurantFolder).child(name + Constant.jepgSTR);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                mImage.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] compressedData = baos.toByteArray();
                UploadTask uploadTask = ref.putBytes(compressedData);
                uploadTask.addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        System.err.println("Error occurred uploading image: "
                                + exception.toString());
                        Toast toast = Toast.makeText(getApplicationContext(),
                                "Failed to upload photo", Toast.LENGTH_LONG);
                        toast.show();
                    }
                }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // taskSnapshot.getMetadata() contains file metadata such as size,
                        // content-type, and download URL.
//                        Uri downloadUrl = taskSnapshot.getDownloadUrl();
                        System.out.println("Succesfully uploaded image");
                        DatabaseReference dbRef = mDatabase.getReference().child
                                (Constant.restaurantPhotosSTR).child(mRestaurantName).child(name);
                        UserUploadedPhoto upload = new UserUploadedPhoto(ref.toString(),
                                mDescription.getText().toString(), mAuth.getCurrentUser().getUid());
                        dbRef.setValue(upload);
                        Toast toast = Toast.makeText(getApplicationContext(),
                                "Photo Uploaded", Toast.LENGTH_LONG);
                        toast.show();
                        Intent restaurantActivity = new Intent(getApplicationContext(),
                                RestaurantActivity.class);
                        restaurantActivity.putExtra("Name", mRestaurantName);
                        finish();
                        //startActivity(restaurantActivity);
                    }
                });

            }
        });
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_PIC_REQUEST) {
            try {
                mImage = (Bitmap) data.getExtras().get("data");
                ImageView imageView = (ImageView) findViewById(R.id.RestaurantPhoto);
                imageView.setImageBitmap(mImage);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (requestCode == GALLERY_PIC_REQUEST) {
            if(data == null)
                return;
            Uri imageUri = data.getData();
            try {
                mImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                ImageView imageView = (ImageView) findViewById(R.id.RestaurantPhoto);
                imageView.setImageBitmap(mImage);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String createDateStamp() {
        return new SimpleDateFormat("MMddyyyyHHmmss").format(Calendar.getInstance()
                .getTime());
    }
}