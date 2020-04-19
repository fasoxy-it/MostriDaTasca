package it.mattiafasoli.mostridatasca;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;

import org.json.JSONException;
import org.json.JSONObject;

public class Register extends AppCompatActivity implements OnMapReadyCallback, Style.OnStyleLoaded {

    // MapBox
    private MapView mapView;
    private MapboxMap mapboxMap;
    private Style style;

    // Server Request Queue
    private RequestQueue requestQueue;

    // Server Request Insertion Text
    JSONObject jsonBody = new JSONObject();

    // Server Request URL
    private static final String BASE_URL = "https://ewserver.di.unimi.it/mobicomp/mostri/";
    private static final String REGISTER_API = "register.php";

    // Shared Preference
    private static final String SHARED_PREFERENCES = "sharedPreference";
    private static final String SESSION_ID = "SESSION_ID";

    // User Information
    private static String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("Register", "Method onCreate");

        // MapBox Token
        Mapbox.getInstance(this, "pk.eyJ1IjoiZmFzb3h5IiwiYSI6ImNrMzcyenJoYzA1a3MzZHFsNmdwaWswbTUifQ.NhDg1pENhLDS5KwpexZLhQ");
        setContentView(R.layout.activity_register);

        // MapBox MapView
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

    }

    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMap) {
        Log.d("MainActivity", "Map ready");

        // MapBox Map
        this.mapboxMap = mapboxMap;
        mapboxMap.setStyle(Style.DARK, this);
    }

    @Override
    public void onStyleLoaded(@NonNull final Style style) {
        Log.d("MainActivity", "Style ready");

        // MapBox Style
        this.style = style;

        // Set Camera Position
        setCameraPosition();

        // Get [session_id] from Shared Preference
        loadData();

        if (userId == "") {

            // Set Register Button
            View registerButton = findViewById(R.id.registerButton);
            registerButton.setOnClickListener(objectClickListener);

        } else {

            Intent loginIntent = new Intent(getApplicationContext(), MainActivity.class);
            loginIntent.putExtra("session_id", userId);
            startActivity(loginIntent);

        }

    }



    @Override
    public void onStart() {
        super.onStart();
        Log.d("Register", "Method onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("Register", "Method onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("Register", "Method onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d("Register", "Method onStop");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("Register", "Method onDestroy");
    }

    // Object Click Listener
    private View.OnClickListener objectClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {

                // onClick Register Button
                case R.id.registerButton:
                    Log.d("Register", "Method onClick registerButton");

                    register();

                    break;
            }
        }
    };

    public void setCameraPosition() {

        Log.d("Register", "Method setCameraPosition");

        CameraPosition position = new CameraPosition.Builder()
                .target(new LatLng(45.4773,9.1815 ))
                .zoom(10)
                .tilt(20)
                .bearing(0)
                .build();
        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 1000);

    }

    public void loadData() {
        Log.d("Register", "Method loadData");

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
        userId = sharedPreferences.getString(SESSION_ID, "");
    }

    public void saveData(){
        Log.d("Register", "Method saveData");

        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(SESSION_ID, userId);
        editor.apply();
    }

    public void register() {

        Log.d("Register", "Method Register");

        // Create the Request Queue
        requestQueue = Volley.newRequestQueue(this);

        // Get [session_id] from Server
        JsonObjectRequest getRegisterRequest = new JsonObjectRequest(
                BASE_URL + REGISTER_API,
                jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {

                        Log.d("Register", "Request done");

                        try {

                            // Set [session_id]
                            userId = response.getString("session_id");

                            // Save [session_id] into SharedPreferences
                            saveData();

                            // Load [session_id] from SharedPreferences
                            loadData();

                            Intent registerIntent = new Intent(getApplicationContext(), Login.class);
                            registerIntent.putExtra("session_id", userId);
                            startActivity(registerIntent);

                        } catch (JSONException ex){
                            ex.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Register", "Request failed");
                        Toast toast = Toast.makeText(getApplicationContext(), "Request failed", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }
        );

        // Add the Request to the Request Queue
        requestQueue.add(getRegisterRequest);

    }
}
