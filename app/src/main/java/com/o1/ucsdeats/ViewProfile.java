package com.o1.ucsdeats;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.Collections;

public class ViewProfile extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private TextView mNameView;
    private TextView mMajorView;
    private TextView mGenderView;
    private TextView mEmailView;
    private TextView mPhoneNumberView;
    private TextView mIntroductionView;
    private DatabaseReference mDatabase;
    private ImageView mProfilePhoto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_profile);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        final FirebaseStorage storage = FirebaseStorage.getInstance();

        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get User object and use the values to update the UI

                User user = dataSnapshot.child(Constant.userSTR).child(mCurrentUser.getUid())
                        .getValue(User.class);

                //Display User's name
                mNameView = (TextView) findViewById(R.id.Name);
                mNameView.setText(user.name);

                //Display User's email
                mEmailView = (TextView) findViewById(R.id.email_view);
                mEmailView.setText(user.email);

                //Display User's major
                mMajorView = (TextView) findViewById(R.id.major_view);
                mMajorView.setText(user.major);

                //Display User's phoneNumber
                mPhoneNumberView = (TextView) findViewById(R.id.phone_view);
                mPhoneNumberView.setText(user.phoneNumber);

                //Display User's gender
                mGenderView = (TextView) findViewById(R.id.gender_view);
                mGenderView.setText(user.gender);

                //Display User's introduction
                mIntroductionView = (TextView)findViewById(R.id.Introduction);
                mIntroductionView.setText(user.intro);

                StorageReference storageRef = storage.getReference();
                StorageReference islandRef = storageRef.child(Constant.profilePathSTR + mCurrentUser
                        .getUid() + Constant.jpgSTR);

                final long ONE_MEGABYTE = 1024 * 1024;
                islandRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        // Data for "images/island.jpg" is returns, use this as needed
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                        mProfilePhoto = (ImageView)findViewById(R.id.imageView2);
                        mProfilePhoto.setScaleType(ImageView.ScaleType.FIT_START);
                        mProfilePhoto.setAdjustViewBounds(true);
                        mProfilePhoto.setImageBitmap(bitmap);

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Handle any errors
                    }
                });

            }


            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w("profile", "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        };
        mDatabase.addValueEventListener(postListener);

    }

    public void deleteAccount(View view)
    {
        deletePostMadeByUser(mCurrentUser.getUid());
        mCurrentUser.delete();
        mAuth.signOut();
        Context context = getApplicationContext();
        CharSequence text = "AccountDeleted!";
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
        finish();
    }

    public void changePassword(View view)
    {
        Intent intent = new Intent(getApplicationContext(), ChangePassword.class);
        startActivity(intent);
    }

    public void actionLogout (View view)
    {
        Context context = getApplicationContext();
        mAuth.signOut();
        CharSequence text = "Logged out!";
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();
        finish();
    }

    public void editProfile (View view) {
        Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
        startActivity(intent);
        finish();

    }

    public void deletePostMadeByUser(final String uid){

        mDatabase = FirebaseDatabase.getInstance().getReference();

        ValueEventListener postListener = new ValueEventListener() {

            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                for(DataSnapshot UpperPosts : dataSnapshot.child(Constant.postSTR).getChildren()){
                    for(DataSnapshot Posts : UpperPosts.getChildren())
                        if(Posts.getValue(Post.class).user == uid) {
                            mDatabase.child(Constant.postSTR).child(Posts.getValue(Post.class).
                                    restaurantName).child(uid).setValue(null);
                        }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        mDatabase.addListenerForSingleValueEvent(postListener);

    }

}
