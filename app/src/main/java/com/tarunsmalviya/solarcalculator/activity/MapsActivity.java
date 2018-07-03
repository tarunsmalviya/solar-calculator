package com.tarunsmalviya.solarcalculator.activity;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.tarunsmalviya.solarcalculator.R;
import com.tarunsmalviya.solarcalculator.util.CommonMethods;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private final int LOCATION_PERMISSIONS_REQUEST = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        initMap();
    }

    private GoogleMap mMap;

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (!checkPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            ActivityCompat.requestPermissions(MapsActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSIONS_REQUEST);
        } else showCurrentLocationOnMap();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSIONS_REQUEST: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    showCurrentLocationOnMap();
                }
                return;
            }
        }
    }

    @SuppressWarnings("MissingPermission")
    private void showCurrentLocationOnMap() {
        FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null && mMap != null) {
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 3));
                        }
                    }
                });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        setUpMap();

        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {
                Log.e("LAT", mMap.getCameraPosition().target.latitude + "");
                Log.e("LONG", mMap.getCameraPosition().target.longitude + "");

                mMap.addMarker(new MarkerOptions().position(mMap.getCameraPosition().target).title("It's Me!"));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mMap.getCameraPosition().target.latitude, mMap.getCameraPosition().target.longitude), 3));
            }
        });
    }

    private void setUpMap() {
        if (mMap != null) {
            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.getUiSettings().setCompassEnabled(true);
            mMap.getUiSettings().setRotateGesturesEnabled(true);
            mMap.getUiSettings().setZoomGesturesEnabled(true);
        }
    }

    private Boolean checkPermission(Context context, String which) {
        return ContextCompat.checkSelfPermission(context, which) == PackageManager.PERMISSION_GRANTED;
    }
}
