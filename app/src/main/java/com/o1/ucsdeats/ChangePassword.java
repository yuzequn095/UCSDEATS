package com.o1.ucsdeats;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.FirebaseAuth;

public class ChangePassword extends AppCompatActivity {

    private EditText mNewPassword;
    private EditText mConfirmPasswor;
    private FirebaseAuth mAuth;
    private FirebaseUser mCurrentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        mNewPassword = (EditText)findViewById(R.id.new_password) ;
        mConfirmPasswor = (EditText)findViewById(R.id.confirm_password);

        mAuth = FirebaseAuth.getInstance();
        mCurrentUser = mAuth.getCurrentUser();

    }

    public void confirmPassword(View view)
    {
        if(isPasswordValid(mNewPassword.getText().toString(), mConfirmPasswor.getText().toString())){
            mCurrentUser.updatePassword(mNewPassword.getText().toString()) ;
            Context context = getApplicationContext();
            CharSequence text = "Change saved";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            finish();
        }
        else{
            Context context = getApplicationContext();
            CharSequence text = "Passwords invalid or not the same";
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(context, text, duration);
            toast.show();
            finish();
        }
    }



    public void cancelChanges(View view)
    {
        finish();
    }

    private boolean isPasswordValid(String newPassword, String confirmPassword){
        return newPassword.length()>=6 && newPassword.equals(confirmPassword);
    }

}
