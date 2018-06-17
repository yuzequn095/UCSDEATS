package com.o1.ucsdeats;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.TimeZone;

import static android.view.View.GONE;

/**
 * Created by Peter on 5/3/2018.
 */

public class HomeUILoader {
    private final int SEARCH_PAGE = 0;
    private final int RECOMMEND_PAGE = 1;
    private final int FRIENDS_PAGE = 2;
    private final int EAT_PAGE = 3;

    private Activity activity;
    private ConstraintLayout constraintLayout;
    private RelativeLayout activePostLayout;
    private LinearLayout listLinear;
    private EditText searchView;
    private DatabaseReference mDatabase;
    private FirebaseStorage mStorage;
    private final double NEARBY_LIMIT = 0.5;
    public boolean noNearbyRestaurant;
    private FirebaseUser User;
    private FirebaseAuth mAuth;
    private Bitmap bitmap;
    private ImageView searchIcon;

    public HomeUILoader(Activity a, ConstraintLayout cl) {
        activity = a;
        constraintLayout = cl;
    }

    public void createPage(int pageNum) {
        clearPage();

        // Create Prompt
        // Create search box
        searchView = new EditText(activity.getApplicationContext());
        searchView.setHint(R.string.search_restaurant);
        searchView.setSingleLine();
        searchView.setId(View.generateViewId());

        // Create layout to wrap search box in
        FrameLayout searchLayout = new FrameLayout(activity.getApplicationContext());
        searchLayout.setBackground(ResourcesCompat.getDrawable(activity.getResources(), R.drawable.textbox_shape, null));
        searchLayout.setId(View.generateViewId());
        searchIcon = new ImageView(activity.getApplicationContext());
        searchIcon.setImageDrawable(ResourcesCompat.getDrawable(activity.getResources(), R.drawable.ic_search_black_24dp, null));
        searchIcon.setId(View.generateViewId());

        // Create active post
        activePostLayout = new RelativeLayout(activity.getApplicationContext());
        activePostLayout.setBackground(ResourcesCompat.getDrawable(activity.getResources(), R.drawable.textbox_shape, null));
        activePostLayout.setVisibility(View.GONE);
        activePostLayout.setId(View.generateViewId());
        searchLayout.setBackgroundColor(Color.TRANSPARENT);

        // Create layout to display list of all restaurants
        RelativeLayout masterListLayout = new RelativeLayout(activity.getApplicationContext());
        masterListLayout.setId(View.generateViewId());
        ScrollView listScroll = new ScrollView(activity.getApplicationContext());
        listScroll.setBackground(ResourcesCompat.getDrawable(activity.getResources(), R.drawable.textbox_shape, null));
        listScroll.setScrollContainer(false);
        listScroll.setId(View.generateViewId());
        listScroll.setBackgroundColor(Color.TRANSPARENT);
        listLinear = new LinearLayout(activity.getApplicationContext());
        listLinear.setOrientation(LinearLayout.VERTICAL);
        listLinear.setId(View.generateViewId());

        // Set text in box offset after search image
        int marginSearchBar = (int) activity.getResources().getDimension(R.dimen.fab_margin_small);
        FrameLayout.LayoutParams searchViewParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT);
        searchViewParams.setMarginStart((int) activity.getResources().getDimension(R.dimen.search_margin_start));

        ConstraintLayout.LayoutParams activePostParams = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
        activePostParams.setMargins(marginSearchBar, marginSearchBar, marginSearchBar, marginSearchBar);

        // Set master layout params
        ConstraintLayout.LayoutParams masterLayoutParams = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
        masterLayoutParams.setMargins(marginSearchBar, marginSearchBar, marginSearchBar, marginSearchBar);

        // Set search box offset
        ConstraintLayout.LayoutParams searchLayoutParams = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
        searchLayoutParams.setMargins(marginSearchBar, marginSearchBar, marginSearchBar, marginSearchBar);

        // Sets list of restaurants
        ConstraintLayout.LayoutParams restaurantsListScrollParams = new ConstraintLayout.LayoutParams(ConstraintLayout.LayoutParams.MATCH_PARENT, ConstraintLayout.LayoutParams.WRAP_CONTENT);
        restaurantsListScrollParams.setMargins(marginSearchBar, marginSearchBar, marginSearchBar, marginSearchBar);

        // Add all generated layouts to the main constraint layout
        constraintLayout.addView(searchLayout, searchLayoutParams);
        searchLayout.addView(searchView, searchViewParams);
        constraintLayout.addView(activePostLayout, activePostParams);
        constraintLayout.addView(masterListLayout, masterLayoutParams);
        masterListLayout.addView(listScroll, restaurantsListScrollParams);
        constraintLayout.addView(searchIcon);
        listScroll.addView(listLinear);

        // Set all constrains on layout items
        int titleBarOffset = ((int) activity.getResources().getDimension(R.dimen.title_bar_height)) + 2 * marginSearchBar;
        ConstraintSet searchLayoutConstraints = new ConstraintSet();
        searchLayoutConstraints.clone(constraintLayout);
        searchLayoutConstraints.connect(searchLayout.getId(), ConstraintSet.LEFT, constraintLayout.getId(), ConstraintSet.LEFT, 0);
        searchLayoutConstraints.connect(searchLayout.getId(), ConstraintSet.TOP, constraintLayout.getId(), ConstraintSet.TOP, titleBarOffset);
        searchLayoutConstraints.connect(activePostLayout.getId(), ConstraintSet.TOP, searchLayout.getId(), ConstraintSet.BOTTOM);
        searchLayoutConstraints.connect(activePostLayout.getId(), ConstraintSet.BOTTOM, masterListLayout.getId(), ConstraintSet.TOP);
        searchLayoutConstraints.connect(masterListLayout.getId(), ConstraintSet.TOP, activePostLayout.getId(), ConstraintSet.BOTTOM);
        searchLayoutConstraints.connect(masterListLayout.getId(), ConstraintSet.BOTTOM, R.id.nav_bottom, ConstraintSet.TOP);
        searchLayoutConstraints.connect(searchIcon.getId(), ConstraintSet.TOP, searchLayout.getId(), ConstraintSet.TOP, marginSearchBar);
        searchLayoutConstraints.connect(searchIcon.getId(), ConstraintSet.LEFT, searchLayout.getId(), ConstraintSet.LEFT, marginSearchBar);
        searchLayoutConstraints.constrainHeight(masterListLayout.getId(), 0);
        searchLayoutConstraints.applyTo(constraintLayout);

        Thread searchThread = new Thread(new Runnable() {
            @Override
            public void run() {
                createPageContent();
            }
        });
        searchThread.start();
    }

    public void createPageContent() {
        BottomNavigationView bnv = activity.findViewById(R.id.nav_bottom);
        if (bnv.getSelectedItemId() == R.id.navigation_search) {
            createSearchPage();
            searchView.setHint(R.string.search_restaurant);
        } else if (bnv.getSelectedItemId() == R.id.navigation_recommend) {
            searchView.setVisibility(GONE);
            searchIcon.setVisibility(View.GONE);
            createRecommendPage();

        } else if (bnv.getSelectedItemId() == R.id.navigation_friends) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if(user != null) {
                createFriendsPage();
                searchView.setHint(R.string.search_friend);
            }else{
                Intent intent = new Intent(activity, LoginActivity.class);
                activity.startActivity(intent);
            }

        } else if (bnv.getSelectedItemId() == R.id.navigation_find_someone) {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if(user != null) {
                searchView.setVisibility(GONE);
                searchIcon.setVisibility(View.GONE);
                createFindSomeonePage();
            }else{
                Intent intent = new Intent(activity, LoginActivity.class);
                activity.startActivity(intent);
            }

        }

        ScrollView scroll = (ScrollView) listLinear.getParent();
        scroll.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.
                OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                BottomNavigationView bnv = activity.findViewById(R.id.nav_bottom);
                ScrollView scroll = (ScrollView) listLinear.getParent();
                if (null != ((LinearLayout) scroll.getChildAt(0)).getChildAt(0)) {
                    if (scroll.getChildAt(0).getBottom() <= (scroll.getHeight() + scroll.
                            getScrollY())) {
                        if (bnv.getSelectedItemId() == R.id.navigation_search) {
                        } else if (bnv.getSelectedItemId() == R.id.navigation_recommend) {

                        } else if (bnv.getSelectedItemId() == R.id.navigation_friends) {

                        } else if (bnv.getSelectedItemId() == R.id.navigation_find_someone) {

                        }
                    } else {
                    }
                }
            }
        });

        searchView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    searchView.setHint("");
                else {
                    BottomNavigationView bnv = activity.findViewById(R.id.nav_bottom);
                    if (bnv.getSelectedItemId() == R.id.navigation_search) {
                        searchView.setHint(R.string.search_restaurant);
                    } else if (bnv.getSelectedItemId() == R.id.navigation_recommend) {
                        searchView.setHint(R.string.search_restaurant);
                    } else if (bnv.getSelectedItemId() == R.id.navigation_friends) {
                        searchView.setHint(R.string.search_friend);
                    } else if (bnv.getSelectedItemId() == R.id.navigation_find_someone) {
                        searchView.setHint(R.string.search_eat);
                    }
                }
            }
        });
    }

    // Creates all the GUI components for the Random Restuarant page
    private void createSearchPage() {
        searchView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    searchView.setHint("");
                else
                    searchView.setHint(R.string.search_restaurant);
            }
        });

        searchView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    listLinear.removeAllViews();

                    //getting User input
                    String restaurantName = searchView.getText().toString();

                    //AutoComplete User input
                    ArrayList<String> restaurantNameArray = autoComplete(restaurantName);

                    for (final String eachRestaurantName : restaurantNameArray){

                        final RestaurantView restaurant = new RestaurantView(activity, listLinear);

                        mDatabase = FirebaseDatabase.getInstance().getReference();

                        mStorage = FirebaseStorage.getInstance();

                        ValueEventListener postListener = new ValueEventListener() {


                        @Override
                        public void onDataChange(final DataSnapshot dataSnapshot) {

                            final Restaurant restaurantV = dataSnapshot.child(Constant.
                                    restaurantSTR).child(eachRestaurantName).getValue(
                                            Restaurant.class);
                            if (restaurantV != null) {

                                restaurant.setRestaurantName(restaurantV.name);
                                restaurant.setRestaurantLocation(getHours(restaurantV));

                                //convert string to resID friendly format
                                String lowerCase = restaurantV.name.toLowerCase();

                                lowerCase = resourceIdName(lowerCase);

                                int resID = activity.getResources().getIdentifier(lowerCase,
                                        "drawable", activity.getPackageName());

                                restaurant.setRestaurantImage(BitmapFactory.decodeResource
                                        (activity.getResources(), resID));
                            } else {
                                return;
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    };


                    mDatabase.addValueEventListener(postListener);
                }


                }
                return false;
            }
        });
    }

    // Creates all the GUI components for the Recommend Restaurants page
    private void createRecommendPage() {

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                listLinear.removeAllViews();
                final LocationService locationService = new LocationService(activity);
                final float[] distance = {0};
                //TODO:Sort the list of restaurants based on distance, rating and favorite food.
                final LinkedList<Restaurant> nearbyRestaurant = new LinkedList<Restaurant>();
                mDatabase = FirebaseDatabase.getInstance().getReference();
                mDatabase.child(Constant.restaurantSTR).addListenerForSingleValueEvent
                        (new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                            Restaurant restaurant = snapshot.getValue(Restaurant.class);
                            if (restaurant == null) {
                                System.err.println("Null Restaurant");
                                return;
                            }
                            double restaurantLatitude = Double.parseDouble(restaurant.latitude);
                            double restaurantLongitude = Double.parseDouble(restaurant.longitude);
                            if (locationService.getLocation() == null) {
                                return;
                            }
                            Location.distanceBetween(locationService.getLocation().getLatitude(),
                                    locationService.getLocation().getLongitude(),
                                    restaurantLatitude, restaurantLongitude, distance);
                            double mileDistance = distance[0] * 0.000621371 ;
                            if (mileDistance <= NEARBY_LIMIT) {
                                nearbyRestaurant.add(restaurant);
                            }


                        }
                        if (nearbyRestaurant.isEmpty()) {
                            noNearbyRestaurant = true;

                        } else {
                            noNearbyRestaurant = false;
                            ValueEventListener postListener;
                            for (int i = 0; i < nearbyRestaurant.size(); i++) {
                                Restaurant restaurantToShow = nearbyRestaurant.get(i);
                                RestaurantView restaurantView = new RestaurantView(activity, listLinear);
                                restaurantView.setRestaurantName(restaurantToShow.name);
                                restaurantView.setRestaurantLocation(getHours(restaurantToShow));
                                String lowerCase = restaurantToShow.name.toLowerCase();
                                lowerCase = resourceIdName(lowerCase);

                                //Change 64 to sf since drawable image doesn't allow digit
                                if (lowerCase.contains("64")) {
                                    lowerCase = lowerCase.replace("64", "sf");
                                }

                                int resID = activity.getResources().getIdentifier(lowerCase,
                                        "drawable", activity.getPackageName());

                                restaurantView.setRestaurantImage(BitmapFactory.decodeResource
                                        (activity.getResources(), resID));
                            }
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e("RecommendCancelled", "Possible Null restaurant appears");
                    }
                });


            }
        });
    }

    // Creates all the GUI components for the Friends page
    private void createFriendsPage() {

        mAuth = FirebaseAuth.getInstance();
        User = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        final FirebaseStorage storage = FirebaseStorage.getInstance();
        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                //listLinear.removeAllViews();
                final User user = dataSnapshot.child(Constant.userSTR).child
                        (User.getUid().toString()).getValue(User.class);
                if (user != null) {
                    if(user.getFriendList().isEmpty()){
                        System.out.println("You don't have friend");
                    }else{

                        for(String s : user.getFriendList()){

                            User friendInfo = dataSnapshot.child(Constant.userSTR).child(s)
                                    .getValue(User.class);
                            final FriendView friend = new FriendView(activity,listLinear,
                                    friendInfo.name, friendInfo.major,s,true);

                            StorageReference storageRef = storage.getReference();
                            StorageReference islandRef = storageRef.child(Constant.profilePathSTR
                                    + s + Constant.jpgSTR);

                            final long ONE_MEGABYTE = 1024 * 1024;
                            islandRef.getBytes(ONE_MEGABYTE).addOnSuccessListener
                                    (new OnSuccessListener<byte[]>() {
                                @Override
                                public void onSuccess(byte[] bytes) {
                                    // Data for "images/island.jpg" is returns, use this as needed
                                    Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,
                                            0,bytes.length);
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
                } else {
                    return;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                listLinear.removeAllViews();
            }
        });

        searchView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    searchView.setHint("");
                else
                    searchView.setHint(R.string.search_friend);
            }
        });

        searchView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    final String friendName = searchView.getText().toString();
                    mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {

                        @Override
                        public void onDataChange(final DataSnapshot dataSnapshot) {
                            listLinear.removeAllViews();
                            final User user = dataSnapshot.child(Constant.userSTR).
                                    child(User.getUid().toString()).getValue(User.class);
                            if (user != null) {
                                if(user.getFriendList().isEmpty()){
                                    System.out.println("You don't have friend");
                                }else{

                                    for(String s : user.getFriendList()){

                                        if(dataSnapshot.child(Constant.userSTR).child(s).getValue
                                                (User.class).name.toLowerCase().contains
                                                (friendName.toLowerCase())) {
                                            User friendInfo = dataSnapshot.child(Constant.userSTR)
                                                    .child(s).getValue(User.class);
                                            final FriendView friend = new FriendView(activity,
                                                    listLinear, friendInfo.name, friendInfo.major
                                                    , s, true);

                                            StorageReference storageRef = storage.getReference();
                                            StorageReference islandRef = storageRef.child(Constant.
                                                    profilePathSTR + s + Constant.jpgSTR);

                                            final long ONE_MEGABYTE = 1024 * 1024;
                                            islandRef.getBytes(ONE_MEGABYTE).addOnSuccessListener
                                                    (new OnSuccessListener<byte[]>() {
                                                @Override
                                                public void onSuccess(byte[] bytes) {
                                                    // Data for "images/island.jpg" is returns,
                                                    // use this as needed
                                                    Bitmap bitmap = BitmapFactory.decodeByteArray
                                                            (bytes, 0, bytes.length);
                                                    friend.setFriendImage(bitmap);

                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception exception){
                                                    // Handle any errors
                                                }
                                            });
                                        }
                                    }
                                }
                            } else {
                                return;
                            }
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }
                return false;
            }
        });

    }

    // Creates all the GUI components for the Find Someone to Eat With page;
    private void createFindSomeonePage() {
        final FloatingActionButton addPost = new FloatingActionButton(activity);
        addPost.setId(View.generateViewId());
        addPost.setImageDrawable(ResourcesCompat.getDrawable(activity.getResources(),
                R.drawable.ic_mode_edit_black_24dp, null));
        addPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent addPostActivity = new Intent(activity, AddPostActivity.class);
                activity.startActivity(addPostActivity);

            }
        });

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                constraintLayout.addView(addPost);

                int marginAddPost = (int) activity.getResources().getDimension(R.dimen.
                        fab_margin_small);

                ConstraintSet addPostConstraints = new ConstraintSet();
                addPostConstraints.clone(constraintLayout);
                addPostConstraints.connect(addPost.getId(), ConstraintSet.BOTTOM, R.id.nav_bottom,
                        ConstraintSet.TOP, marginAddPost);
                addPostConstraints.connect(addPost.getId(), ConstraintSet.END, constraintLayout.
                        getId(), ConstraintSet.END, marginAddPost);
                addPostConstraints.applyTo(constraintLayout);
            }
        });

        searchView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus)
                    searchView.setHint("");
                else
                    searchView.setHint(R.string.search_friend);
            }
        });

        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                listLinear.removeAllViews();
            }
        });


        mAuth = FirebaseAuth.getInstance();
        User = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        final FirebaseStorage storage = FirebaseStorage.getInstance();

        ValueEventListener postListener = new ValueEventListener() {

            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                ArrayList<Post> tempList = new ArrayList<>();
                for(DataSnapshot UpperPosts : dataSnapshot.child(Constant.postSTR).getChildren()){
                    for(DataSnapshot Posts : UpperPosts.getChildren())
                        tempList.add(Posts.getValue(Post.class));
                }

                Collections.sort(tempList,new SortByTime());

                for(Post p : tempList){

                    final EatPostView post = new EatPostView(activity,listLinear, p.user,
                            p.restaurantName, p.maxNumber-p.members.size(),p.maxNumber
                            , p.meetingTime.hour, p.meetingTime.minute,p.countDown.hour,
                            p.countDown.minute, dataSnapshot.child(Constant.userSTR).child(p.user)
                            .getValue(User.class).name);
                    post.setButtonVisibility(User.getUid().equals(p.user),p.maxNumber ==
                            p.members.size(),!p.members.contains(User.getUid()));
                    post.setJoinLeaveButton(p.members.contains(User.getUid()));

                    StorageReference storageRef = storage.getReference();
                    StorageReference islandRef = storageRef.child(Constant.profilePathSTR +
                            p.user + Constant.jpgSTR);
                    final long ONE_MEGABYTE = 1024 * 1024;
                    islandRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener
                            <byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            // Data for "images/island.jpg" is returns, use this as needed
                            bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                            post.setProfileImage(bitmap);

                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle any errors
                        }
                    });

                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };

        mDatabase.addListenerForSingleValueEvent(postListener);



    }

    public void clearPage() {
        constraintLayout.removeViews(1, constraintLayout.getChildCount() - 1);
    }

    private String getHours(Restaurant restaurant) {
        TimeZone zone = TimeZone.getDefault();
        Calendar c = Calendar.getInstance(zone);
        int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);

        switch(dayOfWeek) {
            case 1: return restaurant.hours.sun;
            case 2: return restaurant.hours.mon;
            case 3: return restaurant.hours.tues;
            case 4: return restaurant.hours.weds;
            case 5: return restaurant.hours.thurs;
            case 6: return restaurant.hours.fri;
            case 7: return restaurant.hours.sat;
            default: return "";
        }
    }

    private String resourceIdName(String lowerCase){
        String lowerCaseA[] = lowerCase.split(" ");
        lowerCase = "";

        for (String s : lowerCaseA)
            lowerCase = lowerCase + s + "_";
        lowerCase += "small";

        //Change 64 to sf since drawable image doesn't allow digit
        if (lowerCase.contains("64")) {
            lowerCase = lowerCase.replace("64", "sf");
        }

        StringBuilder sb = new StringBuilder(lowerCase);
        while(sb.indexOf("'") != -1 || sb.indexOf("&") != -1 || sb.indexOf("+") != -1) {

            if(sb.indexOf("'") != -1 ) {
                sb.deleteCharAt(sb.indexOf("'"));
            }
            if(sb.indexOf("&") != -1){
                sb.replace(sb.indexOf("&"),sb.indexOf("&")+1,"and");
            }
            if(sb.indexOf("+") != -1){
                sb.replace(sb.indexOf("+"),sb.indexOf("+")+1,"_and_");
            }

        }
        lowerCase = sb.toString();

        return lowerCase;
    }

    private void randomRestaurant(){
        DatabaseReference restaurantsRef = mDatabase.child(Constant.restaurantSTR);
        restaurantsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Restaurant> restaurantList = new LinkedList<>();
                Random random = new Random();
                for (DataSnapshot restaurant : dataSnapshot.getChildren()) {
                    restaurantList.add(restaurant.getValue(Restaurant.class));
                }

                Restaurant randomRestaurant = restaurantList.get(random.nextInt
                        (restaurantList.size()));

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    private ArrayList<String> autoComplete(String restaurantName){

        ArrayList<String> restaurantNameArray = new ArrayList<String>();
        String tempArr[] = restaurantName.split(" ");

        for (String s: Constant.Restaurants
             ) {
            //check if string has any punctuation, remove all of them for both strings, compare them
            //in no-punct and lower case form

            for(String tempS : tempArr) {
                if (s.replaceAll("\\p{P}", "").toLowerCase().contains(
                        tempS.replaceAll("[^A-Za-z0-9]+", "").toLowerCase())) {
                    if(!restaurantNameArray.contains(s))
                        restaurantNameArray.add(s);
                }
            }
        }

        return restaurantNameArray;
    }

    public void onClick(View view) {
        Button joinLeaveButton = activity.findViewById(view.getId());
        final EatPostView selectedPost = (EatPostView) joinLeaveButton.getParent();

        if (selectedPost.isJoining()) {
            activePostLayout.setVisibility(View.VISIBLE);

            TextView restaurantName = new TextView(activity.getApplicationContext());
            restaurantName.setText(selectedPost.getEatPostRestaurant());
            restaurantName.setId(View.generateViewId());
            TextView restaurantTime = new TextView(activity.getApplicationContext());
            restaurantTime.setText(selectedPost.getEatPostTime());
            restaurantTime.setId(View.generateViewId());
            TextView restaurantCounter = new TextView(activity.getApplicationContext());
            restaurantCounter.setText(selectedPost.getEatPostCountdown());
            restaurantCounter.setId(View.generateViewId());
            TextView seatsLeft = new TextView(activity.getApplicationContext());
            seatsLeft.setText(selectedPost.getEatPostSeats());
            seatsLeft.setId(View.generateViewId());
            ImageButton deleteButton = new ImageButton(activity.getApplicationContext());
            deleteButton.setImageResource(R.drawable.ic_delete_black_24dp);
            deleteButton.setId(View.generateViewId());

            RelativeLayout.LayoutParams nameParams = new RelativeLayout.LayoutParams(RelativeLayout
                    .LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            nameParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
            nameParams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);

            RelativeLayout.LayoutParams timeParams = new RelativeLayout.LayoutParams(RelativeLayout
                    .LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            timeParams.addRule(RelativeLayout.RIGHT_OF, restaurantName.getId());
            timeParams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);

            RelativeLayout.LayoutParams counterParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.
                    WRAP_CONTENT);
            counterParams.addRule(RelativeLayout.RIGHT_OF, restaurantTime.getId());
            counterParams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);

            RelativeLayout.LayoutParams seatsParams = new RelativeLayout.LayoutParams(RelativeLayout
                    .LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
            ;
            seatsParams.addRule(RelativeLayout.RIGHT_OF, restaurantCounter.getId());
            seatsParams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);

            RelativeLayout.LayoutParams deleteParams = new RelativeLayout.LayoutParams(
                    RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.
                    WRAP_CONTENT);
            deleteParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
            deleteParams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);

            activePostLayout.addView(restaurantName, nameParams);
            activePostLayout.addView(restaurantTime, timeParams);
            activePostLayout.addView(restaurantCounter, counterParams);
            activePostLayout.addView(seatsLeft, seatsParams);
            activePostLayout.addView(deleteButton, deleteParams);

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    selectedPost.leavePost();
                    activePostLayout.setVisibility(View.GONE);
                }
            });
        } else {
            activePostLayout.setVisibility(View.GONE);
        }
    }
}


