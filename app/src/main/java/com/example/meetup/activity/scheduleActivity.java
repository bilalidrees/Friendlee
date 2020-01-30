package com.example.meetup.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import android.Manifest;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.meetup.R;
import com.example.meetup.databinding.ActivityScheduleBinding;
import com.example.meetup.login.MainActivity;
import com.example.meetup.model.Meet;
import com.example.meetup.model.User;
import com.example.meetup.utility.Constants;
import com.example.meetup.utility.SessionClass;
import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class scheduleActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_WRITE_PERMISSION = 1;
    GoogleSignInClient mGoogleSignInClient;
    private FirebaseAuth mAuth;
    ActivityScheduleBinding activityScheduleBinding;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference meetDatabaseReference;
    private ChildEventListener mChildEventListener;
    private ArrayList<Integer> selectedDays;
    User user;
    String number,status;
    int selectedDay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        activityScheduleBinding = DataBindingUtil.setContentView(this, R.layout.activity_schedule);
        initialize();
    }

    private void initialize() {
        mGoogleSignInClient = SessionClass.getSession().getGoogleSigninClient(this);
        mAuth = FirebaseAuth.getInstance();
        selectedDays = new ArrayList<>();
        status=getIntent().getExtras().getString("status");
        if(status.equals("true"))
            activityScheduleBinding.rel.setVisibility(View.VISIBLE);
            else
            activityScheduleBinding.rel.setVisibility(View.GONE);
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        number=SessionClass.getSession().getNumber(this,Constants.NUMBER_KEY);
        user = SessionClass.getSession().getUser(this, Constants.USER_KEY);
        meetDatabaseReference = mFirebaseDatabase.getReference().child("Meet");
        activityScheduleBinding.stlayout.setOnClickListener(this);
        activityScheduleBinding.etlayout.setOnClickListener(this);
        activityScheduleBinding.ver.setOnClickListener(this);
        activityScheduleBinding.go.setOnClickListener(this);
        activityScheduleBinding.mon.setOnClickListener(this);
        activityScheduleBinding.tue.setOnClickListener(this);
        activityScheduleBinding.wed.setOnClickListener(this);
        activityScheduleBinding.thu.setOnClickListener(this);
        activityScheduleBinding.fri.setOnClickListener(this);
        activityScheduleBinding.sat.setOnClickListener(this);
        activityScheduleBinding.sun.setOnClickListener(this);


    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        signOut();

    }

    private void signOut() {
        if (GoogleSignIn.getLastSignedInAccount(this) != null)
            mGoogleSignInClient.signOut();
        if (AccessToken.getCurrentAccessToken() != null)
            LoginManager.getInstance().logOut();
        if (mAuth.getCurrentUser() != null)
            mAuth.signOut();
        SessionClass.getSession().setUser(this, null, Constants.USER_KEY);
    }


    @Override
    public void onClick(final View view) {

        switch (view.getId()) {
            case R.id.stlayout:
            case R.id.etlayout:
                Calendar mcurrentsTime = Calendar.getInstance();
                int hour = mcurrentsTime.get(Calendar.HOUR_OF_DAY);
                int minute = mcurrentsTime.get(Calendar.MINUTE);
                TimePickerDialog mTimePicker;
                mTimePicker = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int selectedHour, int selectedMinute) {
                        if (view.getId() == R.id.stlayout)
                            activityScheduleBinding.st.setText(String.format("%02d:%02d", selectedHour, selectedMinute));
                        else
                            activityScheduleBinding.et.setText(String.format("%02d:%02d", selectedHour, selectedMinute));
                    }
                }, hour, minute, true);//Yes 24 hour time
                mTimePicker.setTitle("Select Time");
                mTimePicker.show();
                break;

            case R.id.ver:
                String code=activityScheduleBinding.numbered.getText().toString().trim();
                SessionClass.getSession().setPhoneNumer(scheduleActivity.this,code,Constants.NUMBER_KEY);
                activityScheduleBinding.rel.setVisibility(View.GONE);
                break;
            case R.id.go:
                submit();
                break;

            case R.id.mon:
                setDay(view.getId(), Calendar.MONDAY);
                break;
            case R.id.tue:
                setDay(view.getId(), Calendar.TUESDAY);
                break;
            case R.id.wed:
                setDay(view.getId(), Calendar.WEDNESDAY);
                break;
            case R.id.thu:
                setDay(view.getId(), Calendar.THURSDAY);
                break;
            case R.id.fri:
                setDay(view.getId(), Calendar.FRIDAY);
                break;
            case R.id.sat:
                setDay(view.getId(), Calendar.SATURDAY);
                break;
            case R.id.sun:
                setDay(view.getId(), Calendar.SUNDAY);
                break;


        }
    }

    private void setDay(int id, int day) {
        RadioButton radioButton= findViewById(id);
        boolean selected = radioButton.isSelected();
        if (selected) {
            radioButton.setChecked(false);
            radioButton.setSelected(false);
            if(selectedDays.contains(day))
                selectedDays.remove(Integer.valueOf(2));
        } else {
            radioButton.setChecked(true);
            radioButton.setSelected(true);
            if(!selectedDays.contains(day))
                selectedDays.add(day);
        }

    }

    private void submit() {
        Calendar c = Calendar.getInstance();
        int daysInMonth = c.getActualMaximum(Calendar.DAY_OF_MONTH); // 28
        for(int selectedDay:selectedDays) {
            for (int d = 1; d <= daysInMonth; d++) {
                c.set(Calendar.DAY_OF_MONTH, d);
                int dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
                if (selectedDay == dayOfWeek) {
                    Log.d("time", String.valueOf(c.getTimeInMillis()));
                    Date date = new Date(c.getTimeInMillis());
                    SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy"); // the format of your date
                    Meet meet = new Meet(c.getTimeInMillis(),number,sdf.format(date),activityScheduleBinding.st.getText().toString(), activityScheduleBinding.et.getText().toString(), user);
                    meetDatabaseReference.child(SessionClass.getSession().getNumber(this,Constants.NUMBER_KEY)).child(sdf.format(date)).setValue(meet);
                }
            }
        }
        Toast.makeText(this, "Your scheudle has been set", Toast.LENGTH_SHORT).show();
        navigateToContactActivity();
    }




    private void navigateToContactActivity() {
        if(requestPermission())
        startActivity(new Intent(this, contactActivity.class));
    }

    private boolean requestPermission() {
            if ((ContextCompat.checkSelfPermission(this,Manifest.permission.READ_CONTACTS)
                    == PackageManager.PERMISSION_GRANTED)&& (ContextCompat.checkSelfPermission(this,Manifest.permission.WRITE_CONTACTS)
                    == PackageManager.PERMISSION_GRANTED)) {
                return true;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_CONTACTS,Manifest.permission.WRITE_CONTACTS}, 3);
                return false;
            }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED  && grantResults[1]==PackageManager.PERMISSION_GRANTED){
           startActivity(new Intent(this, contactActivity.class));
       }

    }

}
