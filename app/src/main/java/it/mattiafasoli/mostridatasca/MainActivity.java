package it.mattiafasoli.mostridatasca;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.Bundle;
import android.util.Base64;
import android.util.DisplayMetrics;
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

    // Timer Update MonsterCandy Information
    Timer timer = new Timer();

    // Layout Information
    private int width;
    private int height;

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

        // Get MainActivity Layout
        getMainActivityLayout();

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
                    userInformationIntent.putExtra("session_id", userId);
                    startActivity(userInformationIntent);

                    break;

                // onClick Ranking Information View
                case R.id.rankingInformation:
                    Log.d("MainActivity", "Method onClick rankingInformation");

                    Intent rankingInformationIntent = new Intent(getBaseContext(), Ranking.class);
                    rankingInformationIntent.putExtra("session_id", userId);
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

            if (width == 1080 && height == 2028){
                toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 500);
            } else {
                toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 300);
            }

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

                        if (width == 1080 && height == 2028){
                            toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 500);
                        } else {
                            toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 300);
                        }

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

                    fighteatIntent.putExtra("session_id", userId);
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

            if (width == 1080 && height == 2028){
                toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 500);
            } else {
                toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 300);
            }

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
    public void onExplanationNeeded(List<String> permissionsToExplain) {}

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

            if (width == 1080 && height == 2028){
                toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 500);
            } else {
                toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 300);
            }

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

                            if (userImageString != "null") {
                                byte[] decodedString = Base64.decode(userImageString, Base64.DEFAULT);
                                userImage = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                            } else {
                                byte[] decodedString = Base64.decode("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", Base64.DEFAULT);
                                userImage = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                            }

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

                        if (width == 1080 && height == 2028){
                            toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 500);
                        } else {
                            toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 300);
                        }

                        toast.show();
                    }
                }

        );

        // Add the Request to the Request Queue
        requestQueue.add(getUserInformationRequest);
    }

    public void getMainActivityLayout() {

        Log.d("MainActivity", "Method getMainActivityLayout");

        // Get MainActivity Layout
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        width = displayMetrics.widthPixels;
        height = displayMetrics.heightPixels;

    }
}
