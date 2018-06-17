package com.o1.ucsdeats;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.net.URI;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

public class RestaurantActivity extends AppCompatActivity implements GestureDetector.OnGestureListener {
    private static final int SWIPE_THRESHOLD = 80;
    private static final int SWIPE_VELOCITY_THRESHOLD = 80;
    private static final int MAX_POSTS_LOADABLE = 10;

    private GestureDetectorCompat detector;

    private ImageView image;
    private TextView numPhotosView;
    private TextView descriptionView;
    private ImageButton prevImageButton;
    private ImageButton nextImageButton;

    private Bitmap mainRestaurantImage;
    private List<Bitmap> restaurantImages;
    private List<String> restaurantComments;
    private HashMap<String, Review> restaurantReviews;
    private Map<Integer, Integer> imageNumber;
    private int photoCounter = -1;

    private long timeStart;

    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private FirebaseStorage mStorage;
    private TextView mLocationInfo;
    private TextView mHoursInfo;
    private Button mMenuButton;
    private RatingBar mRatingBar;
    private FirebaseUser User;
    private Bitmap bitmap;

    //private float smallTextSize = getResources().getDimension(R.dimen.small_text_size);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_restaurant);

        Intent intent = getIntent();
        final String name = intent.getStringExtra("Name");


        mDatabase = FirebaseDatabase.getInstance().getReference();
        mStorage = FirebaseStorage.getInstance();
        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot dataSnapshot) {
                final Restaurant restaurant = dataSnapshot.child("Restaurants").child(name).getValue(Restaurant.class);
                if(restaurant == null) {
                    System.err.println("Null restaurant");
                    return;
                }
                mLocationInfo = findViewById(R.id.location_info);
                mLocationInfo.setText(restaurant.location);

                mHoursInfo = findViewById(R.id.hours_info);
                mHoursInfo.setText(getHours(restaurant));

                mLocationInfo = findViewById(R.id.location_info);
                mLocationInfo.setClickable(true);
                mLocationInfo.setMovementMethod(LinkMovementMethod.getInstance());
                String linkLocation = restaurant.location;
                try {
                    linkLocation = URLEncoder.encode(restaurant.location, "utf-8");
                } catch(Exception e) {

                }
                String text = "<a href='https://www.google.com/maps/search/?api=1&query=" + linkLocation + "'>" + restaurant.location +" </a>";
                System.out.println(text);
                mLocationInfo.setText(Html.fromHtml(text));
                mLocationInfo.setLinkTextColor(Color.BLUE);

                FirebaseDatabase.getInstance();

                mMenuButton = findViewById(R.id.menu_button);
                if(restaurant.menu_link != null && !restaurant.menu_link.isEmpty()) {
                    String menuUrl = restaurant.menu_link;
                    setMenuLink(Uri.parse(menuUrl));
                } else {
                    mStorage.getReference().child("restaurant_menu/"+restaurant.menu_folder+".pdf").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(final Uri uri) {
                            setMenuLink(uri);
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            mMenuButton.setVisibility(View.INVISIBLE);
                        }
                    });
                }


                Button writeReviewButton = findViewById(R.id.write_review_button);
                writeReviewButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(mAuth.getCurrentUser() == null) {
                            Toast toast = Toast.makeText(getApplicationContext(), "Must be logged in to write a review", Toast.LENGTH_LONG);
                            toast.show();
                            Intent loginActivity = new Intent(getApplicationContext(), LoginActivity.class);
                            startActivity(loginActivity);
                        } else {
                            Intent writeReviewActivity = new Intent(getApplicationContext(), WriteReviewActivity.class);
                            writeReviewActivity.putExtra("restaurant_folder", restaurant.name);
                            writeReviewActivity.putExtra("restaurant_name", restaurant.name);
                            startActivity(writeReviewActivity);
                        }
                    }
                });

                Button uploadImageButton = findViewById(R.id.upload_image_button);
                uploadImageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(mAuth.getCurrentUser() == null) {
                            Toast toast = Toast.makeText(getApplicationContext(), "Must be logged in to upload a photo", Toast.LENGTH_LONG);
                            toast.show();
                            Intent loginActivity = new Intent(getApplicationContext(), LoginActivity.class);
                            startActivity(loginActivity);
                        } else {
                            Intent uploadActivity = new Intent(getApplicationContext(), UploadRestaurantPhotoActivity.class);
                            uploadActivity.putExtra("restaurant_folder", restaurant.menu_folder);
                            uploadActivity.putExtra("restaurant_name", restaurant.name);
                            startActivity(uploadActivity);
                        }
                    }
                });

                restaurantReviews = new HashMap<>();
                DataSnapshot users = dataSnapshot.child("Users");
                Iterable<DataSnapshot> reviewChildren = dataSnapshot.child("RestaurantReviews").child(restaurant.name).getChildren();

                while(reviewChildren.iterator().hasNext()) {
                    DataSnapshot next = reviewChildren.iterator().next();
                    HashMap<String, Object> map = (HashMap<String, Object>)next.getValue();
                    Review review = new Review((Long) map.get("Rating"), (String) map.get("Text"), (String) map.get("Date"));
                    User user = users.child(next.getKey()).getValue(User.class);
                    String userName = user != null ? user.name : next.getKey();
                    restaurantReviews.put(userName, review);
                }

                mRatingBar = findViewById(R.id.rating_bar);
                float rating = calculateRating(restaurantReviews);
                mRatingBar.setRating(rating);
                TextView ratingText = findViewById(R.id.star_value);
                DecimalFormat formatter = new DecimalFormat();
                formatter.setMaximumFractionDigits( 2 );
                ratingText.setText(formatter.format(rating));

                restaurantImages = new ArrayList<>();
                restaurantComments = new ArrayList<>();
                mStorage.getReference().child("restaurant_image").child(restaurant.menu_folder + ".jpg").getBytes(1000 * 10 * 10 * 10).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        byte[] data = bytes;
                        mainRestaurantImage = BitmapFactory.decodeByteArray(data, 0, data.length);
                        restaurantImages.add(mainRestaurantImage);
                        restaurantComments.add(restaurant.name);
                        loadRestaurantImages(mainRestaurantImage);
                        loadUserRestaurantPhotos(dataSnapshot, restaurant);
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                    }
                });


            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };


        mDatabase.addValueEventListener(postListener);

        detector = new GestureDetectorCompat(getApplicationContext(), this);

        TextView restaurantName = findViewById(R.id.restaurant_name);
        restaurantName.setText(name);

        restaurantImages = new ArrayList<Bitmap>();


        ScrollView mainScroll = (ScrollView) findViewById(R.id.main_scroll);
        ObjectAnimator masterScrollAnimator = ObjectAnimator.ofInt(mainScroll, "scrollY", mainScroll.getBottom());
        masterScrollAnimator.setDuration(1000);
        masterScrollAnimator.start();

        float rating = (float) 4.25;
        LinearLayout starLayout = findViewById(R.id.star_layout);
        RatingBar starRating = findViewById(R.id.rating_bar);
        starRating.setRating(rating);

        ConstraintLayout innerConstraintLayout = findViewById(R.id.constraint_layout);
        ConstraintSet restaurantActivityConstraints = new ConstraintSet();
        restaurantActivityConstraints.clone(innerConstraintLayout);
        restaurantActivityConstraints.applyTo(innerConstraintLayout);

        Button uploadImageButton = findViewById(R.id.upload_image_button);
        uploadImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent uploadActivity = new Intent(getApplicationContext(), UploadRestaurantPhotoActivity.class);
                startActivity(uploadActivity);
            }
        });

        Button addPostButton = findViewById(R.id.write_post_button);
        addPostButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mAuth = FirebaseAuth.getInstance();
                User = mAuth.getCurrentUser();
                if(User != null) {
                    Intent postActivity = new Intent(getApplicationContext(), AddPostActivity.class);
                    postActivity.putExtra("Restaurant_name", name);
                    startActivity(postActivity);
                }else{
                    Toast toast = Toast.makeText(getApplicationContext(), "Must be logged in to make a post", Toast.LENGTH_LONG);
                    toast.show();
                    Intent loginActivity = new Intent(getApplicationContext(),LoginActivity.class);
                    startActivity(loginActivity);
                }
            }
        });

        loadContent(true,name);

        FrameLayout f1 = findViewById(R.id.active_review_button_frame);
        f1.setBackgroundResource(R.drawable.textbox_shape_compact_gray);
        Button b1 = findViewById(R.id.active_post_button);
        b1.setBackgroundColor(Color.WHITE);
        FrameLayout f2 = findViewById(R.id.active_post_button_frame);
        f2.setBackgroundResource(R.drawable.textbox_shape_compact);
        Button b2 = findViewById(R.id.active_review_button);
        b2.setBackgroundColor(getResources().getColor(R.color.nearlyWhite));

        Button postButton = findViewById(R.id.active_post_button);
        postButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadContent(true,name);
                FrameLayout f1 = findViewById(R.id.active_review_button_frame);
                f1.setBackgroundResource(R.drawable.textbox_shape_compact_gray);
                Button b1 = findViewById(R.id.active_post_button);
                b1.setBackgroundColor(Color.WHITE);
                FrameLayout f2 = findViewById(R.id.active_post_button_frame);
                f2.setBackgroundResource(R.drawable.textbox_shape_compact);
                Button b2 = findViewById(R.id.active_review_button);
                b2.setBackgroundColor(getResources().getColor(R.color.nearlyWhite));
            }
        });

        Button reviewButton = findViewById(R.id.active_review_button);
        reviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadContent(false,name);
                FrameLayout f1 = findViewById(R.id.active_post_button_frame);
                f1.setBackgroundResource(R.drawable.textbox_shape_compact_gray);
                Button b1 = findViewById(R.id.active_post_button);
                b1.setBackgroundColor(getResources().getColor(R.color.nearlyWhite));
                FrameLayout f2 = findViewById(R.id.active_review_button_frame);
                f2.setBackgroundResource(R.drawable.textbox_shape_compact);
                Button b2 = findViewById(R.id.active_review_button);
                b2.setBackgroundColor(Color.WHITE);
            }
        });

        final Activity a = this;
        ScrollView pageScroll = findViewById(R.id.main_scroll);
        pageScroll.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {
                ScrollView pageScroll = findViewById(R.id.main_scroll);

                LinearLayout child = (LinearLayout) pageScroll.getChildAt(0);
                LinearLayout postsListLayout = findViewById(R.id.active_content_layout);

                if (null != child.getChildAt(0)) {
                    if (pageScroll.getChildAt(0).getBottom() <= (pageScroll.getHeight()
                            + pageScroll.getScrollY() + 1)) {
                        Button postButton = findViewById(R.id.active_post_button);
                        if (postsListLayout.getChildCount() > MAX_POSTS_LOADABLE) {
                            ImageView img = (ImageView) ((RelativeLayout) ((RelativeLayout)
                                    postsListLayout.getChildAt(
                                            postsListLayout.getChildCount() - 10)).
                                    getChildAt(1)).getChildAt(1);
                            img.setImageBitmap(null);
                            postsListLayout.getChildAt(postsListLayout.getChildCount()
                                    - MAX_POSTS_LOADABLE).setVisibility(View.GONE);
                            pageScroll.scrollTo((int) postsListLayout.getChildAt(
                                    postsListLayout.getChildCount() - 3).getX() + 1,
                                    (int) postsListLayout.getChildAt(
                                            postsListLayout.getChildCount() - 3).getY() + 1);
                        }
                    } else if (postsListLayout.getChildAt(postsListLayout.getChildCount() - 1
                    ).getBottom() > (pageScroll.getHeight() + pageScroll.getScrollY())) {
                        if (postsListLayout.getChildCount() > MAX_POSTS_LOADABLE) {
                            pageScroll.scrollTo((int) postsListLayout.getChildAt(
                                    postsListLayout.getChildCount() - 1).getX(),
                                    (int) postsListLayout.getChildAt(
                                            postsListLayout.getChildCount() - 1).getY());

                            ImageView img = (ImageView) ((RelativeLayout) ((RelativeLayout)
                                    postsListLayout.getChildAt(postsListLayout.
                                            getChildCount() - 10)).getChildAt(1)).
                                    getChildAt(1);
                            img.setImageBitmap(BitmapFactory.decodeResource(a.getResources(),
                                    R.drawable.china_flag));
                            postsListLayout.getChildAt(postsListLayout.getChildCount()
                                    - MAX_POSTS_LOADABLE).setVisibility(View.VISIBLE);

                            postsListLayout.removeViewAt(postsListLayout.getChildCount()- 1);}
                    }
                }
            }
        });
    }

    private void loadUserRestaurantPhotos(DataSnapshot dataSnapshot, Restaurant restaurant) {
        Iterable<DataSnapshot> children = dataSnapshot.child(Constant.restaurantPhotosSTR)
                .child(restaurant.name).getChildren();
        while(children.iterator().hasNext()) {
            HashMap<String, String> map = (HashMap<String, String>)children.iterator().next().getValue();
            final UserUploadedPhoto uup = new UserUploadedPhoto(map.get("Image")
                    , map.get("Text"), map.get("UserId"));
            mStorage.getReferenceFromUrl(uup.Image).getBytes(1000 * 10 * 10 * 10).addOnCompleteListener(new OnCompleteListener<byte[]>() {
                @Override
                public void onComplete(@NonNull Task<byte[]> task) {
                    byte[] data = task.getResult();
                    Bitmap bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                    restaurantImages.add(bmp);
                    restaurantComments.add(uup.Text);
                    loadRestaurantImages(mainRestaurantImage);
                }
            });
        }
    }

    private void setMenuLink(final Uri uri) {
        System.out.println(uri);
        mMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });
    }

    // set up the images related to the restaurant
    private void loadRestaurantImages(Bitmap bmp) {

        if (imageNumber == null) {
            imageNumber = new HashMap<Integer, Integer>();
        }

        int restaurantImageHeight = (int) getResources().getDimension(R.dimen.restaurant_image_height);

        //ImageButton image = new ImageButton(getApplicationContext());
        ImageButton image = findViewById(R.id.restaurant_images_scroll);
        if (image != null) {
            image.setBackgroundResource(R.drawable.textbox_shape);
            image.setScaleType(ImageView.ScaleType.FIT_START);
            image.setAdjustViewBounds(true);
            image.setImageBitmap(bmp);
            image.setId(View.generateViewId());
            imageNumber.put(image.getId(), 0);

            setUpOpenImage(image, restaurantComments.get(0));
        }
    }

    // set ups the list of posts
    private void loadContent(boolean isPost,final String name) {
        // add posts to the layout
        final LinearLayout postsListLayout = findViewById(R.id.active_content_layout);
        postsListLayout.removeAllViewsInLayout();
        final Activity a = this;
        int marginSearchBar = (int) getResources().getDimension(R.dimen.fab_margin_small);
        int marginTextOffset = (int) getResources().getDimension(R.dimen.fab_margin_minimal);
        LinearLayout.LayoutParams postParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        postParams.setMargins(marginTextOffset, marginTextOffset, marginTextOffset,
                marginTextOffset);

        if (isPost) {
            // Must initialize 2 in order to activate scrolling

            mAuth = FirebaseAuth.getInstance();
            User = mAuth.getCurrentUser();
            mDatabase = FirebaseDatabase.getInstance().getReference();
            final FirebaseStorage storage = FirebaseStorage.getInstance();
            if(User != null) {

                ValueEventListener postListener = new ValueEventListener() {

                    @Override
                    public void onDataChange(final DataSnapshot dataSnapshot) {
                        ArrayList<Post> tempList = new ArrayList<>();
                        for (DataSnapshot Posts : dataSnapshot.child(Constant.postSTR)
                                .child(name).getChildren()) {
                            tempList.add(Posts.getValue(Post.class));
                        }

                        Collections.sort(tempList, new SortByTime());

                        for (Post p : tempList) {

                            final EatPostView post = new EatPostView(a, postsListLayout, p.user,
                                    p.restaurantName, p.maxNumber - p.members.size(),
                                    p.maxNumber, p.meetingTime.hour, p.meetingTime.minute,
                                    p.countDown.hour, p.countDown.minute, dataSnapshot.
                                    child(Constant.userSTR).child(p.user)
                                    .getValue(User.class).name);
                            post.setButtonVisibility(User.getUid().equals(p.user),
                                    p.maxNumber == p.members.size(),
                                    !(p.members.contains(User.getUid())));
                            post.setJoinLeaveButton(p.members.contains(User.getUid()));
                            post.removeRestaurantLink();

                            StorageReference storageRef = storage.getReference();
                            StorageReference islandRef = storageRef.child(Constant.profilePathSTR
                                    + p.user + Constant.jpgSTR);
                            final long ONE_MEGABYTE = 1024 * 1024;
                            islandRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(
                                    new OnSuccessListener<byte[]>() {
                                @Override
                                public void onSuccess(byte[] bytes) {
                                    // Data for "images/island.jpg" is returns, use this as needed
                                    bitmap = BitmapFactory.decodeByteArray(bytes,
                                            0, bytes.length);
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
        } else {
            List<TextView> reviews = createReviewPosts(this.restaurantReviews);
            for(TextView review : reviews) {
                postsListLayout.addView(review);
            }
        }
    }

    //opens up the selected image along with the others
    private void setUpOpenImage(ImageButton imageButton, final String comment) {
        final int imageId = imageButton.getId();

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggleAllRestaurantInfo(View.GONE);
                int imgNum = imageNumber.get(imageId);
                photoCounter = imgNum;

                // Create image to be viewed
                image = new ImageView(getApplicationContext());
                image.setImageBitmap(restaurantImages.get(imgNum));
                image.setId(View.generateViewId());

                // Shows number of images and the one viewed
                numPhotosView = new TextView(getApplicationContext());
                numPhotosView.setBackground(ResourcesCompat.getDrawable(getResources(),
                        R.drawable.textbox_shape, null));
                numPhotosView.setBackgroundColor(Color.TRANSPARENT);
                numPhotosView.setText(String.format(getResources().getString(R.string.num_photos), photoCounter + 1, restaurantImages.size()));
                numPhotosView.setTextColor(Color.BLACK);
                numPhotosView.setTextSize(getResources().getDimension(R.dimen.small_text_size));
                numPhotosView.setId(View.generateViewId());

                // Shows description of image
                descriptionView = new TextView(getApplicationContext());
                descriptionView.setBackground(ResourcesCompat.getDrawable(getResources(),
                        R.drawable.textbox_shape, null));
                descriptionView.setBackgroundColor(Color.TRANSPARENT);
                descriptionView.setText(comment != null ? comment :
                        "No Description Currently Available");
                descriptionView.setTextColor(Color.BLACK);
                descriptionView.setTextSize(getResources().getDimension(R.dimen.tiny_text_size));
                descriptionView.setId(View.generateViewId());

                // Button for swiping right
                prevImageButton = new ImageButton(getApplicationContext());
                prevImageButton.setImageResource(R.drawable.ic_keyboard_arrow_left_black_24dp);
                prevImageButton.setId(View.generateViewId());

                // Button for swiping left
                nextImageButton = new ImageButton(getApplicationContext());
                nextImageButton.setImageResource(R.drawable.ic_keyboard_arrow_right_black_24dp);
                nextImageButton.setId(View.generateViewId());

                // Add views to respective parent
                RelativeLayout parent = findViewById(R.id.parent_layout);
                parent.addView(image);
                parent.addView(numPhotosView);
                parent.addView(descriptionView);
                parent.addView(nextImageButton);
                parent.addView(prevImageButton);

                // Add params to image
                RelativeLayout.LayoutParams imageParams = (RelativeLayout.LayoutParams)
                        image.getLayoutParams();
                imageParams.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);
                image.setLayoutParams(imageParams);

                // Add params to num at top
                RelativeLayout.LayoutParams numPhotosParams = (RelativeLayout.LayoutParams)
                        numPhotosView.getLayoutParams();
                numPhotosParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
                numPhotosView.setLayoutParams(numPhotosParams);

                // Add params to description at bottom
                int fabMarginSmall = (int) getResources().getDimension(R.dimen.fab_margin_small);
                RelativeLayout.LayoutParams descriptionParams = (RelativeLayout.LayoutParams)
                        descriptionView.getLayoutParams();
                descriptionParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM, RelativeLayout.TRUE);
                descriptionParams.addRule(RelativeLayout.CENTER_HORIZONTAL, RelativeLayout.TRUE);
                descriptionParams.setMargins(fabMarginSmall, fabMarginSmall, fabMarginSmall,
                        fabMarginSmall);
                descriptionView.setLayoutParams(descriptionParams);

                // Add params to button on left for right swiping
                RelativeLayout.LayoutParams prevButtonParams = new RelativeLayout.LayoutParams
                        (RelativeLayout.LayoutParams.WRAP_CONTENT,
                                RelativeLayout.LayoutParams.WRAP_CONTENT);
                prevButtonParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
                prevButtonParams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
                prevImageButton.setLayoutParams(prevButtonParams);

                // Add params to button on right for left swiping
                RelativeLayout.LayoutParams nextButtonParams = new RelativeLayout.LayoutParams
                        (RelativeLayout.LayoutParams.WRAP_CONTENT,
                                RelativeLayout.LayoutParams.WRAP_CONTENT);
                nextButtonParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
                nextButtonParams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
                nextImageButton.setLayoutParams(nextButtonParams);

                if ((photoCounter + 1) >= restaurantImages.size()) {
                    nextImageButton.setVisibility(View.GONE);
                } else if (photoCounter <= 0) {
                    prevImageButton.setVisibility(View.GONE);
                }

                // Set prevImage onClickListener for a righy swipe
                prevImageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onSwipeRight();
                    }
                });

                // Set nextImage onClickListener for a left swipe
                nextImageButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        onSwipeLeft();
                    }
                });
                idleTimer();
            }
        });
    }

    @Override
    public boolean onDown(MotionEvent motionEvent) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent motionEvent) {
        if (nextImageButton != null) {
            nextImageButton.setVisibility(View.VISIBLE);
            prevImageButton.setVisibility(View.VISIBLE);
            if ((photoCounter + 1) >= restaurantImages.size()) {
                nextImageButton.setVisibility(View.GONE);
            }
            if (photoCounter <= 0) {
                prevImageButton.setVisibility(View.GONE);
            }
            idleTimer();
        }
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent motionEvent) {

    }

    @Override
    public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
        boolean result = false;
        if (photoCounter == -1) {
            return false;
        }
        try {
            float diffY = motionEvent1.getY() - motionEvent.getY();
            float diffX = motionEvent1.getX() - motionEvent.getX();
            if (Math.abs(diffX) > Math.abs(diffY)) {
                if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(v) > SWIPE_VELOCITY_THRESHOLD) {
                    if (diffX > 0) {
                        onSwipeRight();
                    } else {
                        onSwipeLeft();
                    }
                    result = true;
                }
            }
            else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(v1) > SWIPE_VELOCITY_THRESHOLD) {
                if (diffY > 0) {
                    onSwipeDown();
                } else {
                    onSwipeUp();
                }
                result = true;
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return result;
    }

    public void toggleAllRestaurantInfo(int state) {
        findViewById(R.id.upload_image_button).setVisibility(state);
        findViewById(R.id.star_layout).setVisibility(state);
        findViewById(R.id.button_layout).setVisibility(state);
        findViewById(R.id.restaurant_info).setVisibility(state);
        findViewById(R.id.restaurant_name).setVisibility(state);
        findViewById(R.id.active_post_button).setVisibility(state);
        findViewById(R.id.active_review_button).setVisibility(state);
    }

    public void removeOpenedImages() {
        RelativeLayout parent = findViewById(R.id.parent_layout);
        parent.removeView(image);
        parent.removeView(numPhotosView);
        parent.removeView(descriptionView);
        parent.removeView(prevImageButton);
        parent.removeView(nextImageButton);
    }

    public void onSwipeRight() {
        if ((photoCounter - 1) >= 0) {
            photoCounter--;
            numPhotosView.setText(String.format(getResources().getString(R.string.num_photos), photoCounter + 1,
                    restaurantImages.size()));
            image.setImageBitmap(restaurantImages.get(photoCounter));
            String txt = Integer.toString(photoCounter);
            if(restaurantComments.size() > photoCounter) {
                String restaurantComment = restaurantComments.get(photoCounter);
                if(restaurantComment != null && !restaurantComment.isEmpty()) {
                    txt = restaurantComments.get(photoCounter);
                }
            }
            descriptionView.setText(txt);
            nextImageButton.setVisibility(View.VISIBLE);
            prevImageButton.setVisibility(View.VISIBLE);
        }
        if (photoCounter <= 0) {
            prevImageButton.setVisibility(View.GONE);
        }
        idleTimer();
    }


    public void onSwipeLeft() {
        if ((photoCounter + 1) < restaurantImages.size()) {
            photoCounter++;
            numPhotosView.setText(String.format(getResources().getString(R.string.num_photos), photoCounter + 1,
                    restaurantImages.size()));
            image.setImageBitmap(restaurantImages.get(photoCounter));
            String txt = Integer.toString(photoCounter);
            if(restaurantComments.size() > photoCounter) {
                String restaurantComment = restaurantComments.get(photoCounter);
                if(restaurantComment != null && !restaurantComment.isEmpty()) {
                    txt = restaurantComments.get(photoCounter);
                }
            }
            descriptionView.setText(txt);
            nextImageButton.setVisibility(View.VISIBLE);
            prevImageButton.setVisibility(View.VISIBLE);
        }
        if ((photoCounter + 1) >= restaurantImages.size()) {
            nextImageButton.setVisibility(View.GONE);
        }
        idleTimer();
    }

    public void onSwipeDown() {
        photoCounter = -1;
        toggleAllRestaurantInfo(View.VISIBLE);
        removeOpenedImages();
    }

    public void onSwipeUp() {

    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        detector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }

    // Timer remove image changing arrows when idle for 2 seconds
    private void idleTimer() {
        timeStart =  System.currentTimeMillis();

        Thread timerThread = new Thread() {
            public void run() {
                while ((System.currentTimeMillis() - timeStart) < 2000) {}
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        nextImageButton.setVisibility(View.GONE);
                        prevImageButton.setVisibility(View.GONE);
                    }
                });
            }
        };
        timerThread.start();
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

    private boolean checkLinkOrPdf(String name){
        for(String s: Constant.MenuLink){
            if(name.equalsIgnoreCase(s)){
                return true;
            }
        }
        return false;
    }

    private float calculateRating(HashMap<String, Review> reviews) {
        int totalUsers = reviews.size();
        long totalRating = 0;

        if(totalUsers == 0) {
            return 0;
        }

        for (Map.Entry<String, Review> pair : reviews.entrySet()) {
            totalRating += pair.getValue().Rating;
        }

        return (float) totalRating / (float) totalUsers;

    }

    private List<TextView> createReviewPosts(HashMap<String, Review> reviews) {
        List<TextView> reviewPosts = new ArrayList<>();
        for (Map.Entry<String, Review> pair : reviews.entrySet()) {
            TextView text = new TextView(this);
            String dateStr = pair.getValue().Date;
            String prettyDate = Utils.prettifyDate(Utils.parseDateStamp(dateStr));
            String reviewStr = pair.getKey() + " (" + prettyDate + "): " + pair.getValue().Text;
            text.setText(reviewStr);
            text.setTextSize(20);
            text.setTextColor(getResources().getColor(R.color.ucsd_eat_text));
            reviewPosts.add(text);
        }
        return reviewPosts;
    }

    private List<UserUploadedPhoto> getPhotoList(HashMap<String, UserUploadedPhoto> photos) {
        List<UserUploadedPhoto> photoList = new ArrayList<>();
        for (Map.Entry<String, UserUploadedPhoto> pair : photos.entrySet()) {
           photoList.add(pair.getValue());
        }

        return photoList;
    }

    @Override
    public void onBackPressed() {
        if (findViewById(R.id.restaurant_images_layout).getVisibility() == View.GONE) {
            onSwipeDown();
        } else {
            recycleImages();
            finish();
        }
    }

    // Recycles all images, used upon exiting activity
    private void recycleImages() {
        if(restaurantImages.isEmpty())
            return;
        for (Bitmap bmp:restaurantImages) {
            bmp.recycle();
        }
        restaurantImages.clear();

        LinearLayout postsListLayout = findViewById(R.id.active_content_layout);
        int imageNum = postsListLayout.getChildCount();
        if (MAX_POSTS_LOADABLE <= imageNum) {
            imageNum = MAX_POSTS_LOADABLE;
        }
        for (int i = 1; i < imageNum; i++) {
            ImageView img = (ImageView) ((RelativeLayout) ((RelativeLayout)
                    postsListLayout.getChildAt(postsListLayout.getChildCount() - i))
                    .getChildAt(1)).getChildAt(1);
            if (null != img) {
                ((BitmapDrawable) img.getDrawable()).getBitmap().recycle();
            }
        }
    }
}