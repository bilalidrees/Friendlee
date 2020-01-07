package com.example.meetup;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.example.meetup.utility.Constants;
import com.example.meetup.utility.SessionClass;
import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.firebase.auth.FirebaseAuth;

public class scheduleActivity extends AppCompatActivity {

    GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);
        mGoogleSignInClient = SessionClass.getSession().getGoogleSigninClient(this);
        mAuth = FirebaseAuth.getInstance();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        signOut();

    }

    private void signOut() {
        if(GoogleSignIn.getLastSignedInAccount(this)!=null)
            mGoogleSignInClient.signOut();
        if(AccessToken.getCurrentAccessToken() != null)
            LoginManager.getInstance().logOut();
        if(mAuth.getCurrentUser()!=null)
            mAuth.signOut();
        SessionClass.getSession().setUser(this,null, Constants.USER_KEY);
    }


}
