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
import android.view.WindowManager;
import android.widget.ImageView;
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

    // Monster / Candies ArrayList
    private ArrayList<MonsterCandy> monsterscandies = null;

    // Server Request Queue
    public RequestQueue requestQueue = null;

    // Server Request Constant URL
    public static final String BASE_URL = "https://ewserver.di.unimi.it/mobicomp/mostri/";
    public static final String MONSTERCANDY_IMAGE_API = "getimage.php";
    public static final String FIGHTEAT_API = "fighteat.php";

    // Insertion Request Text
    JSONObject jsonBody;

    public static String SESSION_ID = null;
    public static double USER_LAT;
    public static double USER_LON;

    public static String MONSTERCANDY_ID = null;
    public static double MONSTERCANDY_LAT;
    public static double MONSTERCANDY_LON;
    public static String MONSTERCANDY_TYPE = null;
    public static String MONSTERCANDY_SIZE = null;
    public static String MONSTERCANDY_NAME = null;

    public static int USERMONSTERCANDY_RANGE;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("PopUp", "Method onCreate");
        setContentView(R.layout.activity_pop_up);

        // Back Button
        View backButton = findViewById(R.id.backbuttonIcon);
        backButton.setOnClickListener(objectClickListener);

        // Eat Button
        View fighteatButton = findViewById(R.id.fighteatButton);
        fighteatButton.setOnClickListener(objectClickListener);

        // Get Monsters / Candies Information from Model
        monsterscandies = Model.getInstance().getMonstersCandiesList();

        // Extra Information
        Bundle bundle = getIntent().getExtras();

        SESSION_ID = bundle.getString("session_id");
        USER_LAT = bundle.getDouble("user_lat");
        USER_LON = bundle.getDouble("user_lon");

        MONSTERCANDY_ID = bundle.getString("monstercandy_id");
        MONSTERCANDY_LAT = bundle.getDouble("monstercandy_lat");
        MONSTERCANDY_LON = bundle.getDouble("monstercandy_lon");
        MONSTERCANDY_TYPE = bundle.getString("monstercandy_type");
        MONSTERCANDY_SIZE = bundle.getString("monstercandy_size");
        MONSTERCANDY_NAME = bundle.getString("monstercandy_name");

        getMonsterCandyImage();

        TextView monstercandynameTextView = findViewById(R.id.monstercandynameTextView);
        monstercandynameTextView.setText(MONSTERCANDY_NAME);

        TextView monstercandySizeTextView = findViewById(R.id.monstercandysizeTextView);
        monstercandySizeTextView.setText(MONSTERCANDY_SIZE);

        getUserMonsterCandyRange();

        TextView monstercandyrangeTextView = findViewById(R.id.monstercandyrangeTextView);
        monstercandyrangeTextView.setText(String.valueOf(USERMONSTERCANDY_RANGE));

        ImageView fighteatImageView = findViewById(R.id.fighteatIcon);
        TextView fighteatTextView = findViewById(R.id.fighteat);

        if (MONSTERCANDY_TYPE.equals("MO")) {
            fighteatImageView.setImageResource(R.drawable.monster_icon);
            fighteatTextView.setText("Fight");
        } else if (MONSTERCANDY_TYPE.equals("CA")) {
            fighteatImageView.setImageResource(R.drawable.candy_icon);
            fighteatTextView.setText("Eat");
        }

        getPopUpDefaultSettings();
    }

    public void getMonsterCandyImage() {

        // Create the Request Queue
        requestQueue = Volley.newRequestQueue(this);

        // Session_id + Target_id Insertion into Request
        try {
            jsonBody = new JSONObject("{\"session_id\":\"" + SESSION_ID + "\",\"target_id\":\"" +  MONSTERCANDY_ID +  "\"}");
        } catch (JSONException ex) {
            Log.d("EatPopUp", "Insert session_id and target_id failed");
            Toast toast = Toast.makeText(getApplicationContext(), "Insertion failed", Toast.LENGTH_SHORT);
            toast.show();
        }

        // Get Candies Image from Server
        JsonObjectRequest getMonsterCandyImageRequest = new JsonObjectRequest(
                BASE_URL + MONSTERCANDY_IMAGE_API,
                jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("EatPopUp", "Request done");
                        Log.d("EatPopUp", response.toString());

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
                        Log.d("EatPopUp", "Request failed");
                        Toast toast = Toast.makeText(getApplicationContext(), "Request failed", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }
        );

        // Add the Request to the Request Queue
        requestQueue.add(getMonsterCandyImageRequest);
    }

    public void getUserMonsterCandyRange() {
        Location userLocation = new Location("");
        userLocation.setLatitude(USER_LAT);
        userLocation.setLongitude(USER_LON);
        Log.d("Distance", "" + userLocation);

        Location candyLocation = new Location("");
        candyLocation.setLatitude(MONSTERCANDY_LAT);
        candyLocation.setLongitude(MONSTERCANDY_LON);
        Log.d("Distance", "" + candyLocation);

        USERMONSTERCANDY_RANGE = (int) userLocation.distanceTo(candyLocation);

    }

    public void getPopUpDefaultSettings() {
        // PopUp Default Settings
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;

        getWindow().setLayout((int) (width*0.6106870229), (int) (height*0.4195804196));

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.gravity = Gravity.CENTER;
        params.x = 0;
        params.y = -150;

        getWindow().setAttributes(params);
    }

    public void fighteat() {

        // Session_id + Target_id Insertion into Request
        try {
            jsonBody = new JSONObject("{\"session_id\":\"" + SESSION_ID + "\",\"target_id\":\"" +  MONSTERCANDY_ID +  "\"}");
        } catch (JSONException ex) {
            Log.d("EatPopUp", "Insert session_id failed");
            Toast toast = Toast.makeText(getApplicationContext(), "Insertion failed", Toast.LENGTH_SHORT);
            toast.show();
        }

        // Get Eat from Server
        JsonObjectRequest getEatRequest = new JsonObjectRequest(
                BASE_URL + FIGHTEAT_API,
                jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("EatPopUp", "Request done");
                        Log.d("EatPopUp", response.toString());

                        Intent eatIntent = new Intent(getBaseContext(), ResultPopUp.class);
                        startActivity(eatIntent);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("EatPopUp", "Request failed");
                        Toast toast = Toast.makeText(getApplicationContext(), "Request failed", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }
        );

        // Add the Request to the Request Queue
        requestQueue.add(getEatRequest);

    }

    // Object Click Listener
    private View.OnClickListener objectClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {

                // onClick Back Button
                case R.id.backbuttonIcon:
                    Log.d("PopUp", "Method onClick BackButton");
                    PopUp.super.onBackPressed();
                    break;

                // onClick Eat Button
                case R.id.fighteatButton:
                    Log.d("PopUp", "Method onClick Eat Button");

                    fighteat();

                    PopUp.super.onBackPressed();

                    break;
            }
        }
    };

}
