package com.example.saad.hci;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.daimajia.slider.library.Indicators.PagerIndicator;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;

public class MainActivity extends Activity implements BaseSliderView.OnSliderClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener, LocationListener {
    Class_MyApplication         app;
    SliderLayout                mDemoSliderPopular;
    int                         backButtonCount;
    boolean                     displaySearch;
    GoogleApiClient             mGoogleApiClient;
    Location                    mLastLocation;
    Location                    mCurrentLocation;
    LocationRequest             mLocationRequest;
    FusedLocationProviderClient mFusedLocationClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        backButtonCount = 0;
        displaySearch = false;
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        // reference the MyApplication variable to access global variables.
        app = (Class_MyApplication) getApplication();

        // Slider
        mDemoSliderPopular = (SliderLayout)findViewById(R.id.main_slider);
        setupSlider();
        PagerIndicator pagerIndicator = (PagerIndicator)findViewById(R.id.custom_indicator);
        mDemoSliderPopular.setCustomIndicator(pagerIndicator);
        mDemoSliderPopular.setDuration(3);
        mDemoSliderPopular.setCurrentPosition(app.all_HeritageSites.size()-1);

        // Search Bar
        setupSearch();

        // Title
        TextView textView = (TextView) findViewById(R.id.main_title);
        textView.setText("SNIST");

        // GPS
        buildGoogleApiClient();
        createLocationRequest();
        //getLocation();
    }

    public void setupSlider() {
        for (Class_HeritageSite heritageSite : app.all_HeritageSites) {
            int image_id = getResources().getIdentifier(heritageSite.getImageHor(), "drawable", getPackageName());
            Class_MyTextSliderView textSliderView = new Class_MyTextSliderView(this);
            textSliderView.description(heritageSite.getName()).image(image_id).setOnSliderClickListener(this);
            mDemoSliderPopular.addSlider(textSliderView);
        }
    }

    @Override
    public void onSliderClick(BaseSliderView baseSliderView) {
        Toast.makeText(this,"View Them Virtually!",Toast.LENGTH_LONG).show();

    }

    public void setupSearch() {
        AutoCompleteTextView searchbar = (AutoCompleteTextView) findViewById(R.id.main_search);
        String[] s = new String[app.all_HeritageSites.size()];

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, app.all_searchables.toArray(s));
        searchbar.setAdapter(adapter);
        searchbar.setOnEditorActionListener(new AutoCompleteTextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    performSearch(null);
                    return true;
                }
                return false;
            }
        });
    }

    public void performSearch(View v) {
        if (displaySearch == false) {
            displaySearch = true;
            AutoCompleteTextView textView = (AutoCompleteTextView) findViewById(R.id.main_search);
            textView.setVisibility(View.VISIBLE);
            (findViewById(R.id.main_title)).setVisibility(View.INVISIBLE);
            if (textView.requestFocus()) {
                ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).showSoftInput(textView, InputMethodManager.SHOW_IMPLICIT);
            }
        } else {
            AutoCompleteTextView textView = (AutoCompleteTextView) findViewById(R.id.main_search);
            String text = textView.getText().toString();
            if (app.all_searchables.contains(text)) {
                Intent intent;

                    intent = new Intent(this, ListDisplayActivity.class);
                    intent.putExtra("MODE", "SEARCH");
                    intent.putExtra("NAME", text);
                    Toast.makeText(this, "Try viewing Augumentation in Wikitude", Toast.LENGTH_SHORT).show();

                startActivity(intent);
                mDemoSliderPopular.stopAutoCycle();
            } else {
                Toast.makeText(this, "Does Not Exist", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void redirectNearest(View v) {
        Toast.makeText(this, "Redirected to Nearest Page", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, ListDisplayActivity.class);
        intent.putExtra("MODE", "NEAREST");
        startActivity(intent);
        mDemoSliderPopular.stopAutoCycle();
    }

    public void redirectPopular(View v) {
        Toast.makeText(this, "Redirected to Popular Page", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(this, ListDisplayActivity.class);
        intent.putExtra("MODE", "POPULAR");
        startActivity(intent);
        mDemoSliderPopular.stopAutoCycle();
    }

    public void redirectTrails(View v) {
        Toast.makeText(this, "Lets go!", Toast.LENGTH_SHORT).show();
    }




    public void goHome(View v) {
        hideSearchBar();
    }

    public void performAugmented(View v) {
        Intent intent = new Intent(this, AugmentedActivity.class);
        startActivity(intent);
        mDemoSliderPopular.stopAutoCycle();
    }



    @Override
    public void onBackPressed() {
        if(backButtonCount >= 1)
        {

            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
        else
        {
            Toast.makeText(this, "Press the back button once again to close the application.", Toast.LENGTH_SHORT).show();
            backButtonCount++;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mGoogleApiClient.disconnect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        backButtonCount = 0;
        mDemoSliderPopular.startAutoCycle();
        hideSearchBar();
        mGoogleApiClient.connect();
    }

    public void hideSearchBar() {
        displaySearch = false;
        AutoCompleteTextView textView = (AutoCompleteTextView) findViewById(R.id.main_search);
        ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(textView.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        textView.setVisibility(View.GONE);
        textView.setText("");
        findViewById(R.id.main_title).setVisibility(View.VISIBLE);
    }


    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).addApi(LocationServices.API).build();
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void startLocationUpdates() {
       // LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {

                    }
                });

    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        app.setLoc(new float[] {(float)mLastLocation.getLatitude(), (float)mLastLocation.getLongitude()});
    }

    @Override
    public void onConnected(Bundle bundle) {
        startLocationUpdates();
        mLastLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        if (mLastLocation != null) {
            app.setLoc(new float[] {(float)mLastLocation.getLatitude(), (float)mLastLocation.getLongitude()});
        }
    }

    @Override
    public void onConnectionSuspended(int i) {}

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Toast.makeText(this, "Error: Could not connect to GPS", Toast.LENGTH_LONG).show();
    }



}
