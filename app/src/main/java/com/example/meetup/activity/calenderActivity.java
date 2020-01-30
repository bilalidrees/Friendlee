package com.example.meetup.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.style.LineBackgroundSpan;
import android.view.View;
import android.widget.TimePicker;

import com.example.meetup.R;
import com.example.meetup.databinding.ActivityCalenderBinding;
import com.example.meetup.databinding.ActivityScheduleBinding;
import com.example.meetup.model.Meet;
import com.example.meetup.utility.Constants;
import com.example.meetup.utility.SessionClass;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.prolificinteractive.materialcalendarview.CalendarDay;
import com.prolificinteractive.materialcalendarview.DayViewDecorator;
import com.prolificinteractive.materialcalendarview.DayViewFacade;
import com.prolificinteractive.materialcalendarview.MaterialCalendarView;
import com.prolificinteractive.materialcalendarview.OnDateSelectedListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class calenderActivity extends AppCompatActivity implements View.OnClickListener {
    ArrayList<Meet> matchedMeetlist;
    ActivityCalenderBinding activityCalenderBinding;
    public ArrayList<String> listofdates;
    AlertDialog alertDialog;
    private FirebaseAuth mAuth;
    ActivityScheduleBinding activityScheduleBinding;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference meetDatabaseReference;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityCalenderBinding = DataBindingUtil.setContentView(this, R.layout.activity_calender);
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        meetDatabaseReference = mFirebaseDatabase.getReference().child("Meet");
        alertDialog = new AlertDialog.Builder(this).create();
        matchedMeetlist= (ArrayList<Meet>) getIntent().getExtras().getSerializable("matchedList");
        listofdates=getIntent().getExtras().getStringArrayList("dateList");
        activityCalenderBinding.home.setOnClickListener(this);
        //add events in calender with this method
        addEvent();
        activityCalenderBinding.calenderview.setOnDateChangedListener(new OnDateSelectedListener() {
            @Override
            public void onDateSelected(@NonNull MaterialCalendarView materialCalendarView, @NonNull final CalendarDay calenderday, boolean b) {
                final int[] day = {calenderday.getDay()};
                final int monthForCalender = calenderday.getMonth() - 1;
                int month = calenderday.getMonth();
                final int year = calenderday.getYear();
                Calendar myclander = Calendar.getInstance();
                myclander.set(Calendar.YEAR, year);
                myclander.set(Calendar.MONTH, monthForCalender);
                myclander.set(Calendar.DAY_OF_MONTH, day[0]);
                SimpleDateFormat sdfDate = new SimpleDateFormat("dd-MM-yyyy");
                String date = sdfDate.format(myclander.getTime());
                for(final Meet matchedMeet:matchedMeetlist){
                    if(matchedMeet.getDate().equals(date)){
                        String name=getContactName(matchedMeet.getNumber(),calenderActivity.this);
                        alertDialog.setTitle("Matched");
                        alertDialog.setMessage("Your Meeting has been Set with "+name);
                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "OK",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.dismiss();
                                    }
                                });
                        alertDialog.setButton(AlertDialog.BUTTON_NEUTRAL, "Delay",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        meetDatabaseReference.child(matchedMeet.getNumber()).child(matchedMeet.getDate()).setValue(null);
                                        day[0] = day[0] +7;
                                        Calendar myclander = Calendar.getInstance();
                                        myclander.set(Calendar.YEAR, year);
                                        myclander.set(Calendar.MONTH, monthForCalender);
                                        myclander.set(Calendar.DAY_OF_MONTH, day[0]);
                                        SimpleDateFormat sdfDate = new SimpleDateFormat("dd-MM-yyyy");
                                        String Date = sdfDate.format(myclander.getTime());
                                        matchedMeet.setDate(Date);
                                        meetDatabaseReference.child(matchedMeet.getNumber()).child(matchedMeet.getDate()).setValue(matchedMeet);
                                        dialog.dismiss();
                                    }
                                });

                        alertDialog.show();
                    }
                }
            }
        });
    }


    public String getContactName(final String phoneNumber, Context context)
    {
        Uri uri=Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,Uri.encode(phoneNumber));

        String[] projection = new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME};

        String contactName="";
        Cursor cursor=context.getContentResolver().query(uri,projection,null,null,null);

        if (cursor != null) {
            if(cursor.moveToFirst()) {
                contactName=cursor.getString(0);
            }
            cursor.close();
        }

        return contactName;
    }
    private void addEvent() {
        //start the working
        DayViewDecorator d = new DayViewDecorator() {
            @Override
            //this will decide wether date shoudl highlight  or not based on datelist
            public boolean shouldDecorate(CalendarDay calendarDay) {
                int day = calendarDay.getDay();
                int monthForCalender = calendarDay.getMonth() - 1;
                int year = calendarDay.getYear();
                Calendar myclander = Calendar.getInstance();
                myclander.set(Calendar.YEAR, year);
                myclander.set(Calendar.MONTH, monthForCalender);
                myclander.set(Calendar.DAY_OF_MONTH, day);
                SimpleDateFormat sdfDate = new SimpleDateFormat("dd-MM-yyyy");
                String Date = sdfDate.format(myclander.getTime());
                if(listofdates.contains(Date)){
                    return true;
                }
                else{
                    return false;
                }
            }
            @Override
            public void decorate(DayViewFacade dayViewFacade) {
                //this method will draw a circle on date
                dayViewFacade.addSpan(new LineBackgroundSpan() {
                    @Override
                    public void drawBackground(Canvas c, Paint p, int left, int right, int top, int baseline, int bottom, CharSequence text, int start, int end, int lnum) {
                        p.setColor(Color.RED);
                        c.drawCircle((float) ((left + right) / 2), (float) bottom + 13, 5, p);
                    }
                });
            }
        };
        //add event to specific date
        activityCalenderBinding.calenderview.addDecorator(d);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.home:
                startActivity(new Intent(this, scheduleActivity.class));
                finish();
                break;


        }
    }
}
