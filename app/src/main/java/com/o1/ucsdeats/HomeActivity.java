package com.o1.ucsdeats;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.BottomNavigationView;
import android.support.design.widget.Snackbar;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
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

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import static android.Manifest.permission.ACCESS_COARSE_LOCATION;
import static android.Manifest.permission.ACCESS_FINE_LOCATION;

public class HomeActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, LoaderCallbacks<Cursor> {
    private final int SEARCH_PAGE = 0;
    private final int RECOMMEND_PAGE = 1;
    private final int FRIENDS_PAGE = 2;
    private final int EAT_PAGE = 3;

    private static final int REQUEST_GPS_LOCATION = 0;

    private static HomeUILoader homeUILoader;

    private static ConstraintLayout constraintLayout;
    private DatabaseReference mDatabase;
    private FirebaseUser mUser;

    private FirebaseAuth mAuth;

    private Button mapTextButton;
    private TextView emailView;
    private TextView nameView;

    @Override
    protected void onCreate(final Bundle savedInstanceState) {

        final FirebaseStorage storage = FirebaseStorage.getInstance();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mUser = mAuth.getCurrentUser();

        constraintLayout = findViewById(R.id.constraint_layout);
        homeUILoader = new HomeUILoader(this, constraintLayout);
        homeUILoader.createPage(SEARCH_PAGE);

        constraintLayout.setScrollContainer(false);
        findViewById(R.id.nav_bottom).setScrollContainer(false);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        final NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        emailView = (TextView)navigationView.getHeaderView(0).findViewById(R.id.header_email);
        nameView = (TextView)navigationView.getHeaderView(0).findViewById(R.id.header_name);

        final ImageButton profilePicButton = (ImageButton) navigationView.getHeaderView(0)
                .findViewById(R.id.header_photo);
        profilePicButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                mUser = mAuth.getCurrentUser();
                if (mUser == null) {
                    Intent loginActivity = new Intent(getApplicationContext(), LoginActivity.class);
                    startActivity(loginActivity);
                    mAuth = FirebaseAuth.getInstance();
                    mDatabase = FirebaseDatabase.getInstance().getReference();
                    mUser = mAuth.getCurrentUser();
                }else {
                    Intent profileActivity = new Intent(getApplicationContext(), ViewProfile.class);
                    startActivity(profileActivity);
                    mAuth = FirebaseAuth.getInstance();
                    mDatabase = FirebaseDatabase.getInstance().getReference();
                    mUser = mAuth.getCurrentUser();
                }
            }
        });

        mAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if(firebaseAuth.getCurrentUser()!= null){
                    mDatabase.addValueEventListener( new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            // Get User object and use the values to update the UI
                            FirebaseUser currentUser = mAuth.getCurrentUser();
                            if (currentUser != null) {
                                User user = dataSnapshot.child(Constant.userSTR).
                                        child(currentUser.getUid()).getValue(User.class);

                                //Display User's email
                                emailView.setText(user.email);
                                nameView.setText(user.name);

                                StorageReference storageRef = storage.getReference();
                                StorageReference islandRef = storageRef.child(Constant.
                                        profilePathSTR + currentUser.getUid() + Constant.jpgSTR);

                                final long ONE_MEGABYTE = 1024 * 1024;
                                islandRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(
                                        new OnSuccessListener<byte[]>() {
                                    @Override
                                    public void onSuccess(byte[] bytes) {
                                        // Data for "images/island.jpg" is returns, use this
                                        // as needed
                                        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,
                                                0, bytes.length);
                                        profilePicButton.setScaleType(ImageView.ScaleType.
                                                FIT_START);
                                        profilePicButton.setAdjustViewBounds(true);
                                        //mProfilePhoto.setCropToPadding(true);
                                        profilePicButton.setImageBitmap(bitmap);

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
                            // Getting Post failed, log a message
                            Log.w("profile", "loadPost:onCancelled",
                                    databaseError.toException());
                            // ...
                        }
                    });
                }
                else{
                    emailView = (TextView)navigationView.getHeaderView(0).
                            findViewById(R.id.header_email);
                    nameView = (TextView)navigationView.getHeaderView(0).
                            findViewById(R.id.header_name);
                    emailView.setText("N/A");
                    nameView.setText("Please sign in first");
                    profilePicButton.setImageResource(R.mipmap.ic_launcher_round);

                }

            }
        });


        populateMap();

        BottomNavigationView bottomNavigationView = (BottomNavigationView)
                findViewById(R.id.nav_bottom);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.navigation_search:
                        homeUILoader.createPage(SEARCH_PAGE);
                        return true;
                    case R.id.navigation_recommend:
                        homeUILoader.createPage(RECOMMEND_PAGE);
                        return true;
                    case R.id.navigation_friends:
                        homeUILoader.createPage(FRIENDS_PAGE);
                        return true;
                    case R.id.navigation_find_someone:
                        homeUILoader.createPage(EAT_PAGE);
                        return true;
                }
                return false;
            }
        });

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
       /* if (id == R.id.action_settings) {
            Intent intent = new Intent(getApplicationContext(), RestaurantActivity.class);
            startActivity(intent);
            return true;
        } else if (id == R.id.action_map) {
            Intent intent = new Intent(getApplicationContext(), MapsActivity.class);
            startActivity(intent);
            return true;
        } else */
       if (id == R.id.random_restaurant) {
            randomRestaurant();
        }

        return super.onOptionsItemSelected(item);
    }

    private void randomRestaurant(){
        DatabaseReference restaurantsRef = mDatabase.child(Constant.restaurantSTR);
        final Activity a = this;
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
                final String name = randomRestaurant.name;

                AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new AlertDialog.Builder(a, android.R.style.
                            Theme_DeviceDefault_Dialog_Alert);
                } else {
                    builder = new AlertDialog.Builder(a);
                }
                builder.setTitle("Random Restaurant Recommendation:")
                        .setMessage("Would you like to eat at " + name + "?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Intent restaurantActivity = new Intent(a, RestaurantActivity.class);
                                restaurantActivity.putExtra(Constant.nameSTR, name);
                                startActivity(restaurantActivity);
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .setIcon(R.drawable.ic_baseline_not_listed_location_24px)
                        .show();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        FirebaseUser user = mAuth.getCurrentUser();

        if (id == R.id.nav_profile) {

            if(user != null) {
                Intent intent = new Intent(getApplicationContext(), ViewProfile.class);
                startActivity(intent);
            }
            else{
                Context context = getApplicationContext();
                CharSequence text = "Please Sign in first";
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
                Intent loginActivity = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(loginActivity);
                mAuth = FirebaseAuth.getInstance();
                mDatabase = FirebaseDatabase.getInstance().getReference();
                mUser = mAuth.getCurrentUser();
            }

        } else if (id == R.id.nav_logout) {

            if(user != null){
                mAuth.signOut();
                Context context = getApplicationContext();
                CharSequence text = "Logged out!";
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }
            else{
                Context context = getApplicationContext();
                CharSequence text = "Not Signed in";
                int duration = Toast.LENGTH_SHORT;
                Toast toast = Toast.makeText(context, text, duration);
                toast.show();
            }


        } else if (id == R.id.nav_about_us) {
            Intent intent = new Intent(getApplicationContext(), AboutUs.class);
            startActivity(intent);
        }


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void populateMap() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                checkSelfPermission(ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(this, "Granted", Toast.LENGTH_SHORT);
            return true;
        }
        if (shouldShowRequestPermissionRationale(ACCESS_FINE_LOCATION)) {
            Snackbar.make(findViewById(R.id.nav_bottom), R.string.permission_rationale,
                    Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{ACCESS_FINE_LOCATION},
                                    REQUEST_GPS_LOCATION);
                        }
                    });
        } else {
            requestPermissions(new String[]{ACCESS_FINE_LOCATION}, REQUEST_GPS_LOCATION);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_GPS_LOCATION) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            }
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
