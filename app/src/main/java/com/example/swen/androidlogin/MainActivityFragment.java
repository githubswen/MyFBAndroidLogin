package com.example.swen.androidlogin;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.telecom.Call;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.Profile;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {
    CallbackManager callbackManager;
    TextView welcomeTextView;
    TextView latLongTextView;
    LocationManager locMgr;
    LocationListener locListen;
    double latitude = -1.0;
    double longitude = -1.0;

    public MainActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getActivity().getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        //Facebook login button
        LoginButton loginButton = (LoginButton) view.findViewById(R.id.login_button);
        loginButton.setReadPermissions("user_friends");
        loginButton.setFragment(this);
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                AccessToken accessToken = loginResult.getAccessToken();
                Profile profile = Profile.getCurrentProfile();
                String welcome = "Welcome " + profile.getName() + ", your current location is as follows:";
                welcomeTextView.setText(welcome);
                welcomeTextView.setVisibility(View.VISIBLE);
                String latLong = "Latitude: " + latitude + ";Longitude: " + longitude;
                latLongTextView.setText(latLong);
                latLongTextView.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancel() {

            }

            @Override
            public void onError(FacebookException e) {

            }
        });

        //AccessTokenTracker to track if user logging out.
        AccessTokenTracker accessToken = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(AccessToken accessToken, AccessToken accessToken1) {
                if (accessToken1 == null) {
                    Log.v("SwenDev", "Logging out from Facebook");
                    welcomeTextView.setVisibility(View.GONE);
                    latLongTextView.setVisibility(View.GONE);
                }
            }
        };

        //Hide the textview for now
        welcomeTextView = (TextView) view.findViewById(R.id.welcomeTextView);
        welcomeTextView.setVisibility(View.GONE);
        latLongTextView = (TextView) view.findViewById(R.id.latLongTextView);
        latLongTextView.setVisibility(View.GONE);

        //Location tracker
        locMgr = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);
        locListen = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                try {
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                } catch (NullPointerException e) {
                    latitude = -1.0;
                    longitude = -1.0;
                }

                String latLong = "Latitude: " + latitude + ";Longitude: " + longitude;
                latLongTextView.setText(latLong);
                Log.v("SwenDev", latLong);
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        Location loc = locMgr.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        try {
            latitude = loc.getLatitude();
            longitude = loc.getLongitude();
        } catch (NullPointerException e) {
            latitude = -1.0;
            longitude = -1.0;
        }


        //TODO add check permission
        locMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 180000, 0, locListen);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }
}
