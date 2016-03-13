package com.kewlakshat95.maraudersmap;

import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.widget.TextView;

import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MapsActivity extends FragmentActivity implements LocationListener {
    GoogleMap googleMap;
    Firebase ref, usersRef, myUserRef, locationsRef, myLocationsRef, myFriendsRef;
    ArrayList<Long> myFriends;
    ArrayList<Marker> myFriendsMarkers;
    ArrayList<HashMap<String, Double>> allUserLocations;
    Marker myMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Firebase.setAndroidContext(this);
        ref = new Firebase("https://marauders-map-pes.firebaseio.com");
        usersRef = ref.child("users");
        myUserRef = usersRef.child("0");
        locationsRef = ref.child("locations");
        myLocationsRef = locationsRef.child("0");
        myFriendsRef = myUserRef.child("friends");

        myFriendsMarkers = new ArrayList<>();

        //show error dialog if GooglePlayServices not available
        if (!isGooglePlayServicesAvailable()) {
            finish();
        }
        setContentView(R.layout.activity_maps);
        SupportMapFragment supportMapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        googleMap = supportMapFragment.getMap();
        googleMap.setMyLocationEnabled(true);
//        googleMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String bestProvider = locationManager.getBestProvider(criteria, true);
        Location location = locationManager.getLastKnownLocation(bestProvider);
        if (location != null) {
            googleMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(location.getLatitude(), location.getLongitude()))
//                    .tilt(45)
                    .zoom(19)
                    .build();
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            myLocationsRef.child("lat").setValue(new Float(location.getLatitude()));
            myLocationsRef.child("lng").setValue(new Float(location.getLongitude()));
            onLocationChanged(location);
        }
        locationManager.requestLocationUpdates(bestProvider, 1000, 0, this);

        myFriendsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                myFriends = (ArrayList<Long>) dataSnapshot.getValue();
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });

        locationsRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                allUserLocations = (ArrayList<HashMap<String, Double>>) dataSnapshot.getValue();
                if (myFriends != null) {
                    for (int i = 0; i < myFriends.size(); i++) {
                        double lat = allUserLocations.get(myFriends.get(i).intValue()).get("lat");
                        double lng = allUserLocations.get(myFriends.get(i).intValue()).get("lng");
                        LatLng latLng = new LatLng(lat, lng);
                        if (myFriendsMarkers != null && myFriendsMarkers.size() > i && myFriendsMarkers.get(i) != null) {
                            myFriendsMarkers.get(i).remove();
                        }
                        myFriendsMarkers.add(i, googleMap.addMarker(new MarkerOptions()
                                        .position(latLng)
                                        .title("Friend " + i)
                                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_cast_light))
                        ));
                        myFriendsMarkers.get(i).showInfoWindow();
                    }
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    @Override
    public void onLocationChanged(Location location) {
        TextView locationTv = (TextView) findViewById(R.id.latlongLocation);
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        double altitude = location.getAltitude();
        double accuracy = location.getAccuracy();
        LatLng latLng = new LatLng(latitude, longitude);
        if (myMarker != null) {
            myMarker.remove();
        }
        myMarker = googleMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title("You")
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_play_light))
        );
        myMarker.showInfoWindow();
        locationTv.setText("Latitude:" + latitude + ", Longitude:" + longitude);
        myLocationsRef.child("lat").setValue(new Float(latitude));
        myLocationsRef.child("lng").setValue(new Float(longitude));
    }

    @Override
    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub
    }

    private boolean isGooglePlayServicesAvailable() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == status) {
            return true;
        } else {
            GooglePlayServicesUtil.getErrorDialog(status, this, 0).show();
            return false;
        }
    }
}
