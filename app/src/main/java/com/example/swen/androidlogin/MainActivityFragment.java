package com.example.swen.androidlogin;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInstaller;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.Profile;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.xml.transform.Result;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {
    private CallbackManager callbackManager;
    private TextView welcomeTextView;
    private TextView latLongTextView;
    private LocationManager locMgr;
    private LocationListener locListen;
    private double latitude = -1.0;
    private double longitude = -1.0;
    private HttpURLConnection myConn = null;
    private String email;
    private String uid;
    private String name;
    private ArrayList<JSONObject> dataSet;
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    public MainActivityFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FacebookSdk.sdkInitialize(getActivity().getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        email = "abc@def.mail.com";
        uid = "abcdef12345";
        name = "John Doe";
        dataSet = new ArrayList<JSONObject>();
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
        loginButton.setReadPermissions(Arrays.asList("public_profile, email, user_birthday"));
        loginButton.setFragment(this);
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                AccessToken accessToken = loginResult.getAccessToken();
                GraphRequest request = GraphRequest.newMeRequest(accessToken, new GraphRequest.GraphJSONObjectCallback() {
                    @Override
                    public void onCompleted(JSONObject object, GraphResponse response) {
                        email = object.optString("email");
                        uid = object.optString("id");
                        name = object.optString("name");
                        System.out.println("email: " + email + ", uid: " + uid + ", name:" + name);
                        String welcome = "Welcome " + "email: " + email + ", your current location is as follows:";
                        welcomeTextView.setText(welcome);
                        welcomeTextView.setVisibility(View.VISIBLE);
                        String latLong = "Latitude: " + latitude + ";Longitude: " + longitude;
                        latLongTextView.setText(latLong);
                        latLongTextView.setVisibility(View.VISIBLE);

                        //Execute HTTP REST API to the DB
                        String stringURL = "http://192.168.1.66:8080/";
                        String operation = "POST";
                        new HttpActivity().execute(stringURL, operation);
                    }
                });
                Bundle param = new Bundle();
                param.putString("fields", "id, name, email");
                request.setParameters(param);
                request.executeAsync();
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
                    for (int i = 0; i < dataSet.size(); i++) {
                        dataSet.remove(i);
                    }
                    String stringURL = "http://192.168.1.66:8080/" + email;
                    String operation = "DELETE";
                    new HttpActivity().execute(stringURL, operation);
                }
            }
        };

        //Hide the textview for now
        welcomeTextView = (TextView) view.findViewById(R.id.welcomeTextView);
        welcomeTextView.setVisibility(View.GONE);
        latLongTextView = (TextView) view.findViewById(R.id.latLongTextView);
        latLongTextView.setVisibility(View.GONE);

        //RecyclerView
        mRecyclerView = (RecyclerView) view.findViewById(R.id.my_recycler_view);
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(view.getContext(), LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mAdapter = new MyAdapter(dataSet);
        mRecyclerView.setAdapter(mAdapter);

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

                //TO DO execute HTTP to get the closest 10.
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

    //A REST API to update login status and to get the closest 10
    private class HttpActivity extends AsyncTask<String, Integer, String> {

        protected String doInBackground(String... urls) {
            try {
                return handleURL(urls[0], urls[1]);
            } catch (Exception e) {
                return "Error in HttpActivity operation";
            }
        }

        @Override
        protected void onPostExecute(String result) {

            if (myConn != null) {
                Log.v("SwenDev", "myConn is disconnecting");
                myConn.disconnect();
                myConn = null;
            }

            if (result == null) {
                Log.d("SwenDev", "Result is NULL");
                return;
            }

            Log.d("SwenDev", "The result is: " + result);

            //Parse JSON object and assign it to dataSet.
            try {
                if (result.contains("{data")) {
                    JSONObject jObject = new JSONObject(result);
                    JSONArray jArray = jObject.getJSONArray("data");
                    for (int i = 0; i < jArray.length(); i++) {
                        JSONObject obj = jArray.getJSONObject(i);
                        Log.v("SwenDev", "email: " + obj.optString("email"));
                        dataSet.add(obj);
                    }
                }
            }catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }

        private String handleURL(String myURL, String operation) throws IOException, org.json.JSONException {
            InputStream is = null;
            int len = 500;

            try {
                Log.v("SwenDev", "myURL: " + myURL);
                URL url = new URL(myURL);

                if (myConn == null) {
                    myConn = (HttpURLConnection) url.openConnection();
                }

                if (operation.equals("POST")) {
                    myConn.setReadTimeout(10000);
                    myConn.setConnectTimeout(15000);
                    myConn.setRequestMethod("POST");
                    myConn.setDoInput(true);
                    myConn.setDoOutput(true);
                    myConn.setRequestProperty("Content-Type", "application/json");
                    myConn.setRequestProperty("Accept", "application/json");
                    JSONObject data = new JSONObject();
                    JSONObject loc = new JSONObject();
                    loc.put("lon", longitude);
                    loc.put("lat", latitude);
                    data.put("email", email);
                    data.put("loc", loc.toString());
                    OutputStreamWriter wr = new OutputStreamWriter(myConn.getOutputStream());
                    wr.write(data.toString());
                    wr.flush();
                    int responseCode = myConn.getResponseCode();
                    Log.v("SwenDev", "The response is: " + responseCode);
                    is = myConn.getInputStream();
                    String contentAsString = readIt(is, len);
                    return contentAsString;
                }
                else if (operation.equals("DELETE")) {
                    Log.v("SwenDev", "Setting up HTTP DELETE");
                    myConn.setReadTimeout(10000);
                    myConn.setConnectTimeout(15000);
                    myConn.setRequestMethod("DELETE");
                    myConn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
                    myConn.connect();
                    int responseCode = myConn.getResponseCode();
                    is = myConn.getInputStream();
                    String contentAsString = readIt(is, len);
                    return contentAsString;
                }
                else {
                    return null;
                }
            } finally {
                if (is != null)
                    is.close();
            }
        }

        public String readIt(InputStream istream, int length) throws IOException, UnsupportedEncodingException{
            Reader reader = null;
            reader = new InputStreamReader(istream, "UTF-8");
            char [] buffer = new char[length];
            reader.read(buffer);
            istream.close();
            return new String(buffer);
        }
    }
}
