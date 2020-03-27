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
    public RequestQueue requestQueue = null;

    // Server Request Constant URL
    public static final String BASE_URL = "https://ewserver.di.unimi.it/mobicomp/mostri/";
    public static final String MONSTERCANDY_IMAGE_API = "getimage.php";
    public static final String FIGHTEAT_API = "fighteat.php";

    // Insertion Request Text
    JSONObject jsonBody;

    // User Information
    public static String userId;
    public static double userLat;
    public static double userLon;
    public static int userXpBefore;
    public static int userLifepointsBefore;
    public static int userXpAfter;
    public static int userLifepointsAfter;
    public static String userDied;

    // Monster/Candy Information
    public static String monstercandyId;
    public static double monstercandyLat;
    public static double monstercandyLon;
    public static String monstercandyType;
    public static String monstercandySize;
    public static String monstercandyName;

    public static int usermonstercandyRange;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("PopUp", "Method onCreate");
        setContentView(R.layout.activity_pop_up);

        // Get PopUp Default Settings
        getPopUpDefaultSettings();

        // Create the Request Queue
        requestQueue = Volley.newRequestQueue(this);

        // Get Extra Information
        getExtraInformation();

        // Close Button
        View closeButton = findViewById(R.id.closeButton);
        closeButton.setOnClickListener(objectClickListener);

        // Fight/Eat Button
        View fighteatButton = findViewById(R.id.fighteatButton);
        fighteatButton.setOnClickListener(objectClickListener);

        ImageView eatImageView = findViewById(R.id.eatIcon);
        ImageView fightImageView = findViewById(R.id.fightIcon);

        if (monstercandyType.equals("MO")) {
            fightImageView.setVisibility(View.VISIBLE);
        } else if (monstercandyType.equals("CA")) {
            eatImageView.setVisibility(View.VISIBLE);
        }

        // MonsterCandy Name TextView
        TextView monstercandynameTextView = findViewById(R.id.monstercandynameTextView);
        monstercandynameTextView.setText(monstercandyName);

        // MonsterCandy Size TextView
        TextView monstercandySizeTextView = findViewById(R.id.monstercandysizeTextView);
        monstercandySizeTextView.setText(monstercandySize);

        // MonsterCandy Range TextView
        TextView monstercandyrangeTextView = findViewById(R.id.monstercandyrangeTextView);
        getUserMonsterCandyRange();
        monstercandyrangeTextView.setText(String.valueOf(usermonstercandyRange));
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
                    Log.d("PopUp", "Method onClick BackButton");

                    PopUp.super.onBackPressed();

                    break;

                // onClick FightEat Button
                case R.id.fighteatButton:
                    Log.d("PopUp", "Method onClick Eat Button");

                    if (usermonstercandyRange > 50) {
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

    public void getExtraInformation() {

        // Extra Information
        Bundle bundle = getIntent().getExtras();

        // User Information
        userId = bundle.getString("userId");
        userLat = bundle.getDouble("userLat");
        userLon = bundle.getDouble("userLon");
        userXpBefore = bundle.getInt("userXp");
        userLifepointsBefore = bundle.getInt("userLifepoints");

        // MonsterCandy Information
        monstercandyId = bundle.getString("monstercandyId");
        monstercandyLat = bundle.getDouble("monstercandyLat");
        monstercandyLon = bundle.getDouble("monstercandyLon");
        monstercandyType = bundle.getString("monstercandyType");
        monstercandySize = bundle.getString("monstercandySize");
        monstercandyName = bundle.getString("monstercandyName");

        getMonsterCandyImage();

    }

    public void getMonsterCandyImage() {

        // Insertion [session_id + target_id] into Request
        try {
            jsonBody = new JSONObject("{\"session_id\":\"" + userId + "\",\"target_id\":\"" +  monstercandyId +  "\"}");
        } catch (JSONException ex) {
            Log.d("PopUp", "Insert [session_id and target_id] failed");
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

                            // Set Candy Image
                            String monstercandyImageString = response.getString("img");
                            ImageView monstercandyImageView = findViewById(R.id.monstercandyImageView);
                            byte[] decodedString = Base64.decode(monstercandyImageString, Base64.DEFAULT);
                            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                            monstercandyImageView.setImageBitmap(decodedByte);

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

    public void getUserMonsterCandyRange() {

        // User Location
        Location userLocation = new Location("");
        userLocation.setLatitude(userLat);
        userLocation.setLongitude(userLon);

        // MonsterCandy Location
        Location monstercandyLocation = new Location("");
        monstercandyLocation.setLatitude(monstercandyLat);
        monstercandyLocation.setLongitude(monstercandyLon);

        usermonstercandyRange = (int) userLocation.distanceTo(monstercandyLocation);

    }

    public void getPopUpDefaultSettings() {

        // PopUp Default Settings
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

    public void fighteat() {

        // Insertion [session_id + target_id] into Request
        try {
            jsonBody = new JSONObject("{\"session_id\":\"" + userId + "\",\"target_id\":\"" +  monstercandyId +  "\"}");
        } catch (JSONException ex) {
            Log.d("EatPopUp", "Insert session_id failed");
            Toast toast = Toast.makeText(getApplicationContext(), "Insertion failed", Toast.LENGTH_SHORT);
            toast.show();
        }

        // Get fighteat from Server
        JsonObjectRequest getFightEatRequest = new JsonObjectRequest(
                BASE_URL + FIGHTEAT_API,
                jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("PopUp", "Request done");

                        try {
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
