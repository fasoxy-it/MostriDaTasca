package it.mattiafasoli.mostridatasca;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.android.core.permissions.PermissionsListener;
import com.mapbox.android.core.permissions.PermissionsManager;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.maps.UiSettings;
import com.mapbox.mapboxsdk.plugins.annotation.OnSymbolClickListener;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, Style.OnStyleLoaded, PermissionsListener {

    // MapBox
    private MapView mapView;
    private MapboxMap mapboxMap;
    private Style style;

    // User Location
    private PermissionsManager permissionsManager;
    private LocationEngine locationEngine;
    private LocationListeningCallback locationListeningCallback;
    private Location location;

    private static final long DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L;
    private static final long DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 5;

    // Server Request Queue
    public RequestQueue requestQueue = null;

    // Server Request URL
    public static final String BASE_URL = "https://ewserver.di.unimi.it/mobicomp/mostri/";
    public static final String MONSTERS_CANDIES_MAP_API = "getmap.php";
    public static final String USER_PROFILE_INFORMATION_API = "getprofile.php";

    // Insertion Request Text
    JSONObject jsonBody;
    public static String SESSION_ID;

    public static int USER_XP;
    public static int USER_LIFEPOINTS;

    // Monster / Candies ArrayList
    private ArrayList<MonsterCandy> monsterscandies = new ArrayList<MonsterCandy>();

    // Monsters / Candies Icon Map Insertion
    private SymbolManager symbolManager;

    public static final String SMALL_CANDY_ICON = "small candy";
    public static final String MEDIUM_CANDY_ICON = "medium candy";
    public static final String LARGE_CANDY_ICON = "large candy";

    public static final String SMALL_MONSTER_ICON = "small monster";
    public static final String MEDIUM_MONSTER_ICON = "medium monster";
    public static final String LARGE_MONSTER_ICON = "large monster";

    // Monsters / Candies Icon Map ArrayList
    public ArrayList<SymbolManager> monsterscandiesIconMap = new ArrayList<SymbolManager>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("MainActivity", "Method onCreate");

        // MapBox Token
        Mapbox.getInstance(this, "pk.eyJ1IjoiZmFzb3h5IiwiYSI6ImNrMzcyenJoYzA1a3MzZHFsNmdwaWswbTUifQ.NhDg1pENhLDS5KwpexZLhQ");
        setContentView(R.layout.activity_main);

        // MapBox MapView
        mapView = findViewById(R.id.mapView);
        mapView.onCreate(savedInstanceState);
        mapView.getMapAsync(this);

        // Location Permission
        permissionsManager = new PermissionsManager(this);

        // MapBox Location Update
        locationEngine = LocationEngineProvider.getBestLocationEngine(this);
        locationListeningCallback = new LocationListeningCallback(this);

        // Extra Information
        Bundle bundle = getIntent().getExtras();

        // Get SESSION_ID
        SESSION_ID = bundle.getString("session_id");
        Log.d("MainActivity", "SESSION_ID: " +  SESSION_ID);

        // User Location
        View userLocationView = findViewById(R.id.userLocation);
        userLocationView.setOnClickListener(objectClickListener);

        // Profile
        View userInformationView = findViewById(R.id.userInformation);
        userInformationView.setOnClickListener(objectClickListener);

        // Ranking
        View rankingInformationView = findViewById(R.id.rankingInformation);
        rankingInformationView.setOnClickListener(objectClickListener);

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

        Timer timer = new Timer();
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {

                removeMonstersCandiesInformation();
                Log.d("MainActivity", "Method removeMonstersCandiesInformation");

                getMonstersCandiesInformation();
                Log.d("MainActivity", "Method getMonstersCandiesInformation");

            }
        };

        timer.schedule(timerTask, 0, 10000);


        // Permission Location Request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {

            showUserLocation();

            cameraPosition();

            Log.d("MainActivity", "Method showUserLocation");

        } else {

            permissionsManager = new PermissionsManager(this);
            permissionsManager.requestLocationPermissions(this);

        }

    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("MainActivity", "Method onStart");
        mapView.onStart();

    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("MainActivity", "Method onResume");

        getUserInformation();
        Log.d("MainActivity", "Method getUserInformation");

        mapView.onResume();

    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("MainActivity", "Method onPause");
        mapView.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d("MainActivity", "Method onStop");

        // MapBox Remove Location Update
        if (locationEngine != null) {
            locationEngine.removeLocationUpdates(locationListeningCallback);
        }

        mapView.onStop();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.d("MainActivity", "Method onLowMemory");
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("MainActivity", "Method onDestroy");
        mapView.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d("MainActivity", "Method onSavedInstanceState");
        mapView.onSaveInstanceState(outState);
    }

    public void removeMonstersCandiesInformation() {

        monsterscandies.clear();
        //Log.d("MainActivity", "ArrayList<MonsterCandy> monsterscandies: " + monsterscandies.size());

    }

    public void getMonstersCandiesInformation() {

        // Create Server Request Queue
        requestQueue = Volley.newRequestQueue(this);

        // Server Request Insertion SESSION_ID
        try {
            jsonBody = new JSONObject("{\"session_id\":\"" + SESSION_ID + "\"}");
        } catch (JSONException ex) {
            Log.d("MainActivity", "Insert SESSION_ID failed");
            Toast toast = Toast.makeText(getApplicationContext(), "Insertion failed", Toast.LENGTH_SHORT);
            toast.show();
        }

        // Get Monsters / Candies Information
        JsonObjectRequest getMonstersCandiesInformation = new JsonObjectRequest(
                BASE_URL + MONSTERS_CANDIES_MAP_API,
                jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("MainActivity", "Request done");
                        //Log.d("MainActivity", response.toString());

                        // Depopulate Model with Monsters / Candies Information
                        Model.getInstance().depopulateMonsterCandies();
                        Log.d("MainActivity", "Method depopulateMonsterCandies");
                        //Log.d("MainActivity", "Method depopulateMonsterCandies: " + Model.getInstance().getSize());

                        // Populate Model with Monsters / Candies Information
                        Model.getInstance().populateMonstersCandies(response);
                        Log.d("MainActivity", "Method populateMonsterCandies");
                        //Log.d("MainActivity", "Method populateMonsterCandies: " + Model.getInstance().getSize());

                        // Get Monsters / Candies Information from Model
                        monsterscandies = Model.getInstance().getMonstersCandiesList();
                        //Log.d("MainActivity", "Monsters / Candies: " + monsterscandies);

                        // Delete Monsters Candies Icon from Map
                        deleteMonstersCandiesMap();
                        Log.d("MainActivity", "Method deleteMonstersCandiesMap");


                        // Add Monsters Candies Icon from Map
                        addMonstersCandiesMap();
                        Log.d("MainActivity", "Method addMonstersCandiesMap");


                        //Log.d("MainActivity", "ArrayList<MonsterCandy> monsterscandies: " + monsterscandies.size());
                        //Log.d("MainActivity", "ArrayList<SymbolManager> monsterscandiesIconMap: " + monsterscandiesIconMap.size());


                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("MainActivity", "Request failed");
                        Toast toast = Toast.makeText(getApplicationContext(), "Request failed", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }
        );

        // Add the Request to the Request Queue
        requestQueue.add(getMonstersCandiesInformation);

    }

    public void deleteMonstersCandiesMap() {

        for (int i=0; i<monsterscandiesIconMap.size(); i++){
            monsterscandiesIconMap.get(i).deleteAll();
        }

        monsterscandiesIconMap.clear();

        //Log.d("MainActivity", "ArrayList<SymbolManager> monsterscandiesIconMap: " + monsterscandiesIconMap.size());

    }

    public void addMonstersCandiesMap() {
        for (int i = 0; i < monsterscandies.size(); i++) {
            final String monstercandyId = monsterscandies.get(i).getMonsterCandyId();
            final Double monstercandyLat = monsterscandies.get(i).getMonsterCandyLat();
            final Double monstercandyLon = monsterscandies.get(i).getMonsterCandyLon();
            final String monstercandyType = monsterscandies.get(i).getMonsterCandyType();
            final String monstercandySize = monsterscandies.get(i).getMonsterCandySize();
            final String monstercandyName = monsterscandies.get(i).getMonsterCandyName();
            //Log.d("MainActivity", "" + monsters.get(i).getMonster());

            switch (monstercandyType) {

                case "MO" :

                    switch (monstercandySize) {

                        case "S" :
                            style.addImage(SMALL_MONSTER_ICON, BitmapFactory.decodeResource(MainActivity.this.getResources(), R.drawable.small_monster_icon));
                            symbolManager = new SymbolManager(mapView, mapboxMap, style);
                            symbolManager.setIconAllowOverlap(true);
                            symbolManager.setTextAllowOverlap(true);
                            symbolManager.create(new SymbolOptions()
                                    .withLatLng(new LatLng(monstercandyLat, monstercandyLon))
                                    .withIconImage(SMALL_MONSTER_ICON)
                                    .withIconSize(0.75f));
                            monsterscandiesIconMap.add(symbolManager);
                            //Log.d("MainActivity", "Small Monster Added")
                            break;

                        case "M" :
                            style.addImage(MEDIUM_MONSTER_ICON, BitmapFactory.decodeResource(MainActivity.this.getResources(), R.drawable.medium_monster_icon));
                            symbolManager = new SymbolManager(mapView, mapboxMap, style);
                            symbolManager.setIconAllowOverlap(true);
                            symbolManager.create(new SymbolOptions()
                                    .withLatLng(new LatLng(monstercandyLat, monstercandyLon))
                                    .withIconImage(MEDIUM_MONSTER_ICON)
                                    .withIconSize(0.75f));
                            monsterscandiesIconMap.add(symbolManager);
                            //Log.d("MainActivity", "Medium Monster Added");
                            break;

                        case "L" :
                            style.addImage(LARGE_MONSTER_ICON, BitmapFactory.decodeResource(MainActivity.this.getResources(), R.drawable.large_monster_icon));
                            symbolManager = new SymbolManager(mapView, mapboxMap, style);
                            symbolManager.setIconAllowOverlap(true);
                            symbolManager.create(new SymbolOptions()
                                    .withLatLng(new LatLng(monstercandyLat, monstercandyLon))
                                    .withIconImage(LARGE_MONSTER_ICON)
                                    .withIconSize(0.75f));
                            monsterscandiesIconMap.add(symbolManager);
                            //Log.d("MainActivity", "Large Monster Added");
                            break;

                    }

                    break;

                case "CA" :

                    switch (monstercandySize) {

                        case "S" :
                            style.addImage(SMALL_CANDY_ICON, BitmapFactory.decodeResource(MainActivity.this.getResources(), R.drawable.small_candy_icon));
                            symbolManager = new SymbolManager(mapView, mapboxMap, style);
                            symbolManager.setIconAllowOverlap(true);
                            symbolManager.create(new SymbolOptions()
                                    .withLatLng(new LatLng(monstercandyLat, monstercandyLon))
                                    .withIconImage(SMALL_CANDY_ICON)
                                    .withIconSize(0.75f));
                            monsterscandiesIconMap.add(symbolManager);
                            //Log.d("MainActivity", "Small Candy Added");
                            break;

                        case "M" :
                            style.addImage(MEDIUM_CANDY_ICON, BitmapFactory.decodeResource(MainActivity.this.getResources(), R.drawable.medium_candy_icon));
                            symbolManager = new SymbolManager(mapView, mapboxMap, style);
                            symbolManager.setIconAllowOverlap(true);
                            symbolManager.create(new SymbolOptions()
                                    .withLatLng(new LatLng(monstercandyLat, monstercandyLon))
                                    .withIconImage(MEDIUM_CANDY_ICON)
                                    .withIconSize(0.75f));
                            monsterscandiesIconMap.add(symbolManager);
                            //Log.d("MainActivity", "Medium Candy Added");
                            break;

                        case "L" :
                            style.addImage(LARGE_CANDY_ICON, BitmapFactory.decodeResource(MainActivity.this.getResources(), R.drawable.large_candy_icon));
                            symbolManager = new SymbolManager(mapView, mapboxMap, style);
                            symbolManager.setIconAllowOverlap(true);
                            symbolManager.create(new SymbolOptions()
                                    .withLatLng(new LatLng(monstercandyLat, monstercandyLon))
                                    .withIconImage(LARGE_CANDY_ICON)
                                    .withIconSize(0.75f));
                            monsterscandiesIconMap.add(symbolManager);
                            //Log.d("MainActivity", "Large Candy Added");
                            break;

                    }

                    break;
            }

            symbolManager.addClickListener(new OnSymbolClickListener() {
                @Override
                public void onAnnotationClick(Symbol symbol) {

                    Intent fighteatIntent = new Intent(getApplicationContext(), PopUp.class);

                    fighteatIntent.putExtra("userId", SESSION_ID);
                    fighteatIntent.putExtra("userXp", USER_XP);
                    fighteatIntent.putExtra("userLifepoints", USER_LIFEPOINTS);
                    fighteatIntent.putExtra("userLat", location.getLatitude());
                    fighteatIntent.putExtra("userLon", location.getLongitude());
                    fighteatIntent.putExtra("monstercandyId", monstercandyId);
                    fighteatIntent.putExtra("monstercandyLat", monstercandyLat);
                    fighteatIntent.putExtra("monstercandyLon", monstercandyLon);
                    fighteatIntent.putExtra("monstercandyType", monstercandyType);
                    fighteatIntent.putExtra("monstercandySize", monstercandySize);
                    fighteatIntent.putExtra("monstercandyName", monstercandyName);

                    startActivity(fighteatIntent);

                }
            });

        }

    }

    // Permission Request
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    // Permission Result
    @Override
    public void onPermissionResult(boolean granted) {
        if (granted) {

            Log.d("MainActivity", "Location Permission Granted");

            // Showing User Location if the Location Permission is granted
            showUserLocation();

        } else {

            Log.d("MainActivity", "Location Permission Not Granted");

            Toast.makeText(this, "Location permission not granted", Toast.LENGTH_LONG).show();

        }
    }

    public void showUserLocation() {
        LocationEngineRequest request = new LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
                .setPriority(LocationEngineRequest.PRIORITY_NO_POWER)
                .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME)
                .build();

        locationEngine.requestLocationUpdates(request, locationListeningCallback, getMainLooper());
        locationEngine.getLastLocation(locationListeningCallback);

        LocationComponent locationComponent = mapboxMap.getLocationComponent();

        locationComponent.activateLocationComponent(LocationComponentActivationOptions.builder(this, style).build());
        locationComponent.setLocationComponentEnabled(true);
        locationComponent.setCameraMode(CameraMode.NONE);
        locationComponent.setRenderMode(RenderMode.NORMAL);

        UiSettings settings = mapboxMap.getUiSettings();
        settings.setCompassEnabled(false);

    }

    public void cameraPosition() {
        CameraPosition position = new CameraPosition.Builder()
                .target(new LatLng(location.getLatitude(), location.getLongitude()))
                .zoom(10)
                .tilt(20)
                .bearing(0)
                .build();
        mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(position), 1000);
    }

    @Override
    public void onExplanationNeeded(List<String> permissionsToExplain) {

    }

    private static class LocationListeningCallback implements LocationEngineCallback<LocationEngineResult> {

        private final WeakReference<MainActivity> activityWeakReference;
        private MainActivity mainActivity;

        LocationListeningCallback(MainActivity activity) {
            this.activityWeakReference = new WeakReference<>(activity);
            mainActivity = activity;
        }

        @Override
        public void onSuccess(LocationEngineResult result) {
            mainActivity.location = result.getLastLocation();
        }

        @Override
        public void onFailure(@NonNull Exception exception) {

        }
    }

    public void getUserInformation() {

        // Create Server Request Queue
        requestQueue = Volley.newRequestQueue(this);

        // Server Request Insertion SESSION_ID
        try {
            jsonBody = new JSONObject("{\"session_id\":\"" + SESSION_ID + "\"}");
        } catch (JSONException ex) {
            Log.d("MainActivity", "Insert SESSION_ID failed");
            Toast toast = Toast.makeText(getApplicationContext(), "Insertion failed", Toast.LENGTH_SHORT);
            toast.show();
        }

        // Get User Information from Server
        JsonObjectRequest getUserProfileInformationRequest = new JsonObjectRequest(
                BASE_URL + USER_PROFILE_INFORMATION_API,
                jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("MainActivity", "Request done");
                        Log.d("MainActivity", response.toString());

                        try {

                            String username = response.getString("username");
                            TextView usernameTextView = findViewById(R.id.userName);
                            usernameTextView.setText(username);
                            //Log.d("MainActivity", username);

                            String img = response.getString("img");
                            ImageView userImageView = findViewById(R.id.userImage);
                            byte[] decodedString = Base64.decode(img, Base64.DEFAULT);
                            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                            userImageView.setImageBitmap(decodedByte);
                            //Log.d("MainActivity", img);

                            int xp = response.getInt("xp");
                            USER_XP = xp;
                            TextView xpTextView = findViewById(R.id.xp);
                            xpTextView.setText(String.valueOf(xp));
                            //Log.d("MainActivity", xp);

                            int lp = response.getInt("lp");
                            USER_LIFEPOINTS = lp;
                            ProgressBar lifepointsProgressBar = findViewById(R.id.lifepoints);
                            lifepointsProgressBar.setProgress(lp);
                            //Log.d("MainActivity", "" + lp);

                        } catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("MainActivity", "Request failed");
                        Toast toast = Toast.makeText(getApplicationContext(), "Request failed", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }
        );

        // Add the Request to the Request Queue
        requestQueue.add(getUserProfileInformationRequest);
    }

    // Object Click Listener
    private View.OnClickListener objectClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {

                // onClick User Location
                case R.id.userLocation:
                    Log.d("MainActivity", "Method onClick userLocationImageView");
                    if (location != null) {
                        cameraPosition();
                    }
                    break;
                case R.id.rankingInformation:
                    Log.d("MainActivity", "Method onClick rankingInformation");
                    Intent rankingInformationIntent = new Intent(getBaseContext(), Ranking.class);
                    rankingInformationIntent.putExtra("session_id", SESSION_ID);
                    startActivity(rankingInformationIntent);
                    break;
                case R.id.userInformation:
                    Log.d("MainActivity", "Method onClick userInformation");
                    Intent userInformationIntent = new Intent(getBaseContext(), Profile.class);
                    userInformationIntent.putExtra("session_id", SESSION_ID);
                    startActivity(userInformationIntent);
                    break;
            }
        }
    };


}
