package com.o1.ucsdeats;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.TimeZone;

public class AddPostActivity extends AppCompatActivity {
    final int numSize = 40;

    final int MAX_MIN_ONE = 10;
    final int MAX_MIN_TEN = 6;
    final int MAX_HOUR = 3;
    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private Post post;
    private Calendar currentTime;
    private String selectedRestaurant;
    private Spinner restaurantSpinner;
    private DatabaseReference mDatabase;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        databaseReference = FirebaseDatabase.getInstance().getReference();
        TimeZone tz = TimeZone.getTimeZone(Constant.timeZoneSTR);
        currentTime = Calendar.getInstance(tz);
        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        post = new Post();

        mDatabase = FirebaseDatabase.getInstance().getReference(Constant.userSTR).
                child(mCurrentUser.getUid());
        mDatabase.addValueEventListener( new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                user = dataSnapshot.getValue(User.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message

            }
        });

        final SeekBar seatsBar = findViewById(R.id.seats_seekbar);
        seatsBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                TextView seatVal = findViewById(R.id.seats_val);
                seatVal.setText("" + (i + 1));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });


        // Restaurants Spinner
        final List<String> sinnperList = new ArrayList<>(Arrays.asList(Constant.Restaurants));
        restaurantSpinner = findViewById(R.id.restaurant_spinner);
        ArrayAdapter<String> spinnerArrayAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,sinnperList);
        restaurantSpinner.setAdapter(spinnerArrayAdapter);
        Intent intent = getIntent();
        String temp = intent.getStringExtra("Restaurant_name");
        if(temp != null) {
            for (int i = 0; i < restaurantSpinner.getAdapter().getCount(); i++)
                if (restaurantSpinner.getAdapter().getItem(i).toString().contains(temp))
                    restaurantSpinner.setSelection(i);
        }
    }

    public void changeValue(View view) {
        switch (view.getId()) {
            case R.id.hour_timer_up:
                setValue(true, R.id.hour_timer_val);
                break;
            case R.id.min_ten_timer_up:
                setValue(true, R.id.min_ten_timer_val);
                break;
            case R.id.min_one_timer_up:
                setValue(true, R.id.min_one_timer_val);
                break;
            case R.id.hour_timer_down:
                setValue(false, R.id.hour_timer_val);
                break;
            case R.id.min_ten_timer_down:
                setValue(false, R.id.min_ten_timer_val);
                break;
            case R.id.min_one_timer_down:
                setValue(false, R.id.min_one_timer_val);
        }

    }

    private void setValue(boolean isUp, int id) {
        int change = -1;
        if (isUp) {
            change = 1;
        }

        EditText num = findViewById(id);
        int newVal = Integer.parseInt(num.getText().toString()) + change;

        int mod = MAX_HOUR;
        if (id == R.id.min_one_timer_val) {
            mod = MAX_MIN_ONE;
            if (newVal >= mod || newVal < 0) {
                setValue(isUp, R.id.min_ten_timer_val);
            }
        } else if (id == R.id.min_ten_timer_val) {
            mod = MAX_MIN_TEN;
            if (newVal >= mod || newVal < 0) {
                setValue(isUp, R.id.hour_timer_val);
            }
        }

        num.setText("" + ((newVal + mod) % mod));
    }

    public void actionSubmit (View view) {
            EditText hour = findViewById(R.id.hour_timer_val);
            EditText minTen = findViewById(R.id.min_ten_timer_val);
            EditText minOne = findViewById(R.id.min_one_timer_val);
            TextView setVale = findViewById(R.id.seats_val);
            selectedRestaurant = restaurantSpinner.getSelectedItem().toString();
            post.maxNumber = Integer.parseInt(setVale.getText().toString());
            post.meetingTime.hour = currentTime.get(Calendar.HOUR_OF_DAY);
            post.meetingTime.minute = currentTime.get(Calendar.MINUTE);
            post.countDown.hour = Integer.parseInt(hour.getText().toString());
            post.countDown.minute = Integer.parseInt(minOne.getText().toString())
                    + Integer.parseInt(minTen.getText().toString()) * 10;
            post.restaurantName = selectedRestaurant;
            post.user = mCurrentUser.getUid();

            DatabaseReference tempRef = databaseReference.child(Constant.postSTR);
            tempRef = tempRef.child(selectedRestaurant).child(post.user);
            tempRef.setValue(post);

            mDatabase.updateChildren(user.toMap());

            Context context = getApplicationContext();
            CharSequence text = "Submit successfully";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();

            // Sets a delay in order to view Splash Screen
            Thread splashThread = new Thread() {
                public void run() {
                    try {
                        sleep(1000);
                        finish();
                    } catch (InterruptedException ex) {
                        System.err.println(ex);
                    }
                }
            };

            splashThread.start();

    }


}
