package com.petyachoeva.motoassistant;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.text.format.DateFormat;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class MyService extends JobService {

    BackgroundTask backgroundTask;
    NotificationHelper helper;

    DatabaseHelper myDb;

    ArrayList<String> listEventsNames;

    ArrayAdapter<String> adapter;

    @Override
    public boolean onStartJob(final JobParameters job) {
        myDb = new DatabaseHelper(this);
        backgroundTask = new BackgroundTask() {
            @Override
            protected void onPostExecute(DistressEvent s) {
                double lastTime = System.currentTimeMillis()-10000;
                final FirebaseDatabase database;
                final FirebaseAuth mAuth = null;
                database = FirebaseDatabase.getInstance();
                final DatabaseReference ref = database.getReference("events");
                ref.limitToLast(1).orderByChild("timestamp").addChildEventListener(new ChildEventListener() {
                    @Override
                    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                        final DistressEvent lastEvent = dataSnapshot.getValue(DistressEvent.class);

                        myDb = new DatabaseHelper(getBaseContext());
                        Cursor res = myDb.getAllData();
                        String[] columnNames = res.getColumnNames();

                        if(res.getCount() > 0) {
                            while (res.moveToNext()) {
                                if (!res.getString(1).equals(lastEvent.getSender_username()) || !res.getString(2).equals(Long.toString(lastEvent.getTimestamp()))) {
                                    String date = getDate(lastEvent.getTimestamp());

                                    Intent resultIntent = new Intent(getBaseContext(), locationFragment.class);
                                    PendingIntent resultPendingIntent = PendingIntent.getActivity(getBaseContext(), 1, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                                    helper = new NotificationHelper(getBaseContext());

                                    Notification.Builder builder = helper.getChannelNotification(lastEvent.getSender_username() + " | " + date, getAddress(lastEvent.getLatitude(), lastEvent.getLongitude()));
                                    builder.setContentIntent(resultPendingIntent);
                                    helper.getManager().notify(new Random().nextInt(), builder.build());


                                    boolean isUpdated = myDb.updateData(lastEvent.getSender_username(), Long.toString(lastEvent.getTimestamp()));

                                }
                                return;
                            }
                        } else {
                            String date = getDate(lastEvent.getTimestamp());

                            Intent resultIntent = new Intent(getBaseContext(), locationFragment.class);
                            PendingIntent resultPendingIntent = PendingIntent.getActivity(getBaseContext(),1, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                            helper = new NotificationHelper(getBaseContext());

                            Notification.Builder builder = helper.getChannelNotification(lastEvent.getSender_username() + " | " + date, getAddress(lastEvent.getLatitude(), lastEvent.getLongitude()));
                            builder.setContentIntent(resultPendingIntent);
                            helper.getManager().notify(new Random().nextInt(), builder.build());

                            myDb = new DatabaseHelper(getBaseContext());
                            boolean isInserted = myDb.insertData(lastEvent.getSender_username(),
                                    (Long.toString(lastEvent.getTimestamp())));
                        }

                    }

                    @Override
                    public void onChildChanged(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onChildRemoved(DataSnapshot dataSnapshot) {

                    }

                    @Override
                    public void onChildMoved(DataSnapshot dataSnapshot, String s) {

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                Toast.makeText(getApplicationContext(), "Message from background" , Toast.LENGTH_SHORT).show();
                jobFinished(job, false);
            }
        };

        backgroundTask.execute();


        return true;
    }

    @Override
    public boolean onStopJob(JobParameters job) {
        return true;
    }

    public static class BackgroundTask extends AsyncTask<Void, Void, DistressEvent> {
        @Override
        protected DistressEvent doInBackground(Void... voids) {

            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
    }
    protected String getDate(long time) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(time);
        String date = DateFormat.format("dd-MM-yyyy HH:mm:ss", cal).toString();
        return date;
    }

    public String getAddress(double latitude, double longitude) {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();
            String postalCode = addresses.get(0).getPostalCode();
            String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL

            String addr = address + "\n" + city + " " + state + "\n" + country + " " + postalCode + "\n" + knownName;
            return addr;
        } catch (IOException e) {

            e.printStackTrace();
            return "Error loading address!";

        }
    }
}
