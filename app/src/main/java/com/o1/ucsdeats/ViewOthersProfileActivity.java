package com.o1.ucsdeats;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseAuth;
import android.content.Intent;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.database.ValueEventListener;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import java.util.Map;

public class ViewOthersProfileActivity extends AppCompatActivity {

    private TextView mEmailView;
    private TextView mNameView;
    private TextView mInTroductionView;
    private TextView mPhoneView;
    private TextView mGenderView;
    private TextView mMajorView;
    private  ImageView mProfilePhoto;

    private FirebaseUser User;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabaseRef;
    private String otherUserUid;
    final FirebaseStorage storage = FirebaseStorage.getInstance();
    private Button addOrDeleteFriend;
    private Boolean isFriend;
    private DatabaseReference mDatabase;
    private DatabaseReference mDatabase2;
    private User currentUser;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_others_profile);

        mAuth = FirebaseAuth.getInstance();
        User = mAuth.getCurrentUser();

        isFriend = getIntent().getBooleanExtra(Constant.isFriendSTR,false);

        otherUserUid = getIntent().getStringExtra(Constant.otherUserUidSTR);

        addOrDeleteFriend = (Button) findViewById(R.id.AddFriend);

        mDatabase = FirebaseDatabase.getInstance().getReference(Constant.userSTR).
                child(User.getUid().toString());
        mDatabase2 = FirebaseDatabase.getInstance().getReference(Constant.userSTR)
                .child(otherUserUid);

        //Reference to current user database
        mDatabase.addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentUser = dataSnapshot.getValue(User.class);
                isFriend = currentUser.friendList.contains(otherUserUid);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message

            }
        });


        //Reference to the other user information
        mDatabase2.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);

                mEmailView = (TextView) findViewById(R.id.InfoEmail);
                mEmailView.setText(user.email);

                mGenderView = (TextView) findViewById(R.id.InfoGender);
                mGenderView.setText(user.gender);

                mMajorView = (TextView) findViewById(R.id.InfoMajor);
                mMajorView.setText(user.major);

                mNameView = (TextView) findViewById(R.id.InfoName);
                mNameView.setText(user.name);

                mPhoneView = (TextView) findViewById(R.id.InfoPhone);
                mPhoneView.setText(user.phoneNumber);

                mInTroductionView = (TextView) findViewById(R.id.InfoIntroduction);
                mInTroductionView.setText(user.intro);

                StorageReference storageRef = storage.getReference();
                StorageReference islandRef = storageRef.child(Constant.profilePathSTR +
                        otherUserUid + Constant.jpgSTR);

                final long ONE_MEGABYTE = 1024 * 1024;
                islandRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(
                        new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        // Data for "images/island.jpg" is returns, use this as needed
                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                        mProfilePhoto = (ImageView)findViewById(R.id.ProfilePhoto);
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

            }
        });

        if(isFriend){
            addOrDeleteFriend.setText("Delete Friend");
            if(User.getUid().equals(otherUserUid)){
                addOrDeleteFriend.setVisibility(View.GONE);
            }
        }else{
            addOrDeleteFriend.setText("Add Friend");
        }

        addOrDeleteFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(isFriend){
                    addOrDeleteFriend.setText("Add Friend");
                    currentUser.deleteFriend(otherUserUid);
                    Map<String, Object> postValues = currentUser.toMap();
                    mDatabase.updateChildren(postValues);
                }else{
                    addOrDeleteFriend.setText("Delete Friend");
                    currentUser.addFriend(otherUserUid);
                    Map<String, Object> postValues = currentUser.toMap();
                    mDatabase.updateChildren(postValues);
                }
                finish();

            }
        });

    }
}
