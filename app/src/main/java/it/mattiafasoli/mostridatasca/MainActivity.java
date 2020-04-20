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
import android.view.Gravity;
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
    private RequestQueue requestQueue;

    // Server Request Insertion Text
    JSONObject jsonBody = new JSONObject();

    // Server Request URL
    private static final String BASE_URL = "https://ewserver.di.unimi.it/mobicomp/mostri/";
    private static final String MONSTERS_CANDIES_MAP_API = "getmap.php";
    private static final String USER_PROFILE_INFORMATION_API = "getprofile.php";

    // User Information
    public static String userId;
    public static String userName;
    public static Bitmap userImage;
    public static int userXp;
    public static int userLifepoints;

    // MonsterCandy ArrayList
    private ArrayList<MonsterCandy> monsterscandies = new ArrayList<MonsterCandy>();

    // MonsterCandy Icon Map Insertion
    private SymbolManager symbolManager;

    // MonsterCandy Icon Map Information
    private static final String SMALL_CANDY_ICON = "small candy";
    private static final String MEDIUM_CANDY_ICON = "medium candy";
    private static final String LARGE_CANDY_ICON = "large candy";

    private static final String SMALL_MONSTER_ICON = "small monster";
    private static final String MEDIUM_MONSTER_ICON = "medium monster";
    private static final String LARGE_MONSTER_ICON = "large monster";

    // MonsterCandy Icon Map ArrayList
    public ArrayList<SymbolManager> monsterscandiesIconMap = new ArrayList<SymbolManager>();

    // Timer MonsterCandy Information
    Timer timer = new Timer();

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

        // Get Extra Information from previous Activity
        getExtraInformation();

        // Set User Location Button
        View userLocationButton = findViewById(R.id.userLocation);
        userLocationButton.setOnClickListener(objectClickListener);

        // Set Profile Information Button
        View userInformationView = findViewById(R.id.userInformation);
        userInformationView.setOnClickListener(objectClickListener);

        // Set Ranking Information Button
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

        // Update MonsterCandy Information
        TimerTask timerTask = new TimerTask() {
            @Override
            public void run() {

                // Remove MonsterCandy Information from ArrayList
                removeMonstersCandiesInformation();

                // Get MonsterCandy Information into ArrayList
                getMonstersCandiesInformation();

            }
        };

        timer.schedule(timerTask, 0, 10000);

        // Verify Permission Location Request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {

            // Show User Location
            showUserLocation();

            // Set Camera Position
            setCameraPosition();

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

        // Get User Information from Server
        getUserInformation();

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

        // Remove MonsterCandy Information Update
        timer.cancel();

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

    // Object Click Listener
    private View.OnClickListener objectClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {

                // onClick User Location Button
                case R.id.userLocation:
                    Log.d("MainActivity", "Method onClick userLocationButton");

                    if (location != null) {
                        setCameraPosition();
                    }

                    break;

                // onClick User Information View
                case R.id.userInformation:
                    Log.d("MainActivity", "Method onClick userInformation");

                    Intent userInformationIntent = new Intent(getBaseContext(), Profile.class);
                    userInformationIntent.putExtra("userId", userId);

                    startActivity(userInformationIntent);

                    break;

                // onClick Ranking Information View
                case R.id.rankingInformation:
                    Log.d("MainActivity", "Method onClick rankingInformation");

                    Intent rankingInformationIntent = new Intent(getBaseContext(), Ranking.class);
                    rankingInformationIntent.putExtra("userId", userId);
                    startActivity(rankingInformationIntent);

                    break;
            }
        }
    };

    public void getExtraInformation() {

        Log.d("MainActivity", "Method getExtraInformation");

        // Get Extra Information from previous Activity
        Bundle bundle = getIntent().getExtras();

        // Get / Set [session_id] from previous Activity
        userId = bundle.getString("session_id");

    }

    public void removeMonstersCandiesInformation() {

        Log.d("MainActivity", "Method removeMonstersCandiesInformation");

        monsterscandies.clear();

    }

    public void getMonstersCandiesInformation() {

        Log.d("MainActivity", "Method getMonstersCandiesInformation");

        // Create the Request Queue
        requestQueue = Volley.newRequestQueue(this);

        // Insertion [session_id] into Request
        try {
            jsonBody.put("session_id", userId);
        } catch (JSONException ex) {
            ex.printStackTrace();
            Log.d("MainActivity", "Insert [session_id] failed");
            Toast toast = Toast.makeText(getApplicationContext(), "Insertion failed", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 500);
            toast.show();
        }

        // Get MonsterCandy Information from Server
        JsonObjectRequest getMonstersCandiesInformationRequest = new JsonObjectRequest(
                BASE_URL + MONSTERS_CANDIES_MAP_API,
                jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("MainActivity", "Request done");

                        // Depopulate Model with MonsterCandy Information
                        Model.getInstance().depopulateMonstersCandies();
                        Log.d("MainActivity", "Method depopulateMonstersCandies");

                        // Populate Model with MonsterCandy Information
                        Model.getInstance().populateMonstersCandies(response);
                        Log.d("MainActivity", "Method populateMonstersCandies");

                        // Get MonsterCandy Information from Model
                        monsterscandies = Model.getInstance().getMonstersCandiesList();

                        // Delete Monsters Candies Icon from Map
                        deleteMonstersCandiesMap();

                        // Add Monsters Candies Icon from Map
                        addMonstersCandiesMap();

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("MainActivity", "Request failed");
                        Toast toast = Toast.makeText(getApplicationContext(), "Request failed", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 500);
                        toast.show();
                    }
                }
        );

        // Add the Request to the Request Queue
        requestQueue.add(getMonstersCandiesInformationRequest);

    }

    public void deleteMonstersCandiesMap() {

        Log.d("MainActivity", "Method deleteMonstersCandiesMap");

        for (int i=0; i<monsterscandiesIconMap.size(); i++){
            monsterscandiesIconMap.get(i).deleteAll();
        }

        monsterscandiesIconMap.clear();

    }

    public void addMonstersCandiesMap() {

        Log.d("MainActivity", "Method addMonsterCandiesMap");
        for (int i = 0; i < monsterscandies.size(); i++) {
            final String monstercandyId = monsterscandies.get(i).getMonsterCandyId();
            final Double monstercandyLat = monsterscandies.get(i).getMonsterCandyLat();
            final Double monstercandyLon = monsterscandies.get(i).getMonsterCandyLon();
            final String monstercandyType = monsterscandies.get(i).getMonsterCandyType();
            final String monstercandySize = monsterscandies.get(i).getMonsterCandySize();
            final String monstercandyName = monsterscandies.get(i).getMonsterCandyName();

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

                            break;

                    }

                    break;
            }

            symbolManager.addClickListener(new OnSymbolClickListener() {
                @Override
                public void onAnnotationClick(Symbol symbol) {

                    Intent fighteatIntent = new Intent(getApplicationContext(), PopUp.class);

                    fighteatIntent.putExtra("userId", userId);
                    fighteatIntent.putExtra("userXp", userXp);
                    fighteatIntent.putExtra("userLifepoints", userLifepoints);
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

            // Set Camera Position if the Location Permission is granted
            setCameraPosition();

        } else {

            Log.d("MainActivity", "Location Permission Not Granted");

            Toast toast = Toast.makeText(getApplicationContext(), "Location permission not granted", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 500);
            toast.show();

        }
    }

    public void showUserLocation() {

        Log.d("MainActivity", "Mehtod showUserLocation");
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

    public void setCameraPosition() {

        Log.d("MainActivity", "Method setCameraPosition");

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

        Log.d("MainActivity", "Method getUserInformation");

        // Create the Request Queue
        requestQueue = Volley.newRequestQueue(this);

        // Insertion [session_id] into Request
        try {
            jsonBody.put("session_id", userId);
        } catch (JSONException ex) {
            ex.printStackTrace();
            Log.d("MainActivity", "Insert [session_id] failed");
            Toast toast = Toast.makeText(getApplicationContext(), "Insertion failed", Toast.LENGTH_SHORT);
            toast.show();
        }

        // Get User Information from Server
        JsonObjectRequest getUserInformationRequest = new JsonObjectRequest(
                BASE_URL + USER_PROFILE_INFORMATION_API,
                jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("MainActivity", "Request done");

                        try {

                            // Get / Set User Name
                            userName = response.getString("username");

                            // Set User Name TextView
                            TextView userNameTextView = findViewById(R.id.userNameTextView);
                            userNameTextView.setText(userName);

                            // Get / Set User Image
                            String userImageString = response.getString("img");
                            byte[] decodedString = Base64.decode(userImageString, Base64.DEFAULT);
                            userImage = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                            // Set User Image ImageView
                            ImageView userImageImageView = findViewById(R.id.userImageImageView);
                            userImageImageView.setImageBitmap(userImage);

                            // Get / Set User Xp
                            userXp = response.getInt("xp");

                            // Set User Xp TextView
                            TextView userXpTextView = findViewById(R.id.userXpTextView);
                            userXpTextView.setText(String.valueOf(userXp));

                            // Get / Set User Lifepoints
                            userLifepoints = response.getInt("lp");

                            // Set User Lifepoints ProgressBar
                            ProgressBar userLifepointsProgressBar = findViewById(R.id.userLifepointsProgressBar);
                            userLifepointsProgressBar.setProgress(userLifepoints);

                        } catch (JSONException ex){
                            ex.printStackTrace();
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
        requestQueue.add(getUserInformationRequest);
    }

}
