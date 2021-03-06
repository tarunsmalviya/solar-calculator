package com.tarunsmalviya.solarcalculator.activity;

import android.Manifest;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocompleteFragment;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnSuccessListener;
import com.tarunsmalviya.solarcalculator.R;
import com.tarunsmalviya.solarcalculator.adapter.PinnedLocationAdapter;
import com.tarunsmalviya.solarcalculator.model.PinnedLocation;
import com.tarunsmalviya.solarcalculator.util.CommonMethod;
import com.tarunsmalviya.solarcalculator.util.OnPinnedLocationSelected;
import com.tarunsmalviya.solarcalculator.util.SunAlgorithm;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, View.OnClickListener, OnPinnedLocationSelected {

    private final int LOCATION_PERMISSIONS_REQUEST = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        init();
        initView();
        initMap();
        initPlaceAutoComplete();
        setUpListener();

        dateTxt.setText(
                String.valueOf(calendar.get(Calendar.DAY_OF_MONTH)) + " " +

                        calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.US) + ", " +
                        String.valueOf(calendar.get(Calendar.YEAR)));
    }

    private static LatLng point;
    private static Calendar calendar;
    private static CalculationTask calculationTask;
    private AlertDialog pinnedLocationDialog;
    private RealmResults<PinnedLocation> locations;
    private PinnedLocationAdapter pinnedLocationAdapter;

    private void init() {
        point = new LatLng(28.6139, 77.2090);
        calendar = Calendar.getInstance();
        calculationTask = new CalculationTask();
    }

    private GoogleMap mMap;

    private void initMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void initPlaceAutoComplete() {
        PlaceAutocompleteFragment autocompleteFragment = (PlaceAutocompleteFragment)
                getFragmentManager().findFragmentById(R.id.autocomplete_fragment);
        autocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {
                if (mMap != null && place.getLatLng() != null)
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 15f));
            }

            @Override
            public void onError(Status status) {
            }
        });
    }

    // private EditText searchEdt;
    private View actionBar, infoCard, locateBtn, viewPinnedBtn, dateLyt, pinBtn;
    private static TextView dateTxt;
    private static TextView sunriseTxt, sunsetTxt;

    private void initView() {
        // searchEdt = findViewById(R.id.search_edt);
        actionBar = findViewById(R.id.action_bar);
        infoCard = findViewById(R.id.info_card);
        locateBtn = findViewById(R.id.locate_btn);
        viewPinnedBtn = findViewById(R.id.view_pinned_btn);
        dateLyt = findViewById(R.id.date_lyt);
        pinBtn = findViewById(R.id.pin_btn);
        dateTxt = findViewById(R.id.date_txt);
        sunriseTxt = findViewById(R.id.sunrise_txt);
        sunsetTxt = findViewById(R.id.sunset_txt);
    }

    private void setUpListener() {
        locateBtn.setOnClickListener(this);
        viewPinnedBtn.setOnClickListener(this);
        dateLyt.setOnClickListener(this);
        pinBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.locate_btn:
                showCurrentLocationOnMap();
                break;
            case R.id.view_pinned_btn:
                actionViewPinnedLocation();
                break;
            case R.id.date_lyt:
                DialogFragment newFragment = new DatePickerFragment();
                newFragment.show(getSupportFragmentManager(), "datePicker");
                break;
            case R.id.pin_btn:
                actionPinLocation();
                break;
        }
    }

    private void actionViewPinnedLocation() {
        if (locations == null || pinnedLocationAdapter == null || pinnedLocationDialog == null) {
            locations = Realm.getDefaultInstance().where(PinnedLocation.class).findAllAsync().sort("id", Sort.DESCENDING);
            pinnedLocationAdapter = new PinnedLocationAdapter(MapsActivity.this, 0, locations);
            pinnedLocationDialog = new AlertDialog.Builder(MapsActivity.this)
                    .setIcon(R.drawable.ic_pin)
                    .setTitle(R.string.title_pinned_location)
                    .setNegativeButton("Close", null)
                    .setAdapter(pinnedLocationAdapter, null)
                    .create();
        } else pinnedLocationAdapter.notifyDataSetChanged();
        pinnedLocationDialog.show();
    }

    private void actionPinLocation() {
        Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                Number number = realm.where(PinnedLocation.class).max("id");
                long count = 0;
                if (number != null)
                    count = number.longValue();

                PinnedLocation location = realm.createObject(PinnedLocation.class, count + 1);
                location.setDay(calendar.get(Calendar.DAY_OF_MONTH));
                location.setMonth(calendar.get(Calendar.MONTH));
                location.setYear(calendar.get(Calendar.YEAR));
                location.setLatitude(point.latitude);
                location.setLongitude(point.longitude);

                Toast.makeText(getApplicationContext(), "Saved!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onPinnedLocationSelected(final int position, Boolean delete) {
        if (delete) Realm.getDefaultInstance().executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                locations.get(position).deleteFromRealm();
            }
        });
        else {
            if (pinnedLocationDialog != null)
                pinnedLocationDialog.dismiss();

            calendar.set(Calendar.DAY_OF_MONTH, locations.get(position).getDay());
            calendar.set(Calendar.MONTH, locations.get(position).getMonth());
            calendar.set(Calendar.YEAR, locations.get(position).getYear());

            if (mMap != null)
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(locations.get(position).getLatitude(), locations.get(position).getLongitude()), 15f));
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        setUpMap();
    }

    private void setUpMap() {
        if (mMap != null) {
            mMap.getUiSettings().setCompassEnabled(true);
            mMap.getUiSettings().setRotateGesturesEnabled(true);
            mMap.getUiSettings().setZoomGesturesEnabled(true);
            mMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
                @Override
                public void onCameraMoveStarted(int i) {
                    actionBar.setVisibility(View.GONE);
                    infoCard.setVisibility(View.GONE);
                }
            });
            mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
                @Override
                public void onCameraIdle() {
                    Log.d("LATITUDE", mMap.getCameraPosition().target.latitude + "");
                    Log.d("LONGITUDE", mMap.getCameraPosition().target.longitude + "");

                    point = new LatLng(mMap.getCameraPosition().target.latitude, mMap.getCameraPosition().target.longitude);

                    updateTimes();

                    actionBar.setVisibility(View.VISIBLE);
                    infoCard.setVisibility(View.VISIBLE);
                }
            });
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(point, 15f));

            showCurrentLocationOnMap();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[],
                                           int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSIONS_REQUEST: {
                showCurrentLocationOnMap();
                return;
            }
        }
    }

    private void showCurrentLocationOnMap() {
        if (!CommonMethod.checkPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            ActivityCompat.requestPermissions(MapsActivity.this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSIONS_REQUEST);
        } else {
            FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            if (mMap != null)
                                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 15f));
                        }
                    });
        }
    }

    private static void updateTimes() {
        if (calculationTask != null) {
            calculationTask.cancel(true);
            calculationTask = null;
        }
        calculationTask = new CalculationTask();
        calculationTask.execute("");
    }

    private static class CalculationTask extends AsyncTask<String, Integer, double[]> {

        protected double[] doInBackground(String... urls) {
            return SunAlgorithm.calculateTimes(calendar.getTimeInMillis(), point.latitude, point.longitude);
        }

        @Override
        protected void onPostExecute(double[] times) {
            super.onPostExecute(times);

            SimpleDateFormat date = new SimpleDateFormat("dd MMM, yyyy");
            SimpleDateFormat time = new SimpleDateFormat("hh:mm a");

            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(Math.round(times[0]));

            dateTxt.setText(date.format(calendar.getTime()));
            Log.d("Date", date.format(calendar.getTime()));

            sunriseTxt.setText(time.format(calendar.getTime()));
            Log.d("Sunrise Time", time.format(calendar.getTime()));

            calendar.setTimeInMillis(Math.round(times[1]));
            sunsetTxt.setText(time.format(calendar.getTime()));
            Log.d("Sunset Time", time.format(calendar.getTime()));
        }
    }

    public static class DatePickerFragment extends DialogFragment
            implements DatePickerDialog.OnDateSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            if (calendar == null)
                return null;

            // Use the current date as the default date in the picker
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            return new DatePickerDialog(getActivity(), this, year, month, day);
        }

        public void onDateSet(DatePicker view, int year, int month, int day) {
            if (calendar == null)
                return;

            calendar.set(Calendar.DAY_OF_MONTH, day);
            calendar.set(Calendar.MONTH, month);
            calendar.set(Calendar.YEAR, year);

            dateTxt.setText(String.valueOf(day) + " " + calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.US) + ", " + String.valueOf(year));

            updateTimes();
        }
    }
}
