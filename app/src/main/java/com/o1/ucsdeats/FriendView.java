package com.o1.ucsdeats;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

/**
 * Created by Peter on 5/7/2018.
 */

public class FriendView extends View {
    private Activity activity;
    private LinearLayout parentLayout;
    private ImageView friendImage;
    private TextView friendName;
    private TextView friendMajor;
    private String uid;
    private Boolean isFriend;
    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentuser;
    private User otherUser;
    private String name;
    private String major;


    public FriendView(Activity a, LinearLayout l, String name, String major, String uid,
                      boolean isFriend) {
        super(a);
        activity = a;
        parentLayout = l;
        this.uid = uid;
        this.name = name;
        this.major = major;
        this.isFriend = isFriend;
        createView();
    }

    private void createView() {

        final Button openFriendButton = new Button(activity);
        openFriendButton.getBackground().setAlpha(0);
        openFriendButton.setId(View.generateViewId());

        RelativeLayout friendFrame = new RelativeLayout(activity);
        friendFrame.setBackground(ResourcesCompat.getDrawable(getResources(),
                R.drawable.textbox_shape, null));
        friendFrame.setId(View.generateViewId());
        friendFrame.setBackgroundColor(Color.TRANSPARENT);

        friendImage = new ImageView(activity);
        //friendImage.setBackgroundColor(Color.GREEN);
        friendImage.setId(View.generateViewId());
        friendImage.setBackgroundColor(Color.TRANSPARENT);

        // Create name of friend
        friendName = new TextView(activity);
        friendName.setTextColor(Color.BLACK);
        friendName.setId(View.generateViewId());
        friendName.setTextSize(24);
        friendName.setTypeface(null, Typeface.BOLD);

        // Create location of friend
        friendMajor = new TextView(activity);
        friendMajor.setTextColor(getResources().getColor(R.color.ucsd_eat_text));
        friendMajor.setId(View.generateViewId());
        friendMajor.setTypeface(null, Typeface.BOLD);

        int friendTextboxHeight = (int) activity.getResources().getDimension(R.dimen.restaurant_text_box_height);
        int fabMarginSmall = (int) activity.getResources().getDimension(R.dimen.fab_margin_small);
        int fabMarginMinimal = (int) activity.getResources().getDimension(R.dimen.fab_margin_minimal);
        LinearLayout.LayoutParams friendFrameParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, friendTextboxHeight);
        friendFrameParams.setMargins(fabMarginSmall, fabMarginMinimal,fabMarginSmall,
                fabMarginMinimal);

        // Set params for the button
        RelativeLayout.LayoutParams openfriendParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);

        // Set friend image params on far left
        RelativeLayout.LayoutParams friendImageParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        friendImageParams.width = friendTextboxHeight;
        friendImageParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        friendImageParams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
        friendImageParams.setMargins(fabMarginSmall, fabMarginSmall, fabMarginSmall, fabMarginSmall);

        // Set name params
        RelativeLayout.LayoutParams friendNameParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        friendNameParams.addRule(RelativeLayout.RIGHT_OF, friendImage.getId());
        friendNameParams.setMargins(fabMarginSmall, fabMarginSmall, fabMarginSmall, fabMarginSmall);

        // Set location params
        RelativeLayout.LayoutParams friendMajorParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        friendMajorParams.addRule(RelativeLayout.RIGHT_OF, friendImage.getId());
        friendMajorParams.addRule(RelativeLayout.BELOW, friendName.getId());
        friendMajorParams.setMargins(fabMarginSmall, fabMarginSmall, fabMarginSmall, fabMarginSmall);

        // Add all views to respective parents
        parentLayout.addView(friendFrame, friendFrameParams);
        friendFrame.addView(openFriendButton, openfriendParams);
        friendFrame.addView(friendImage, friendImageParams);
        friendFrame.addView(friendName, friendNameParams);
        friendFrame.addView(friendMajor, friendMajorParams);

        // Set onClickListener for each friendView
        openFriendButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent friendActivity = new Intent(activity, ViewOthersProfileActivity.class);
                friendActivity.putExtra(Constant.otherUserUidSTR,uid);
                friendActivity.putExtra(Constant.isFriendSTR,isFriend);

                activity.startActivity(friendActivity);
            }
        });

        mAuth = FirebaseAuth.getInstance();
        mCurrentuser = mAuth.getCurrentUser();

        mDatabase = FirebaseDatabase.getInstance().getReference(Constant.userSTR).
                child(mCurrentuser.getUid());
        mDatabase.addListenerForSingleValueEvent( new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                otherUser = dataSnapshot.getValue(User.class);

                if (otherUser != null) {
                    setFriendName(name);
                    setFriendMajor(major);
                    setFriendUid(uid);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message

            }
        });
    }

    // Set name of friend
    public void setFriendName(String name) {
        friendName.setText(name);
    }

    // Set location of friend
    public void setFriendMajor(String major) {
        friendMajor.setText(major);
    }

    // Set image for friend using Bitmap
    public void setFriendImage(Bitmap bm) {
        friendImage.setImageBitmap(bm);
    }

    // Set image for friend using Drawable
    public void setFriendImage(Drawable d) {
        friendImage.setImageDrawable(d);
    }

    //Set String for friend uid
    public  void setFriendUid(String s){ uid = s;}
}
