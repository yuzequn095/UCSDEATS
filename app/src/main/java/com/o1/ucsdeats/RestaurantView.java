package com.o1.ucsdeats;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by Peter on 5/4/2018.
 */

public class RestaurantView extends View {
    private LinearLayout parentLayout;
    private ImageView restaurantImage;
    private TextView restaurantName;
    private TextView restaurantLocation;
    private Activity activity;

    public RestaurantView(Activity a, LinearLayout l) {
        super(a);
        activity = a;
        parentLayout = l;
        createView();
    }

    private void createView() {
        // Create button to link to restaurant page
        final Button openRestaurantButton = new Button(activity);
        openRestaurantButton.getBackground().setAlpha(0);
        openRestaurantButton.setId(View.generateViewId());

        // Create layout store each set of info
        final RelativeLayout restaurantFrame = new RelativeLayout(activity);
        restaurantFrame.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.textbox_shape, null));
        restaurantFrame.setId(View.generateViewId());
        restaurantFrame.setBackgroundColor(Color.TRANSPARENT);

        // Create restaurant's image preferably logo
        restaurantImage = new ImageView(activity);
        //restaurantImage.setBackgroundColor(Color.GREEN);
        restaurantImage.setId(View.generateViewId());

        // Create name of restaurant
        restaurantName = new TextView(activity);
        restaurantName.setTextColor(Color.BLACK);
        restaurantName.setId(View.generateViewId());
        restaurantName.setTextSize(24);
        restaurantName.setTextColor(getResources().getColor(R.color.ucsd_eat_text));
        restaurantName.setTypeface(null, Typeface.BOLD);

        // Create location of restaurant
        restaurantLocation = new TextView(activity);
        restaurantLocation.setTextColor(Color.GRAY);
        restaurantLocation.setId(View.generateViewId());
        restaurantLocation.setTextColor(getResources().getColor(R.color.ucsd_eat_text));
        restaurantLocation.setTypeface(null, Typeface.BOLD);

        // Set params for the main layout
        int restaurantTextboxHeight = (int) activity.getResources().getDimension(R.dimen.restaurant_text_box_height);
        int fabMarginSmall = (int) activity.getResources().getDimension(R.dimen.fab_margin_small);
        int fabMarginBig = (int) activity.getResources().getDimension(R.dimen.fab_margin);
        int fabMarginMinimal = (int) activity.getResources().getDimension(R.dimen.fab_margin_minimal);
        final LinearLayout.LayoutParams restaurantFrameParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, restaurantTextboxHeight);
        restaurantFrameParams.setMargins(fabMarginSmall, fabMarginMinimal,fabMarginSmall, fabMarginMinimal);

        // Set params for the button
        final RelativeLayout.LayoutParams openRestaurantParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);

        // Set restaurant imagee params on far right
        final RelativeLayout.LayoutParams restaurantImageParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        restaurantImageParams.width = restaurantTextboxHeight;
        restaurantImageParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, RelativeLayout.TRUE);
        restaurantImageParams.addRule(RelativeLayout.CENTER_VERTICAL, RelativeLayout.TRUE);
        restaurantImageParams.setMargins(fabMarginSmall, fabMarginBig, fabMarginSmall, fabMarginSmall);
        restaurantImageParams.addRule(RelativeLayout.BELOW, restaurantName.getId());

        // Set name params
        final RelativeLayout.LayoutParams restaurantNameParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        restaurantNameParams.addRule(RelativeLayout.ALIGN_PARENT_LEFT, RelativeLayout.TRUE);
        restaurantNameParams.setMargins(fabMarginSmall, fabMarginSmall, fabMarginSmall, fabMarginSmall);

        // Set location params
        final RelativeLayout.LayoutParams restaurantLocationParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        restaurantLocationParams.addRule(RelativeLayout.BELOW, restaurantName.getId());
        restaurantLocationParams.setMargins(fabMarginSmall, fabMarginSmall, fabMarginSmall, fabMarginSmall);

        // Add all views to respective parents
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                parentLayout.addView(restaurantFrame, restaurantFrameParams);
                restaurantFrame.addView(openRestaurantButton, openRestaurantParams);
                restaurantFrame.addView(restaurantImage, restaurantImageParams);
                restaurantFrame.addView(restaurantName, restaurantNameParams);
                restaurantFrame.addView(restaurantLocation, restaurantLocationParams);
            }
        });

        // Set onClickListener for each RestaurantView
        openRestaurantButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent restaurantActivity = new Intent(activity, RestaurantActivity.class);
                restaurantActivity.putExtra("Name", restaurantName.getText().toString());
                activity.startActivity(restaurantActivity);
            }
        });
    }

    // Set name of restaurant
    public void setRestaurantName(String name) {
        restaurantName.setText(name);
    }

    // Set location of restaurant
    public void setRestaurantLocation(String loc) {
        restaurantLocation.setText(loc);
    }

    // Set image for restaurant using Bitmap
    public void setRestaurantImage(Bitmap bm) {
        restaurantImage.setImageBitmap(bm);
    }

    // Set image for restaurant using Drawable
    public void setRestaurantImage(Drawable d) {
        restaurantImage.setImageDrawable(d);
    }
}
