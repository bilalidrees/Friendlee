package com.example.meetup.login;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Toast;

import com.example.meetup.R;
import com.example.meetup.model.User;
import com.example.meetup.activity.scheduleActivity;
import com.example.meetup.utility.Constants;
import com.example.meetup.utility.SessionClass;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;

import com.example.meetup.databinding.ActivityMainBinding;
import com.example.meetup.model.OnClickHandlerInterface;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int RC_SIGN_IN = 0;
    private static final String TAG = "google";
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mUserDatabaseReference;
    private ChildEventListener mChildEventListener;

    ActivityMainBinding activityMainBinding;

    GoogleSignInClient mGoogleSignInClient;
    CallbackManager callbackManager;
    OnClickHandlerInterface onClickHandlerInterface;
    String mVerificationId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityMainBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        initialize();
    }


    private void initialize() {
        mAuth = FirebaseAuth.getInstance();
        //activityMainBinding.setClickHandler((OnClickHandlerInterface) this);
        activityMainBinding.loginButton.setOnClickListener(this);
        activityMainBinding.signInButton.setOnClickListener(this);
        activityMainBinding.submit.setOnClickListener(this);
        activityMainBinding.signu.setOnClickListener(this);
        activityMainBinding.send.setOnClickListener(this);
        mGoogleSignInClient = SessionClass.getSession().getGoogleSigninClient(this);
        callbackManager = CallbackManager.Factory.create();
    }


    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }


    private void navigateToSchedule(User user,String status) {
        SessionClass.getSession().setUser(this, user, Constants.USER_KEY);
        Intent intent=new Intent(this, scheduleActivity.class);
        intent.putExtra("status",status);
        startActivity(intent);
        finish();
    }

    private boolean validation() {
        if (!activityMainBinding.usernameedit.getText().toString().isEmpty()) {
            if (!activityMainBinding.passwordedit.getText().toString().isEmpty()) {
                return true;
            } else
                return false;
        } else
            return false;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // Result returned from launching the Intent from GoogleSignInClient.getSignInIntent(...);
        if (requestCode == RC_SIGN_IN) {
            //The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        } else {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }

    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            // Signed in successfully, show authenticated UI.
            //updateUI(account);
            navigateToSchedule(new User(account.getDisplayName(), account.getEmail()),"true");
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w(TAG, "signInResult:failed code=" + e.getStatusCode());
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (GoogleSignIn.getLastSignedInAccount(this) != null || AccessToken.getCurrentAccessToken() != null || mAuth.getCurrentUser()!=null)
            navigateToSchedule(SessionClass.getSession().getUser(this, Constants.USER_KEY),"");
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.submit:
                    String code=activityMainBinding.passwordedit.getText().toString().trim();
                    if(code.isEmpty() || code.length()<6){
                        activityMainBinding.passwordedit.setText("Invalid");
                        activityMainBinding.passwordedit.requestFocus();
                        return;
                    }
                    verifyCode(code);
                break;
            case R.id.sign_in_button:
                signIn();
                break;
            case R.id.login_button:
                FB_Signin();
                break;
            case R.id.signu:
                navigateToSignu();
                break;
            case R.id.send:
                sendVerificationCode();
                break;

        }
    }

    private void verifyCode(String code) {
        try {
            PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
            signInWithPhoneAuthCredential(credential);
        }catch (Exception e){
            Toast toast = Toast.makeText(getApplicationContext(), "Verification Code is wrong, try again", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            SessionClass.getSession().setPhoneNumer(MainActivity.this,activityMainBinding.usernameedit.getText().toString(),Constants.NUMBER_KEY);
                            AuthResult authResult = task.getResult();
                            navigateToSchedule(new User(authResult.getUser().getDisplayName(), authResult.getUser().getEmail()),"false");
                            // ...
                        } else {
                            // Sign in failed, display a message and update the UI

                            if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                // The verification code entered was invalid
                            }
                        }
                    }
                });
    }

    private void sendVerificationCode() {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                activityMainBinding.usernameedit.getText().toString(),        // Phone number to verify
                60,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                this,               // Activity (for callback binding)
                mCallbacks);
        Toast.makeText(this,"Verification code has been sent",Toast.LENGTH_SHORT).show();
    }

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks mCallbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

        @Override
        public void onVerificationCompleted(PhoneAuthCredential credential) {
            String code=credential.getSmsCode();
            if(code!=null){
                activityMainBinding.passwordedit.setText(code);
            }
        }

        @Override
        public void onVerificationFailed(FirebaseException e) {
            // This callback is invoked in an invalid request for verification is made,
            // for instance if the the phone number format is not valid.

            if (e instanceof FirebaseAuthInvalidCredentialsException) {

                Toast.makeText(getApplicationContext(),"Invalid Number",Toast.LENGTH_LONG).show();
                // Invalid request
                // ...
            } else if (e instanceof FirebaseTooManyRequestsException) {
                Toast.makeText(getApplicationContext(),"The SMS quota for the project has been exceeded",Toast.LENGTH_LONG).show();

            }

            // Show a message and update the UI
            // ...
        }

        @Override
        public void onCodeSent(String verificationId,
                               PhoneAuthProvider.ForceResendingToken token) {
            mVerificationId = verificationId;
        }
    };

    private void navigateToSignu() {
    startActivity(new Intent(this,SignupActivity.class));
    finish();
    }

    private void FB_Signin() {
        activityMainBinding.loginButton.setReadPermissions(Arrays.asList("email", "public_profile"));
        activityMainBinding.loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                AuthCredential authCredential = FacebookAuthProvider.getCredential(loginResult.getAccessToken().getToken());
                mAuth.signInWithCredential(authCredential)
                        .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    AuthResult authResult = task.getResult();
                                    navigateToSchedule(new User(authResult.getUser().getDisplayName(), authResult.getUser().getEmail()),"true");
                                }
                            }

                        });

            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException error) {
            }
        });
    }
}
