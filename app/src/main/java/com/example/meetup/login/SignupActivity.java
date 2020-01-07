package com.example.meetup.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.meetup.R;
import com.example.meetup.databinding.ActivitySignupBinding;
import com.example.meetup.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class SignupActivity extends AppCompatActivity implements View.OnClickListener{
    private static final int RC_SIGN_IN = 0;
    private static final String TAG = "google";
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mUserDatabaseReference;
    private ChildEventListener mChildEventListener;

    ActivitySignupBinding activitySignupBinding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activitySignupBinding= DataBindingUtil.setContentView(this, R.layout.activity_signup);
        initialize();
    }

    private void initialize() {
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mUserDatabaseReference = mFirebaseDatabase.getReference().child("Users");
        activitySignupBinding.submitsign.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.submitsign:
                if (validation()) {
                   register();
                }
                break;
        }

    }

    private void register() {
        mAuth.createUserWithEmailAndPassword(activitySignupBinding.emailedit.getText().toString(),activitySignupBinding.passwordedit.getText().toString())
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            User user=new User(activitySignupBinding.usernameedit.getText().toString(),activitySignupBinding.emailedit.getText().toString());
                            mUserDatabaseReference.push().setValue(user);
                            navigateToLogin();
                        }
                    }

                });
    }

    private void navigateToLogin() {
        startActivity(new Intent(this,MainActivity.class));
        finish();
    }

    private boolean validation() {
        if (!activitySignupBinding.usernameedit.getText().toString().isEmpty()) {
            if(!activitySignupBinding.emailedit.getText().toString().isEmpty()){
                if (!activitySignupBinding.passwordedit.getText().toString().isEmpty()) {
                    return true;
                } else
                    return false;
            }
            else
                return false;
        } else
            return false;
    }
}
