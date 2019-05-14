package de.hacktival.burst;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofenceStatusCodes;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingEvent;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.hacktival.burst.utils.Network;
//import static de.hacktival.burst.Constants;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private static final int MY_PERMISSIONS_LOCATION = 1;

    private GeofencingClient geofencingClient;
    private Geofence zkmFence;
    private PendingIntent geofencePendingIntent;
    private boolean active = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        JSONObject requestParams = new JSONObject();
        try {
            requestParams.put("username", de.hacktival.burst.Settings.username);
            requestParams.put("password", de.hacktival.burst.Settings.password);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest
                (Request.Method.POST, Settings.serverUrl + "user/login/", requestParams, new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {
                        String userToken = null;
                        try {
                            userToken = response.getString("token");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        UserState.getInstance().setUserToken(userToken);
                    }
                }, new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // TODO: Handle error
                        System.out.println("error");
                    }
                });
        Network.getInstance(this).addToRequestQueue(jsonObjectRequest);


        Context context = getApplicationContext();

        if (ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            String[] permissions = {
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
            };
            ActivityCompat.requestPermissions(this,
                    permissions,
                    MY_PERMISSIONS_LOCATION);
        } else {
            setupGeofence();
        }

        setContentView(R.layout.activity_main); //set the layout
        final TextView simpleTextView = (TextView) findViewById(R.id.location_text); //get the id for TextView
        final Button activateButton = (Button) findViewById(R.id.button); //get the id for button

        simpleTextView.setText("Current Location: 6.5555, 3.2222");

        setInZone(false);

        activateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                active = !active;
                if (active) {
                    activateButton.setText("Active");
                } else {
                    activateButton.setText("Inactive");
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay!
                    Log.i(TAG, "Location permission granted");
                    setupGeofence();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Log.i(TAG, "Location permission denied");
                }
            }
            return;
        }
    }

    @SuppressLint("MissingPermission")
    private void setupGeofence() {
        zkmFence = new Geofence.Builder()
                .setRequestId("zkmFence")
                .setCircularRegion(49.001620, 8.383602, Constants.GEOFENCE_RADIUS_IN_METERS)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER |
                        Geofence.GEOFENCE_TRANSITION_EXIT)
                .setExpirationDuration(Constants.GEOFENCE_EXPIRATION_IN_MILLISECONDS)
                .build();

        geofencingClient = LocationServices.getGeofencingClient(this);
        geofencingClient.addGeofences(getGeofencingRequest(), getGeofencePendingIntent())
                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.i(TAG, "Geofences added");
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "Failed to add geofences");
                    }
                });
    }

    private GeofencingRequest getGeofencingRequest() {
        GeofencingRequest.Builder builder = new GeofencingRequest.Builder();
        builder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        List<Geofence> geofenceList = new ArrayList();
        geofenceList.add(zkmFence);
        builder.addGeofences(geofenceList);
        return builder.build();
    }

    private PendingIntent getGeofencePendingIntent() {
        // Reuse the PendingIntent if we already have it.
        if (geofencePendingIntent != null) {
            return geofencePendingIntent;
        }
        Intent intent = new Intent(this, MainActivity.class);
        // We use FLAG_UPDATE_CURRENT so that we get the same pending intent back when
        // calling addGeofences() and removeGeofences().
        geofencePendingIntent = PendingIntent.getService(this, 0, intent, PendingIntent.
                FLAG_UPDATE_CURRENT);
        return geofencePendingIntent;
    }

    protected void onHandleIntent(Intent intent) {
        GeofencingEvent geofencingEvent = GeofencingEvent.fromIntent(intent);
        if (geofencingEvent.hasError()) {
            int errorCode = geofencingEvent.getErrorCode();
            Log.e(TAG, "Error: " + String.valueOf(errorCode));
            return;
        }

        // Get the transition type.
        int geofenceTransition = geofencingEvent.getGeofenceTransition();

        // Test that the reported transition was of interest.
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER ||
                geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {

            // Get the geofences that were triggered. A single event can trigger
            // multiple geofences.
            List<Geofence> triggeringGeofences = geofencingEvent.getTriggeringGeofences();

            // TODO: Check geofence that was entered

            if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
                setInZone(true);
            } else if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_EXIT) {
                setInZone(false);
            }
        }
    }

    private void setInZone(boolean inZone) {
        if (inZone) {
            Log.i(TAG, "Entered zone!");
            final TextView simpleTextView = (TextView) findViewById(R.id.location_text);
            simpleTextView.setBackgroundColor(Color.GREEN);
        } else {
            Log.i(TAG, "Exited zone!");
            final TextView simpleTextView = (TextView) findViewById(R.id.location_text);
            simpleTextView.setBackgroundColor(Color.YELLOW);
        }
    }

}
