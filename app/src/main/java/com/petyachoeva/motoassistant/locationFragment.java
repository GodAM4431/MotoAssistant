package com.petyachoeva.motoassistant;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Camera;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class locationFragment extends Fragment implements OnMapReadyCallback {

    GoogleMap mGoogleMap;
    MapView mMapView;
    View mView;


    public locationFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_location, container,false);
        return mView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.getActivity().setTitle("My location");

        mMapView = (MapView) mView.findViewById(R.id.map);
        if(mMapView != null) {
            mMapView.onCreate(null);
            mMapView.onResume();
            mMapView.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        MapsInitializer.initialize(getContext());
        double distress_latitude;
        double distress_longitude;
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            this.getActivity().setTitle("Distress location");

            distress_latitude = bundle.getDouble("location_latitude");
            distress_longitude = bundle.getDouble("location_longitude");
            LatLng currLocation = new LatLng(distress_latitude, distress_longitude);

            googleMap.addMarker(new MarkerOptions().position(currLocation).title("Mt location").snippet("It`s like riding a bike :)."));

            CameraPosition myLocation = CameraPosition.builder().target(currLocation).zoom(16).bearing(0).tilt(45).build();

            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(myLocation));
            Log.d("Log bundle", String.valueOf(distress_latitude) + " " + String.valueOf(distress_longitude));
            Log.d("Log bundle address", getAddress(distress_latitude, distress_longitude));
        } else {


            mGoogleMap = googleMap;
            googleMap.setMapType(googleMap.MAP_TYPE_NORMAL);

            if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(getContext(), "Location permission not granted", Toast.LENGTH_LONG).show();
                return;
            }
            mGoogleMap.setMyLocationEnabled(true);


            final GPSTracker[] gps = {new GPSTracker(getContext())};

            // check if GPS enabled
            if (gps[0].canGetLocation()) {

                double latitude = gps[0].getLatitude();
                double longitude = gps[0].getLongitude();
                LatLng currLocation = new LatLng(latitude, longitude);

                googleMap.addMarker(new MarkerOptions().position(currLocation).title("Mt location").snippet("It`s like riding a bike :)."));

                CameraPosition myLocation = CameraPosition.builder().target(currLocation).zoom(16).bearing(0).tilt(45).build();

                googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(myLocation));


                // \n is for new line
                Toast.makeText(getContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
            } else {
                // can't get location
                // GPS or Network is not enabled
                // Ask user to enable GPS/network in settings
                gps[0].showSettingsAlert();
            }
        }
    }
    public String getAddress(double latitude, double longitude) {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(getContext(), Locale.getDefault());

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
