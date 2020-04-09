package it.mattiafasoli.mostridatasca;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
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
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class PopUp extends Activity {

    // Server Request Queue
    private RequestQueue requestQueue;

    // Server Request Insertion Text
    JSONObject jsonBody = new JSONObject();

    // Server Request URL
    private static final String BASE_URL = "https://ewserver.di.unimi.it/mobicomp/mostri/";
    private static final String MONSTERCANDY_IMAGE_API = "getimage.php";
    private static final String FIGHTEAT_API = "fighteat.php";

    // User Information
    private static String userId;
    private static double userLat;
    private static double userLon;
    private static int userXpBefore;
    private static int userLifepointsBefore;
    private static int userXpAfter;
    private static int userLifepointsAfter;

    private static String userDied;

    // MonsterCandy Information
    private static String monstercandyId;
    private static double monstercandyLat;
    private static double monstercandyLon;
    private static String monstercandyType;
    private static String monstercandySize;
    private static String monstercandyName;

    private static Bitmap monstercandyImage;

    private static int monstercandyRange;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("PopUp", "Method onCreate");
        setContentView(R.layout.activity_pop_up);

        // Get PopUp Layout
        getPopUpLayout();

        // Get Extra Information from previous Activity
        getExtraInformation();

        // Set PopUp Information
        setPopUpInformation();

        // Set Close Button
        View closeButton = findViewById(R.id.closeButton);
        closeButton.setOnClickListener(objectClickListener);

        // Set Fight/Eat Button
        View fighteatButton = findViewById(R.id.fighteatButton);
        fighteatButton.setOnClickListener(objectClickListener);

        ImageView fightImageView = findViewById(R.id.fightImageView);
        ImageView eatImageView = findViewById(R.id.eatImageView);

        if (monstercandyType.equals("MO")) {
            fightImageView.setVisibility(View.VISIBLE);
        } else if (monstercandyType.equals("CA")) {
            eatImageView.setVisibility(View.VISIBLE);
        }

    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("PopUp", "Method onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("PopUp", "Method onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("PopUp", "Method onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d("PopUp", "Method onStop");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("PopUp", "Method onDestroy");
    }

    // Object Click Listener
    private View.OnClickListener objectClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {

                // onClick Close Button
                case R.id.closeButton:
                    Log.d("PopUp", "Method onClick CloseButton");
                    PopUp.super.onBackPressed();
                    break;

                // onClick FightEat Button
                case R.id.fighteatButton:
                    Log.d("PopUp", "Method onClick FightEat Button");

                    if (monstercandyRange > 50) {
                        Toast toast = Toast.makeText(getApplicationContext(), "Too far from the target", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER_VERTICAL|Gravity.CENTER_HORIZONTAL, 0, 500);
                        toast.show();
                    } else {
                        fighteat();
                        PopUp.super.onBackPressed();
                    }

                    break;

            }
        }
    };

    public void getPopUpLayout() {

        Log.d("PopUp", "Method getPopUpLayout");

        // Get PopUp Layout
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;

        getWindow().setLayout((int) (width*0.6106870229), (int) (height*0.406504065));

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.gravity = Gravity.CENTER;
        params.x = 0;
        params.y = -150;

        getWindow().setAttributes(params);

    }

    public void getExtraInformation() {

        Log.d("PopUp", "Method getExtraInformation");

        // Get Extra Information from previous Activity
        Bundle bundle = getIntent().getExtras();

        // Get / Set User Information from previous Activity
        userId = bundle.getString("userId");
        userLat = bundle.getDouble("userLat");
        userLon = bundle.getDouble("userLon");
        userXpBefore = bundle.getInt("userXp");
        userLifepointsBefore = bundle.getInt("userLifepoints");

        // Get / Set MonsterCandy Information from previous Activity
        monstercandyId = bundle.getString("monstercandyId");
        monstercandyLat = bundle.getDouble("monstercandyLat");
        monstercandyLon = bundle.getDouble("monstercandyLon");
        monstercandyType = bundle.getString("monstercandyType");
        monstercandySize = bundle.getString("monstercandySize");
        monstercandyName = bundle.getString("monstercandyName");

    }

    public void getMonsterCandyRange() {

        Log.d("PopUp", "Method getMonsterCandyRange");

        // Set User Location
        Location userLocation = new Location("");
        userLocation.setLatitude(userLat);
        userLocation.setLongitude(userLon);

        // Set MonsterCandy Location
        Location monstercandyLocation = new Location("");
        monstercandyLocation.setLatitude(monstercandyLat);
        monstercandyLocation.setLongitude(monstercandyLon);

        // Set MonsterCandy Range
        monstercandyRange = (int) userLocation.distanceTo(monstercandyLocation);

        // Set MonsterCandy Range TextView
        TextView monstercandyRangeTextView = findViewById(R.id.monstercandyrangeTextView);
        monstercandyRangeTextView.setText(String.valueOf(monstercandyRange));

    }

    public void getMonsterCandyImage() {

        Log.d("PopUp", "Method getMonsterCandyImage");

        // Create the Request Queue
        requestQueue = Volley.newRequestQueue(this);

        // Insertion [session_id + target_id] into Request
        try {
            jsonBody.put("session_id", userId);
            jsonBody.put("target_id", monstercandyId);
        } catch (JSONException ex) {
            ex.printStackTrace();
            Log.d("PopUp", "Insert [session_id + target_id] failed");
            Toast toast = Toast.makeText(getApplicationContext(), "Insertion failed", Toast.LENGTH_SHORT);
            toast.show();
        }

        // Get MonsterCandy Image from Server
        JsonObjectRequest getMonsterCandyImageRequest = new JsonObjectRequest(
                BASE_URL + MONSTERCANDY_IMAGE_API,
                jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("PopUp", "Request done");

                        try {

                            // Set MonsterCandy Image
                            String monstercandyImageString = response.getString("img");
                            byte[] decodedString = Base64.decode(monstercandyImageString, Base64.DEFAULT);
                            monstercandyImage = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                            // Set MonsterCandy Image ImageView
                            ImageView monstercandyImageImageView = findViewById(R.id.monstercandyImageView);
                            monstercandyImageImageView.setImageBitmap(monstercandyImage);

                        } catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("PopUp", "Request failed");
                        Toast toast = Toast.makeText(getApplicationContext(), "Request failed", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }
        );

        // Add the Request to the Request Queue
        requestQueue.add(getMonsterCandyImageRequest);
    }

    public void setPopUpInformation() {

        Log.d("PopUp", "Method setPopUpInformation");

        // Set MonsterCandy Image ImageView
        getMonsterCandyImage();

        // Set MonsterCandy Name TextView
        TextView monstercandyNameTextView = findViewById(R.id.monstercandynameTextView);
        monstercandyNameTextView.setText(monstercandyName);

        // Set MonsterCandy Size TextView
        TextView monstercandySizeTextView = findViewById(R.id.monstercandysizeTextView);
        monstercandySizeTextView.setText(monstercandySize);

        // Set MonsterCandy Range TextView
        getMonsterCandyRange();

    }

    public void fighteat() {

        Log.d("PopUp", "Method fighteat");

        // Create the Request Queue
        requestQueue = Volley.newRequestQueue(this);

        // Insertion [session_id + target_id] into Request
        try {
            jsonBody.put("session_id", userId);
            jsonBody.put("target_id", monstercandyId);
        } catch (JSONException ex) {
            Log.d("PopUp", "Insert [session_id + target_id] failed");
            Toast toast = Toast.makeText(getApplicationContext(), "Insertion failed", Toast.LENGTH_SHORT);
            toast.show();
        }

        // Get [fighteat] from Server
        JsonObjectRequest getFightEatRequest = new JsonObjectRequest(
                BASE_URL + FIGHTEAT_API,
                jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("PopUp", "Request done");

                        try {

                            // Set [fighteat] Result
                            userDied = response.getString("died");
                            userXpAfter = response.getInt("xp");
                            userLifepointsAfter = response.getInt("lp");

                            Intent fighteatIntent = new Intent(getApplicationContext(), ResultPopUp.class);

                            fighteatIntent.putExtra("userId", userId);
                            fighteatIntent.putExtra("userXpBefore", userXpBefore);
                            fighteatIntent.putExtra("userLifepointsBefore", userLifepointsBefore);
                            fighteatIntent.putExtra("userXpAfter", userXpAfter);
                            fighteatIntent.putExtra("userLifepointsAfter", userLifepointsAfter);
                            fighteatIntent.putExtra("userDied", userDied);
                            fighteatIntent.putExtra("monstercandyType", monstercandyType);

                            startActivity(fighteatIntent);

                        } catch (JSONException e){
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("PopUp", "Request failed");
                        Toast toast = Toast.makeText(getApplicationContext(), "Request failed", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }
        );

        // Add the Request to the Request Queue
        requestQueue.add(getFightEatRequest);

    }

}
