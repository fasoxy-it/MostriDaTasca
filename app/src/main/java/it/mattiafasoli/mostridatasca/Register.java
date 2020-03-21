package it.mattiafasoli.mostridatasca;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class Register extends AppCompatActivity {

    // Server Request Queue
    public RequestQueue requestQueue = null;

    // Server Request URL
    public static final String BASE_URL = "https://ewserver.di.unimi.it/mobicomp/mostri/";
    public static final String REGISTER_API = "register.php";

    // Insertion Request Text
    JSONObject jsonBody;

    // Shared Preference Variables
    public static final String SHARED_PREFERENCES = "sharedPreference";
    public static final String SESSION_ID = "SESSION_ID";
    public static String session_id = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("Register", "Method onCreate");
        setContentView(R.layout.activity_register);

        loadData();

        if (session_id == "") {

            // Register Button
            View registerButton = findViewById(R.id.registerButton);
            registerButton.setOnClickListener(objectClickListener);

            // Create the Request Queue
            requestQueue = Volley.newRequestQueue(this);

        } else {

            Log.d("Register", "SESSION_ID: " + session_id);

            Intent mainActivityIntent = new Intent(getApplicationContext(), MainActivity.class);
            mainActivityIntent.putExtra("session_id", session_id);
            startActivity(mainActivityIntent);

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
    public void onLowMemory() {
        super.onLowMemory();
        Log.d("Register", "Method onLowMemory");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("Register", "Method onDestroy");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.d("Register", "Method onSavedInstanceState");
    }

    // Object Click Listener
    private View.OnClickListener objectClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {

                // onClick Register Button
                case R.id.registerButton:
                    Log.d("Register", "Method onClick RegisterButton");

                    // Set Register to Server
                    JsonObjectRequest register = new JsonObjectRequest(
                            BASE_URL + REGISTER_API,
                            jsonBody,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {

                                    Log.d("Register", "Request done");

                                    try {
                                        session_id = response.getString("session_id");

                                        saveData();

                                        loadData();

                                        Log.d("Register", "SESSION_ID: " + session_id);

                                        Intent mainActivityIntent = new Intent(getApplicationContext(), MainActivity.class);
                                        mainActivityIntent.putExtra("session_id", session_id);
                                        startActivity(mainActivityIntent);

                                    } catch (JSONException e){
                                        e.printStackTrace();
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
                    requestQueue.add(register);

                    break;
            }
        }
    };

    public void loadData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
        session_id = sharedPreferences.getString(SESSION_ID, "");
    }

    public void saveData(){
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFERENCES, MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        editor.putString(SESSION_ID, session_id);
        editor.apply();
    }
}
