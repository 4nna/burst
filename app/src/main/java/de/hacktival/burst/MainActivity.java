
package de.hacktival.burst;

import android.content.ContentResolver;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import de.hacktival.burst.utils.Network;

public class MainActivity extends AppCompatActivity {


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
                (Request.Method.POST, Settings.serverUrl+"user/login/", requestParams, new Response.Listener<JSONObject>() {

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

        setContentView(R.layout.activity_main); //set the layout
        final TextView simpleTextView = (TextView) findViewById(R.id.location_text); //get the id for TextView
        final Button activateButton = (Button) findViewById(R.id.button); //get the id for button
        activateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                simpleTextView.setText("Current Location: 6.5555, 3.2222"); //set the text after clicking button
                activateButton.setText("Inactive");
            }

        });

    }
}
