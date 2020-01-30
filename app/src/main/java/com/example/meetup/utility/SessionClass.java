package com.example.meetup.utility;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.Nullable;

import com.example.meetup.model.User;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.gson.Gson;

public class SessionClass {
     static SessionClass sessionClass;
     GoogleSignInOptions gso;

    public static SessionClass getSession(){
        if(sessionClass==null)
            sessionClass=new SessionClass();
        return sessionClass;
    }

    public void setPhoneNumer(Context context, String number, String key){
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor prefsEditor = mPrefs.edit();
        prefsEditor.putString(key, number);
        prefsEditor.apply();
    }
    public String getNumber(Context context, String key){
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        return  mPrefs.getString(key, "");

    }

    public void setUser(Context context, User user, String key){
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor prefsEditor = mPrefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(user);
        prefsEditor.putString(key, json);
        prefsEditor.apply();
    }

    public User getUser(Context context, String key){
        SharedPreferences mPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        Gson gson = new Gson();
        String json = mPrefs.getString(key, "");
        return gson.fromJson(json, User.class);
    }

    public GoogleSignInClient getGoogleSigninClient( Context context){
        if(gso==null){
            gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestEmail()
                    .build();
        }
        return GoogleSignIn.getClient(context, gso);

    }


}
