package com.o1.ucsdeats;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class WriteReviewActivity extends AppCompatActivity {

    private final int MAX_STARS = 5;

    private RatingBar mRatingBar;
    private TextView mReviewText;
    private Button mSubmitButton;

    private DatabaseReference mDatabase;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_write_review);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        mAuth = FirebaseAuth.getInstance();

        mRatingBar = findViewById(R.id.rating_bar);
        mReviewText = findViewById(R.id.review_text);
        mSubmitButton = findViewById(R.id.submit_button);

        final Intent intent = getIntent();
        final String restaurant_folder = intent.getStringExtra("restaurant_folder");
        mRatingBar.setNumStars(MAX_STARS);

        mRatingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() {
            @Override
            public void onRatingChanged(RatingBar ratingBar, float v, boolean b) {
                ratingBar.setRating(v);

                TextView ratingValue = findViewById(R.id.rating_value);
                ratingValue.setText(v + "");
            }
        });

        mSubmitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int rating = (int) mRatingBar.getRating();
                String reviewText = mReviewText.getText().toString();
                Review review = new Review(new Long(rating), reviewText, Utils.createDateStamp());
                FirebaseUser user = mAuth.getCurrentUser();
                if(user == null) {
                    System.err.println("User is not logged in to make review");
                    Toast toast = Toast.makeText(getApplicationContext(),
                            "You must be logged in to leave a review", Toast.LENGTH_LONG);
                    toast.show();
                    finish();
                }
                mDatabase.child(Constant.restaurantReviewsSTR)
                        .child(restaurant_folder).child(user.getUid()).setValue(review);
                finish();

            }
        });
    }

}