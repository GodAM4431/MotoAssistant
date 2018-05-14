package com.petyachoeva.motoassistant;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import static com.google.android.gms.internal.zzs.TAG;

public class eventsFragment extends Fragment {

    ListView listViewEvents;
    FirebaseDatabase database;
    DatabaseReference ref;
    ArrayList<DistressEvent> dataForTheAdapter = new ArrayList<DistressEvent>();
    ArrayList<String> listEvents;
    ArrayAdapter<String> adapter;
    DistressEvent event;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_events, null);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.getActivity().setTitle("Latest distress events");

        event = new DistressEvent();
        listViewEvents = (ListView) view.findViewById(R.id.ListViewEvents);
        database = FirebaseDatabase.getInstance();
        ref = database.getReference("events");
        listEvents = new ArrayList<String>();

        adapter = new ArrayAdapter<String>(getContext(),android.R.layout.simple_list_item_1, listEvents);

        double currTime = System.currentTimeMillis();
        ref.limitToLast(20).endAt(currTime).addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                listEvents.clear();
                listViewEvents.setAdapter(null);

                dataForTheAdapter.clear();
                for(DataSnapshot ds: dataSnapshot.getChildren()) {
                    event = ds.getValue(DistressEvent.class);

                    if(event.getTimestamp() == 0) {
                        break;
                    }

                    dataForTheAdapter.add(event);
                    String date = getDate(event.getTimestamp());

                    listEvents.add(event.getSender_username() + "\n" + getAddress(event.getLatitude(), event.getLongitude()) + "\n" + date);
                }
                ArrayList<String> tempEvents = new ArrayList<String>(listEvents);
                for(int i=0;i<listEvents.size();i++) {
                    listEvents.set(i, tempEvents.get(listEvents.size() - i - 1));
                }
                listViewEvents.setAdapter(adapter);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w(TAG, "Failed to read value.", databaseError.toException());
            }
        });

        listViewEvents.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                        public void onItemClick(AdapterView<?> parent, View v, int position, long id) {

                locationFragment nextFrag= new locationFragment();
                Bundle bundle = new Bundle();
                bundle.putDouble("location_latitude", dataForTheAdapter.get(dataForTheAdapter.size()-position-1).getLatitude());
                bundle.putDouble("location_longitude", dataForTheAdapter.get(dataForTheAdapter.size()-position - 1).getLongitude());
                nextFrag.setArguments(bundle);
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.screen_area, nextFrag,"findThisFragment")
                        .addToBackStack(null)
                        .commit();


            }
        });
    }
    public String getAddress(double latitude, double longitude) {
        Geocoder geocoder;
        List<Address> addresses = null;

        geocoder = new Geocoder(getContext(), Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
        } catch (IOException ioException) {
            // Catch network or other I/O problems.
            String errorMessage = "Service not Available";
            Log.d(TAG, errorMessage, ioException);
        } catch (IllegalArgumentException illegalArgumentException) {
            // Catch invalid latitude or longitude values.
            String errorMessage = "Invalid Coords";
            Log.d(TAG, errorMessage + ". " + "Latitude = " + latitude + ", Longitude = " + longitude, illegalArgumentException);
        }
        String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
        String city = addresses.get(0).getLocality();
        String state = addresses.get(0).getAdminArea();
        String country = addresses.get(0).getCountryName();
        String postalCode = addresses.get(0).getPostalCode();
        String knownName = addresses.get(0).getFeatureName(); // Only if available else return NULL

        String addr = address + "\n" + city + " " + state + "\n" + country + " " + postalCode + "\n" + knownName;
        return addr;
    }

    private String getDate(long time) {
        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(time);
        String date = DateFormat.format("dd-MM-yyyy HH:mm:ss", cal).toString();
        return date;
    }
}
