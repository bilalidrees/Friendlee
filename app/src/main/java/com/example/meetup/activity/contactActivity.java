package com.example.meetup.activity;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewPropertyAnimatorListenerAdapter;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SearchView;

import com.example.meetup.R;
import com.example.meetup.adapter.ContactAdapter;
import com.example.meetup.databinding.ActivityContactBinding;
import com.example.meetup.databinding.ContactItemBinding;
import com.example.meetup.login.Check;
import com.example.meetup.model.Contact;
import com.example.meetup.model.Meet;
import com.example.meetup.utility.Constants;
import com.example.meetup.utility.SessionClass;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

public class contactActivity extends AppCompatActivity implements View.OnClickListener, Check {

    ActivityContactBinding activityContactBinding;
    ArrayList<Contact> mcontactlist, markedContactList = new ArrayList<>();
    ArrayList<Meet> matchedMeetlist = new ArrayList<>();
    public ArrayList<String> listofdates = new ArrayList<>();
    ContactAdapter contactAdapter;
    AlertDialog alertDialog;
    private FirebaseAuth mAuth;
    private FirebaseDatabase mFirebaseDatabase;
    private DatabaseReference mCurrentDatabaseReference, mSelectedDatabaseReference;
    private ChildEventListener mChildEventListener;
    int count = 1, userpos;
    Check check;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        activityContactBinding = DataBindingUtil.setContentView(this, R.layout.activity_contact);
        alertDialog = new AlertDialog.Builder(this).create();
        mAuth = FirebaseAuth.getInstance();
        check = this;
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mCurrentDatabaseReference = mFirebaseDatabase.getReference().child("Meet").child(SessionClass.getSession().getNumber(this, Constants.NUMBER_KEY));
        activityContactBinding.recyclerview.setLayoutManager(new LinearLayoutManager(this));
        activityContactBinding.go.setOnClickListener(this);
        contactAdapter = new ContactAdapter(this, getContact(), new ContactAdapter.Click() {
            @Override
            public void onclick(Contact contact, ContactItemBinding binding, int position) {
                setArrayList(contact, binding, position);
            }
        });

        activityContactBinding.recyclerview.setAdapter(contactAdapter);
        // perform set on query text listener event
        activityContactBinding.simpleSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
// do something on text submit
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
// do something when text changes
                contactAdapter.getFilter().filter(newText);
                return false;
            }
        });
        activityContactBinding.rg.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            public void onCheckedChanged(RadioGroup group, int checkedId) {

                switch (checkedId) {
                    case R.id.bestfr:
                        setusertype(1);
                        break;
                    case R.id.good:
                        setusertype(2);
                        break;
                    case R.id.fr:
                        setusertype(3);
                        break;
                    case R.id.Acquaintances:
                        setusertype(4);
                        break;
                    case R.id.pubblic:
                        setusertype(5);
                        break;


                }
            }
        });

    }

    private void setusertype(int i) {
        mcontactlist.get(userpos).setType(i);
        activityContactBinding.rg.setVisibility(View.GONE);
    }

    private void setArrayList(Contact contact, ContactItemBinding binding, int position) {
        userpos = position;
        if (!mcontactlist.get(position).isSelected()) {
            activityContactBinding.rg.setVisibility(View.VISIBLE);
            binding.rad.setChecked(true);
            mcontactlist.get(position).setSelected(true);
            markedContactList.add(mcontactlist.get(position));
            binding.rad.setSelected(true);
        } else {
            binding.rad.setChecked(false);
            mcontactlist.get(position).setSelected(false);
            markedContactList.remove(mcontactlist.get(position));
            binding.rad.setSelected(false);
        }

    }

    private ArrayList<Contact> getContact() {
        mcontactlist = new ArrayList<>();
        Cursor cursor = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, ContactsContract.Contacts.DISPLAY_NAME + " ASC");
        cursor.moveToFirst();
        while (cursor.moveToNext()) {
            mcontactlist.add(new Contact(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)), cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))));
        }
        return mcontactlist;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.go:
                startScanning();
                break;


        }
    }

    private void startScanning() {
        sow();
        for (Contact contact : markedContactList) {
            mSelectedDatabaseReference = mFirebaseDatabase.getReference().child("Meet").child(contact.getNumber());
            mSelectedDatabaseReference.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        innerCheck(snapshot);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                hide();
            }
        }, 7000);
    }

    private void hide() {
        alertDialog.show();
        Intent intent = new Intent(this, calenderActivity.class);
        intent.putExtra("matchedList", matchedMeetlist);
        intent.putExtra("dateList", listofdates);
        startActivity(intent);
    }

    private void innerCheck(final DataSnapshot selected) {
        mCurrentDatabaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    if (snapshot.getKey().toString().equals(selected.getKey().toString())) {
                        Meet meet = selected.getValue(Meet.class);
                        matchedMeetlist.add(meet);
                        listofdates.add(meet.getDate());
                    }
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });

    }

    private void sow() {
        alertDialog.setTitle("Searching");
        alertDialog.setMessage("Searching for Similar Schedules");
        alertDialog.show();
    }


    @Override
    public void check() {
        if (count == markedContactList.size()) {
            hide();
        } else {
            count++;
        }
    }
}
