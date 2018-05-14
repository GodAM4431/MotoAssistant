package com.petyachoeva.motoassistant;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NotificationCompat;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class ProfileDrawerActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    LinearLayout linearLayout;
    ImageView imageViewDrawer;
    TextView textViewUsername;
    TextView textViewEmail;
    TextView textViewWelcome;
    String profileImageUrl;
    FirebaseDatabase mDatabase;

    FloatingActionButton buttonDistressFloat;

    FirebaseAuth mAuth;

    NotificationHelper helper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_drawer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mAuth = FirebaseAuth.getInstance();
        if(mAuth.getCurrentUser().getDisplayName() != null) {
            setTitle("Welcome, " + mAuth.getCurrentUser().getDisplayName());
        } else setTitle("Welcome");


        helper = new NotificationHelper(this);
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);

        View headerView = navigationView.getHeaderView(0);
        ImageView drawerImage = (ImageView) headerView.findViewById(R.id.imageViewDrawer);
        TextView drawerUsername = (TextView) headerView.findViewById(R.id.TextViewUsername);
        TextView drawerAccount = (TextView) headerView.findViewById(R.id.textViewEmail);
        buttonDistressFloat = (FloatingActionButton) findViewById(R.id.fab);

        buttonDistressFloat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //check for location
                if (ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getBaseContext(), "Location permission not granted", Toast.LENGTH_LONG).show();
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        requestPermissions(new String[]
                                {
                                        Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
                                        Manifest.permission.INTERNET
                                }, 10);
                    }

                    return ;
                } // for ActivityCompat#requestPermissions for more details.
                // end of location permission check

                //Date
                Date c = Calendar.getInstance().getTime();
                System.out.println("Current time => " + c);

                SimpleDateFormat df = new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss");
                String formattedDate = df.format(c);
                Timestamp timestamp = new Timestamp(System.currentTimeMillis());

                //Location
                final GPSTracker[] gps = {new GPSTracker(getBaseContext())};

                // check if GPS enabled
                if(gps[0].canGetLocation()){

                    double latitude = gps[0].getLatitude();
                    double longitude = gps[0].getLongitude();
                    LatLng currLocation = new LatLng(latitude, longitude);

                    String address = getAddress (latitude, longitude);

                    // \n is for new line
                    Toast.makeText(getBaseContext(), "print c:" + c + "\nYour Location is - \n" + address + "\nDate: " + formattedDate + "\nUser: " + mAuth.getCurrentUser().getDisplayName(), Toast.LENGTH_LONG).show();

                    writeNewEvent(mAuth.getCurrentUser().getDisplayName(),latitude, longitude, ((long) System.currentTimeMillis()));
                    Notification.Builder builder = helper.getChannelNotification("Distress event", "Successfully send.");
                    helper.getManager().notify(new Random().nextInt(), builder.build());

                }else{
                    // can't get location
                    // GPS or Network is not enabled
                    // Ask user to enable GPS/network in settings
                    gps[0].showSettingsAlert();
                }
            }
        });


        Glide.with(this /* context */)
                .load(mAuth.getCurrentUser().getPhotoUrl())
                .into(drawerImage);

        drawerImage.setImageURI(mAuth.getCurrentUser().getPhotoUrl());
        drawerUsername.setText(mAuth.getCurrentUser().getDisplayName());
        drawerAccount.setText(mAuth.getCurrentUser().getEmail());

        textViewWelcome = (TextView) findViewById(R.id.welcomeUser);
        imageViewDrawer = (ImageView) findViewById(R.id.imageViewDrawer);
        textViewUsername = (TextView) findViewById(R.id.TextViewUsername);
        linearLayout = (LinearLayout) findViewById(R.id.linearLayoutDefault);

        loadUserInformation();

        linearLayout.setVisibility(LinearLayout.VISIBLE);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
    }

    private void writeNewEvent(String sender_username, double latitude, double longitude, long timestamp) {
        DistressEvent event = new DistressEvent(sender_username, latitude, longitude, timestamp);

        FirebaseDatabase database =  FirebaseDatabase.getInstance();
        FirebaseUser user =  mAuth.getCurrentUser();
        DatabaseReference mRef =  database.getReference().child("events").child(String.valueOf("event-" + timestamp + sender_username.hashCode()));
        mRef.child("sender_username").setValue(sender_username);
        mRef.child("latitude").setValue(latitude);
        mRef.child("longitude").setValue(longitude);
        mRef.child("timestamp").setValue(timestamp);
    }

    private void loadUserInformation() {
        FirebaseUser user = mAuth.getCurrentUser();

        if(user != null){
            if(user.getPhotoUrl() != null) {
//                Glide.with(this)
//                        .load(user.getPhotoUrl().toString())
//                        .into(imageViewDrawer);

            }
            if(user.getDisplayName() != null) {
//                textViewUsername.setText(user.getDisplayName());
            }
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() == null) {
            finish();
            startActivity(new Intent(this, MainActivity.class));
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.profile_drawer, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.

        android.support.v4.app.Fragment fragment = null;

        int id = item.getItemId();

        if (id == R.id.nav_profile) {

            linearLayout.setVisibility(LinearLayout.GONE);
            fragment = new profileFragment();
        } else if (id == R.id.nav_articles) {
            fragment = new articlesFragment();
        } else if(id == R.id.nav_numbers) {
            fragment = new emergencyNumbersFragment();
        } else if (id == R.id.nav_location) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Location permission not granted", Toast.LENGTH_LONG).show();
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    requestPermissions(new String[]
                            {
                                    Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,
                                    Manifest.permission.INTERNET
                            }, 10);
                }
            } // for ActivityCompat#requestPermissions for more details.

            fragment = new locationFragment();
        } else if (id == R.id.nav_events) {
            fragment = new eventsFragment();
        } else if (id == R.id.nav_settings) {
            fragment = new settingsFragment();
        } else  if (id == R.id.action_settings) {
            fragment = new settingsFragment();
        } else if(id == R.id.nav_logout) {
            FirebaseAuth.getInstance().signOut();
            finish();
            startActivity(new Intent(this, MainActivity.class));
        }

        if (fragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction ft = fragmentManager.beginTransaction();

            ft.replace(R.id.screen_area, fragment);

            ft.commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public String getAddress (double latitude, double longitude) {
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
