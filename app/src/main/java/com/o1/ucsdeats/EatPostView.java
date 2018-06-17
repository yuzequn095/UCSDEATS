package com.o1.ucsdeats;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Peter on 5/7/2018.
 */

public class EatPostView extends View{
    private String JOIN = Constant.JOIN;
    private String LEAVE = Constant.LEAVE;

    private LinearLayout parentLayout;
    private ImageView profileImage;
    private Button joinLeaveButton;
    private Button openRestaurantButton;
    private TextView profileName;
    private TextView eatPostSeats;
    private TextView eatPostTime;
    private TextView eatPostCountdown;
    private TextView eatPostRestaurant;
    private Activity activity;
    private DatabaseReference mDatabase;
    private DatabaseReference mDatabaseForUser;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private String hostUid;
    private String restaurantName;
    private int remain;
    private int total;
    private int hour;
    private int min;
    private int countDownHour;
    private int countDownMin;
    private String name;
    private Post post;
    private boolean isFriend;
    private User user;

    public EatPostView(Activity a, LinearLayout l) {
        super(a);
        activity = a;
        parentLayout = l;
        createView();
    }

    public EatPostView(Activity a, LinearLayout l, String hostUid,String restaurantName,int remain,
                       int total,int hour, int min,int countDownHour, int countDownMin, String name){
        super(a);
        activity = a;
        parentLayout = l;
        this.hostUid = hostUid;
        this.restaurantName = restaurantName;
        this.remain = remain;
        this.total = total;
        this.hour = hour;
        this.min = min;
        this.countDownHour = countDownHour;
        this.countDownMin = countDownMin;
        this.name = name;

        createView();
    }

    private void createView() {
        // Create button to link to eatPost page
        openRestaurantButton = new Button(activity);
        openRestaurantButton.getBackground().setAlpha(0);
        openRestaurantButton.setId(View.generateViewId());

        final Button openEatPostButton = new Button(activity);
        openEatPostButton.getBackground().setAlpha(0);
        openEatPostButton.setId(View.generateViewId());

        final Button viewProfileButton = new Button(activity);
        viewProfileButton.getBackground().setAlpha(0);
        viewProfileButton.setId(View.generateViewId());

        joinLeaveButton = new Button(activity);
        joinLeaveButton.setBackgroundColor(Color.GREEN);
        joinLeaveButton.setText(JOIN);
        joinLeaveButton.setId(View.generateViewId());

        final RelativeLayout joinLeaveFrame = new RelativeLayout(activity);
        joinLeaveFrame.setBackgroundResource(R.drawable.button_shape_round);
        joinLeaveFrame.setId(View.generateViewId());

        final RelativeLayout joinLeaveInfoFrame = new RelativeLayout(activity);
        joinLeaveInfoFrame.setId(View.generateViewId());

        final RelativeLayout eatPostOuterFrame = new RelativeLayout(activity);
        eatPostOuterFrame.setBackground(ResourcesCompat.getDrawable(activity.getResources(), R.drawable.textbox_shape, null));
        eatPostOuterFrame.setId(View.generateViewId());

        // Create layout store each set of info
        final RelativeLayout eatPostFrame = new RelativeLayout(activity);
        eatPostFrame.setBackground(ResourcesCompat.getDrawable(activity.getResources(), R.drawable.textbox_shape, null));
        eatPostFrame.setId(View.generateViewId());


        final RelativeLayout profileFrame = new RelativeLayout(activity);
        profileFrame.setBackground(ResourcesCompat.getDrawable(activity.getResources(), R.drawable.textbox_shape, null));
        profileFrame.setId(View.generateViewId());

        // Create eatPost image preferably profile picture
        profileImage = new ImageView(activity);
        profileImage.setBackgroundColor(Color.GREEN);
        profileImage.setScaleType(ImageView.ScaleType.FIT_START);
        profileImage.setAdjustViewBounds(true);
        profileImage.setId(View.generateViewId());

        // Create name of user who uploaded the post
        profileName = new TextView(activity);
        profileName.setTextColor(Color.BLACK);
        profileName.setId(View.generateViewId());
        profileName.setTextSize(18);

        // Create number of seat available
        eatPostSeats = new TextView(activity);
        eatPostSeats.setTextColor(Color.BLACK);
        eatPostSeats.setId(View.generateViewId());

        // Create time stamp of post
        eatPostTime = new TextView(activity);
        eatPostTime.setTextColor(Color.BLACK);
        eatPostTime.setId(View.generateViewId());

        // Create post timer till end
        eatPostCountdown = new TextView(activity);
        eatPostCountdown.setTextColor(Color.BLACK);
        eatPostCountdown.setId(View.generateViewId());

        // Create restaurant name
        int titleSize = (int) activity.getResources().getDimension(R.dimen.tiny_text_size);
        eatPostRestaurant = new TextView(activity);
        eatPostRestaurant.setTextColor(getResources().getColor(R.color.ucsd_eat_text));
        eatPostRestaurant.setTextSize(titleSize);
        eatPostRestaurant.setId(View.generateViewId());

        // Set params for the main layout
        int eatPostTextboxHeight = (int) activity.getResources().getDimension(R.dimen.post_text_box_height);
        int fabMarginSmall = (int) activity.getResources().getDimension(R.dimen.fab_margin_small);
        int fabMarginMinimal = (int) activity.getResources().getDimension(R.dimen.fab_margin_minimal);
        final LinearLayout.LayoutParams outerFrameParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        outerFrameParams.setMargins(fabMarginSmall, fabMarginMinimal,fabMarginSmall, fabMarginMinimal);

        final RelativeLayout.LayoutParams joinLeaveButtonParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        joinLeaveButtonParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        joinLeaveButtonParams.addRule(RelativeLayout.ALIGN_RIGHT, joinLeaveInfoFrame.getId());

        final RelativeLayout.LayoutParams eatPostFrameParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, eatPostTextboxHeight);

        // Set params for the text button
        final RelativeLayout.LayoutParams eatPostRestaurantParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        eatPostRestaurantParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        eatPostRestaurantParams.setMarginStart(fabMarginMinimal);

        // Set params for text showing time posted
        final RelativeLayout.LayoutParams eatPostTimeParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        eatPostTimeParams.addRule(RelativeLayout.BELOW, eatPostRestaurant.getId());
        eatPostTimeParams.setMarginStart(fabMarginMinimal);

        // Set params for text showing time until post over
        final RelativeLayout.LayoutParams eatPostCountdownParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        eatPostCountdownParams.addRule(RelativeLayout.BELOW, eatPostTime.getId());
        eatPostCountdownParams.setMarginStart(fabMarginMinimal);

        // Set params for text showing number of seat available
        final RelativeLayout.LayoutParams eatPostSeatsParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        eatPostSeatsParams.addRule(RelativeLayout.BELOW, eatPostCountdown.getId());
        eatPostSeatsParams.setMarginStart(fabMarginMinimal);

        // Set params for button to join or leave the post
        final RelativeLayout.LayoutParams joinLeaveParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        joinLeaveParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        joinLeaveParams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
        joinLeaveParams.setMargins(fabMarginSmall, fabMarginMinimal,fabMarginSmall, fabMarginMinimal);

        // Links to Restaurant Activity
        final RelativeLayout.LayoutParams openRestaurantParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        openRestaurantParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        openRestaurantParams.addRule(RelativeLayout.ALIGN_END, eatPostRestaurant.getId());
        openRestaurantParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);

        // Links to Eat Post Activity
        final RelativeLayout.LayoutParams openEatPostParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        openEatPostParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        openEatPostParams.addRule(RelativeLayout.LEFT_OF, joinLeaveButton.getId());
        openEatPostParams.addRule(RelativeLayout.BELOW, eatPostRestaurant.getId());

        final RelativeLayout.LayoutParams profileFrameParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, eatPostTextboxHeight / 2);
        profileFrameParams.addRule(RelativeLayout.BELOW, eatPostFrame.getId());

        // Set params for the profile image
        final RelativeLayout.LayoutParams profileImageParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        profileImageParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        profileImageParams.setMargins(fabMarginSmall, fabMarginMinimal,fabMarginSmall, fabMarginMinimal);

        // Set eatPost profile name params on far right
        final RelativeLayout.LayoutParams profileNameParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        profileNameParams.addRule(RelativeLayout.LEFT_OF, profileImage.getId());
        profileNameParams.addRule(RelativeLayout.CENTER_VERTICAL, profileImage.getId());

        // Set params for hidden button link to profile page
        final RelativeLayout.LayoutParams viewProfileParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        viewProfileParams.addRule(RelativeLayout.ALIGN_LEFT, profileName.getId());
        viewProfileParams.addRule(RelativeLayout.ALIGN_PARENT_TOP, RelativeLayout.TRUE);

        // Add all views to respective parents
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                parentLayout.addView(eatPostOuterFrame, outerFrameParams);
                eatPostOuterFrame.addView(eatPostFrame, eatPostFrameParams);
                eatPostOuterFrame.addView(profileFrame, profileFrameParams);
                eatPostFrame.addView(eatPostCountdown, eatPostCountdownParams);
                eatPostFrame.addView(eatPostRestaurant, eatPostRestaurantParams);
                eatPostFrame.addView(eatPostSeats, eatPostSeatsParams);
                eatPostFrame.addView(eatPostTime, eatPostTimeParams);
                eatPostFrame.addView(openRestaurantButton, openRestaurantParams);
                eatPostFrame.addView(openEatPostButton, openEatPostParams);
                eatPostFrame.addView(joinLeaveButton, joinLeaveParams);
                profileFrame.addView(viewProfileButton,viewProfileParams);
                profileFrame.addView(profileImage, profileImageParams);
                profileFrame.addView(profileName, profileNameParams);
            }
        });

        //eatPostFrame.setBackgroundColor(Color.TRANSPARENT);
        //eatPostOuterFrame.setBackgroundColor(Color.TRANSPARENT);
        //profileFrame.setBackgroundColor(Color.TRANSPARENT);

        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference(Constant.postSTR).
                child(restaurantName).child(hostUid);


        post = null;
        mDatabase.addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                post = dataSnapshot.getValue(Post.class);

                if (post != null) {
                    setEatPostSeats(post.maxNumber, post.maxNumber - post.members.size());
                    setEatPostCountdown(countDownHour,countDownMin);
                    setEatPostRestaurant(restaurantName);
                    setEatPostTime(hour,min);
                    setProfileName(name);
                    setHostUid(hostUid);
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
                user = dataSnapshot.getValue(User.class);

                if (user != null) {
                    isFriend = user.friendList.contains(hostUid);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message

            }
        });


        joinLeaveButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (((ColorDrawable) joinLeaveButton.getBackground()).getColor() == Color.GREEN) {
                    joinLeaveButton.setText(LEAVE);
                    joinLeaveButton.setBackgroundColor(Color.RED);

                    post.joinMeeting(mUser.getUid());
                    Map<String, Object> postValues = post.toMap();
                    mDatabase.updateChildren(postValues);

                } else {
                    joinLeaveButton.setText(Constant.JOIN);
                    joinLeaveButton.setBackgroundColor(Color.GREEN);

                    post.leaveMeeting(mUser.getUid());
                    Map<String, Object> postValues = post.toMap();
                    mDatabase.updateChildren(postValues);
                }
            }
        });

        // Set onClickListener for each EatPostView
        openRestaurantButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent restaurantActivity= new Intent(activity, RestaurantActivity.class);
                restaurantActivity.putExtra("Name", eatPostRestaurant.getText().toString());
                activity.startActivity(restaurantActivity);
            }
        });

        openEatPostButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent eatPostActivity = new Intent(activity, EatPostActivity.class);
                eatPostActivity.putExtra("Time",String.format(getResources().getString(R.string.time_posted), hour, min));
                eatPostActivity.putExtra("Countdown",String.format(getResources().getString(R.string.time_posted), countDownHour, countDownMin));
                eatPostActivity.putExtra("HostUid",hostUid);
                eatPostActivity.putExtra("RestaurantName",restaurantName);
                activity.startActivity(eatPostActivity);
            }
        });

        viewProfileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent viewOthersProfileActivity = new Intent(activity, ViewOthersProfileActivity.class);
                viewOthersProfileActivity.putExtra(Constant.otherUserUidSTR,hostUid);
                viewOthersProfileActivity.putExtra(Constant.isFriendSTR,isFriend);
                activity.startActivity(viewOthersProfileActivity);
            }
        });
    }

    // Set name of eatPost
    public void setEatPostRestaurant(String name) {
        eatPostRestaurant.setText(name);
    }

    public String getEatPostRestaurant() {
        return eatPostRestaurant.getText().toString();
    }

    private void updateEatPostSeats(int i) {
        String text = (String) eatPostSeats.getText();
        String[] txt = text.split(" ");
        setEatPostSeats(Integer.parseInt(txt[txt.length - 1]), Integer.parseInt(txt[txt.length - 4]) - i);
    }

    // Set location of eatPost
    public void setEatPostSeats(int total, int remain) {
        eatPostSeats.setText(String.format(getResources().getString(R.string.seat_available), remain, total));
    }

    public String getEatPostSeats() {
        String[] seats = eatPostSeats.getText().toString().split("\\s+");
        String returnStr = seats[2] + "/" + seats[5];
        return returnStr;
    }

    // Set image for eatPost using Bitmap
    public void setEatPostTime(int hour, int min) {
        eatPostTime.setText(String.format(getResources().getString(R.string.time_posted), hour, min));
    }

    public String getEatPostTime() {
        String[] time = eatPostSeats.getText().toString().split("\\s+");
        return time[3];
    }

    // Set image for eatPost using Drawable
    public void setEatPostCountdown(int hour, int min) {
        eatPostCountdown.setText(String.format(getResources().getString(R.string.countdown_time), hour, min));
    }

    public String getEatPostCountdown() {
        String[] countdown = eatPostSeats.getText().toString().split("\\s+");
        String returnStr = countdown[3] + "h " + countdown[5] + "m";
        return returnStr;
    }

    public void setProfileImage(Bitmap bm) {
        profileImage.setImageBitmap(bm);
    }

    public void setProfileName(String name) {
        profileName.setText(name);
    }

    public int getId() {
        return this.getId();
    }

    public boolean isJoining() {
        if (((ColorDrawable) joinLeaveButton.getBackground()).getColor() == Color.GREEN) {
            return true;
        }
        return false;
    }

    public void leavePost() {
        joinLeaveButton.setText(Constant.JOIN);
        joinLeaveButton.setBackgroundColor(Color.GREEN);
        updateEatPostSeats(-1);
    }

    public void removeRestaurantLink() {
        openRestaurantButton.setOnClickListener(null);
        openRestaurantButton.setVisibility(View.GONE);
    }

    public void setProfileVisibility(boolean bool) {
        if (bool) {
            profileImage.setVisibility(View.GONE);
        }
    }

    public void setButtonVisibility(boolean bool,boolean bool2, boolean bool3){
        if((bool || bool2) && bool3){
            joinLeaveButton.setVisibility(View.GONE);
        }
    }

    public void setHostUid(String hostUid){
        this.hostUid = hostUid;
    }

    public void setJoinLeaveButton(boolean isJoined){
        if (isJoined) {
            joinLeaveButton.setBackgroundColor(Color.RED);
            joinLeaveButton.setText("LEAVE");
        } else {
            joinLeaveButton.setBackgroundColor(Color.GREEN);
            joinLeaveButton.setText("JOIN!!!");
        }
    }
}
