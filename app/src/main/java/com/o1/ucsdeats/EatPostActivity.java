package com.o1.ucsdeats;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.media.Image;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;
import java.util.Map;

public class EatPostActivity extends AppCompatActivity {
    private String JOIN = Constant.JOIN;
    private String LEAVE = Constant.LEAVE;

    private String hostUid;
    private String time;
    private String countDown;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private DatabaseReference mDatabase;
    private Post post;
    private String restaurantName;
    private TextView timeText;
    private TextView durationText;
    private TextView seatsText;
    private TextView restaurant_name;
    private ImageButton joinLeaveButton;
    private TextView joinLeaveText;
    private User user;
    private User currentUser;
    private DatabaseReference mDatabaseForList;
    private DatabaseReference mDatabaseForUser;
    private ImageView joinLeaveImage;
    private boolean isFriend;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_eat_post);


        timeText = findViewById(R.id.post_start_time);
        durationText = findViewById(R.id.post_duration);
        seatsText = findViewById(R.id.seats_available);
        restaurant_name = findViewById(R.id.restaurant_name);
        joinLeaveText = findViewById(R.id.join_leave_text);
        joinLeaveButton = findViewById(R.id.join_leave_button);
        joinLeaveImage = findViewById(R.id.join_leave_image);

        Intent intent = getIntent();
        restaurantName = intent.getStringExtra("RestaurantName");
        time = intent.getStringExtra("Time");
        countDown = intent.getStringExtra("Countdown");
        hostUid = intent.getStringExtra("HostUid");

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference(Constant.postSTR).
                child(restaurantName).child(hostUid);
        mUser = mAuth.getCurrentUser();

        post = null;
        mDatabase.addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                post = dataSnapshot.getValue(Post.class);

                if (post != null) {

                    timeText.setText(String.format(getResources().getString(R.string.time_posted),
                            post.meetingTime.hour, post.meetingTime.minute));
                    durationText.setText(String.format(getResources().getString(R.string.countdown_time),
                            post.countDown.hour, post.countDown.minute));
                    seatsText.setText(String.format(getResources().getString(R.string.seat_available),
                            post.maxNumber-post.members.size(), post.maxNumber));
                    restaurant_name.setText(restaurantName);
                    setButtonVisibility(post.user.equals(mUser.getUid()),post.maxNumber
                                    == post.members.size(),
                            !post.members.contains(mUser.getUid()));
                    setJoinLeaveButton(post.members.contains(mUser.getUid()));
                    populateUsersList();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message

            }
        });


        mDatabaseForUser = FirebaseDatabase.getInstance().getReference(Constant.userSTR).
                child(mUser.getUid());
        mDatabaseForUser.addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentUser = dataSnapshot.getValue(User.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message

            }
        });

        joinLeaveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((ColorDrawable) joinLeaveButton.getBackground()).getColor() == Color.GREEN) {
                    joinLeaveText.setText("LEAVE");
                    joinLeaveButton.setBackgroundColor(Color.RED);

                    post.joinMeeting(mUser.getUid());
                    Map<String, Object> postValues = post.toMap();
                    mDatabase.updateChildren(postValues);

                } else {
                    joinLeaveText.setText(JOIN);
                    joinLeaveButton.setBackgroundColor(Color.GREEN);

                    post.leaveMeeting(mUser.getUid());
                    Map<String, Object> postValues = post.toMap();
                    mDatabase.updateChildren(postValues);
                }
            }
        });



    }

    public void setButtonVisibility(boolean bool, boolean bool2, boolean bool3){
        if((bool || bool2) && bool3){
            joinLeaveButton.setVisibility(View.GONE);
            joinLeaveText.setVisibility(View.GONE);
            joinLeaveImage.setVisibility(View.GONE);
        }
    }

    public void setJoinLeaveButton(boolean isJoined){
        if(isJoined){
            joinLeaveButton.setBackgroundColor(Color.RED);
            joinLeaveText.setText("LEAVE");

        }else{
            joinLeaveButton.setBackgroundColor(Color.GREEN);
            joinLeaveText.setText(JOIN);
        }
    }

    private void populateUsersList() {
        final Activity a = this;

        mDatabaseForList = FirebaseDatabase.getInstance().getReference(Constant.userSTR);
        final FirebaseStorage storage = FirebaseStorage.getInstance();
        ValueEventListener postListener = new ValueEventListener() {

            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                LinearLayout listLinear = findViewById(R.id.users_list_layout);

                listLinear.removeAllViews();
                if (post != null) {

                        user = dataSnapshot.child(hostUid).getValue(User.class);
                        final FriendView hostView = new FriendView(a,listLinear,user.name,user.major
                                ,hostUid,currentUser.friendList.contains(hostUid));
                        StorageReference storageRef = storage.getReference();
                        StorageReference islandRef = storageRef.child(Constant.profilePathSTR +
                                hostUid + Constant.jpgSTR);

                        final long ONE_MEGABYTE = 1024 * 1024;
                        islandRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(
                                new OnSuccessListener<byte[]>() {
                            @Override
                            public void onSuccess(byte[] bytes) {
                                // Data for "images/island.jpg" is returns, use this as needed
                                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0,
                                        bytes.length);
                                hostView.setFriendImage(bitmap);

                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Handle any errors
                            }
                        });

                        for (String s: post.members) {

                            User friendInfo = dataSnapshot.child(s).getValue(User.class);
                            final FriendView friend = new FriendView(a,listLinear,friendInfo.name,
                                    friendInfo.major,s,currentUser.friendList.contains(s));
                            //StorageReference storageRef = storage.getReference();
                            StorageReference islandRef2 = storageRef.child(Constant.profilePathSTR
                                    + s + Constant.jpgSTR);

                            final long ONE_MEGABYTE2 = 1024 * 1024;
                            islandRef2.getBytes(ONE_MEGABYTE2).addOnSuccessListener(
                                    new OnSuccessListener<byte[]>() {
                                @Override
                                public void onSuccess(byte[] bytes) {
                                    // Data for "images/island.jpg" is returns, use this as needed
                                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,
                                            0, bytes.length);
                                    friend.setFriendImage(bitmap);

                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    // Handle any errors
                                }
                            });
                        }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        mDatabaseForList.addValueEventListener(postListener);
    }

}