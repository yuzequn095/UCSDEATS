package com.o1.ucsdeats;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
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
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;

import static android.Manifest.permission.READ_EXTERNAL_STORAGE;

public class ProfileActivity extends AppCompatActivity implements LoaderManager
        .LoaderCallbacks<Cursor> {

    private static final String TAG = "ProfileActivity";
    private static final int REQUEST_READ_STORAGE = 0;

    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;
    private TextView mNameView;
    private TextView mMajorView;
    private RadioGroup mGenderGroup;
    private TextView mPhoneNumberView;
    private TextView mIntroductionView;
    private View mScrollView;
    private View mProgressView;
    private DatabaseReference mDatabase;
    private ImageButton mProfilePhoto;
    private String userID;
    private Spinner foodS;
    private User user;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();
    StorageReference profilePictureRef;
    private String profilePictureFilename = "";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        userID = mCurrentUser.getUid();

//        Button submitButton = (Button)findViewById(R.id.save_profile_changes);
//        submitButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                actionSaveChanges(view);
//                finish();
//            }
//        });

        ValueEventListener postListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // Get User object and use the values to update the UI

                user = dataSnapshot.child(Constant.userSTR).child(mCurrentUser.getUid())
                        .getValue(User.class);

                //Display User's name
                mNameView = (TextView) findViewById(R.id.name_info);
                mNameView.setText(user.name);

                //Display User's major
                mMajorView = (TextView) findViewById(R.id.major_info);
                mMajorView.setText(user.major);

                //Display User's phoneNumber
                mPhoneNumberView = (TextView) findViewById(R.id.phone_info);
                mPhoneNumberView.setText(user.phoneNumber);

                //Display User's gender

                mGenderGroup = (RadioGroup) findViewById(R.id.gender_info);
                if(user.gender.equals("Male"))
                    mGenderGroup.check(R.id.gender_male);
                else
                    mGenderGroup.check(R.id.gender_female);

                //Display User's introduction
                mIntroductionView = (TextView)findViewById(R.id.introduction_info);
                mIntroductionView.setText(user.intro);

                foodS = (Spinner)findViewById(R.id.food_spinner);
                for(int i= 0; i < foodS.getAdapter().getCount(); i++)
                    if(foodS.getAdapter().getItem(i).toString().contains(user.food))
                        foodS.setSelection(i);
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Getting Post failed, log a message
                Log.w("profile", "loadPost:onCancelled", databaseError.toException());
                // ...
            }
        };
        mDatabase.addValueEventListener(postListener);

        StorageReference storageRef = storage.getReference();
        StorageReference islandRef = storageRef.child(Constant.profilePathSTR +
                mCurrentUser.getUid() + Constant.jpgSTR);

        final long ONE_MEGABYTE = 1024 * 1024;
        islandRef.getBytes(ONE_MEGABYTE).addOnSuccessListener(new OnSuccessListener<byte[]>() {
            @Override
            public void onSuccess(byte[] bytes) {
                // Data for "images/island.jpg" is returns, use this as needed
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
                mProfilePhoto = (ImageButton) findViewById(R.id.image_info);
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

        mProfilePhoto = (ImageButton) findViewById(R.id.image_info);
        mProfilePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.
                        Images.Media.INTERNAL_CONTENT_URI);
                startActivityForResult(intent, 100);
            }
        });
        populateImageUpload();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        mScrollView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mScrollView.setVisibility(show ? View.GONE : View.VISIBLE);
            mScrollView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mScrollView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mScrollView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    public void actionSaveChanges(View view){

        showProgress(true);
        mDatabase = FirebaseDatabase.getInstance().getReference();
        if (mGenderGroup.getCheckedRadioButtonId() != -1) {
            int genderID = mGenderGroup.getCheckedRadioButtonId();
            View radioButton = mGenderGroup.findViewById(genderID);
            int radioId = mGenderGroup.indexOfChild(radioButton);
            RadioButton btn = (RadioButton) mGenderGroup.getChildAt(radioId);
            user.gender = (String) btn.getText();
        }
        user.intro = mIntroductionView.getText().toString();
        user.phoneNumber = mPhoneNumberView.getText().toString();
        user.name = mNameView.getText().toString();
        user.major = mMajorView.getText().toString();

        Bitmap bitmap = ((BitmapDrawable)mProfilePhoto.getDrawable()).getBitmap();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] data = baos.toByteArray();

        //Upload to storage
        storageRef.child(Constant.profileSTR).child(userID + Constant.jpgSTR).delete();
        profilePictureRef = storageRef.child(Constant.profileSTR).child(userID + Constant.jpgSTR);
        UploadTask uploadTask = profilePictureRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                Log.d(TAG, "Profile picture upload: failure.");
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, etc.
                // ...
                Log.d(TAG, "Profile picture upload: success.");
                profilePictureFilename = userID + Constant.jpgSTR;
                mDatabase.child(Constant.userSTR).child(userID).child(Constant.profilePicSTR).
                        removeValue();
                user.profilePicFilename = profilePictureFilename;
                mDatabase.child(Constant.userSTR).child(userID).updateChildren(user.toMap());
                Log.d(TAG, user.profilePicFilename);
            }
        });

        Context context = getApplicationContext();
        CharSequence text = "Change saved";
        int duration = Toast.LENGTH_SHORT;
        Toast toast = Toast.makeText(context, text, duration);
        toast.show();

        // Sets a delay in order to view Splash Screen
        Thread splashThread = new Thread() {
            public void run() {
                try {
                    sleep(3000);
                    finish();
                } catch (InterruptedException ex) {
                    System.err.println(ex);
                }
            }
        };

        splashThread.start();
    }

    private void populateImageUpload() {
        if (!mayRequestContacts()) {
            return;
        }

        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_EXTERNAL_STORAGE)) {
            Snackbar.make(mProfilePhoto, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_EXTERNAL_STORAGE},
                                    REQUEST_READ_STORAGE);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_EXTERNAL_STORAGE}, REQUEST_READ_STORAGE);
        }
        return false;
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        System.out.println("onAct");
        if(resultCode == RESULT_OK) {
            System.out.println(requestCode +" OK " + resultCode);
            if(requestCode == 100) {
                System.out.println("IMAGE");
                Uri selectedImage = data.getData();
                mProfilePhoto.setImageURI(selectedImage);
            }
        }
    }
}
