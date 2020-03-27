package it.mattiafasoli.mostridatasca;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;

public class Profile extends AppCompatActivity {

    // Server Request Queue
    public RequestQueue requestQueue = null;

    // Server Request Constant URL
    public static final String BASE_URL = "https://ewserver.di.unimi.it/mobicomp/mostri/";
    public static final String USER_PROFILE_INFORMATION_API = "getprofile.php";
    public static final String SET_USER_PROFILE_INFORMATION_API="setprofile.php";

    // Insertion Request Text
    JSONObject jsonBody;
    public static String SESSION_ID = null;

    ImageView userImageView;

    private static final int IMAGE_PICK_CODE = 1000;
    private static final int PERMISSION_CODE = 1001;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Extra Information
        Bundle bundle = getIntent().getExtras();

        // Get SESSION_ID
        SESSION_ID = bundle.getString("session_id");
        Log.d("Profile", "SESSION_ID: " +  SESSION_ID);

        // Modify UserName objectClickListener
        View modifyUserName = findViewById(R.id.modifyUserNameIcon);
        modifyUserName.setOnClickListener(objectClickListener);

        // Modify UserImage objectClickListener
        View modifyUserImage = findViewById(R.id.modifyImageIcon);
        modifyUserImage.setOnClickListener(objectClickListener);

        // Create the Request Queue
        requestQueue = Volley.newRequestQueue(this);

        // Session_id Insertion into Request
        try {
            jsonBody = new JSONObject("{\"session_id\":\"" + SESSION_ID + "\"}");
        } catch (JSONException ex) {
            Log.d("Profile", "Insert session_id failed");
            Toast toast = Toast.makeText(getApplicationContext(), "Insertion failed", Toast.LENGTH_SHORT);
            toast.show();
        }

        // Get User Profile Information from Server
        JsonObjectRequest getUserProfileInformationRequest = new JsonObjectRequest(
                BASE_URL + USER_PROFILE_INFORMATION_API,
                jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("Profile", "Request get profile done");
                        Log.d("Profile", response.toString());

                        try {

                            String username = response.getString("username");
                            TextView usernameTextView = findViewById(R.id.usernameTextView);
                            usernameTextView.setText(username);
                            Log.d("Profile", username);

                            String img = response.getString("img");
                            userImageView = findViewById(R.id.userImageView);
                            byte[] decodedString = Base64.decode(img, Base64.DEFAULT);
                            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                            userImageView.setImageBitmap(decodedByte);
                            Log.d("Profile", img);

                            String xp = response.getString("xp");
                            TextView xpTextView = findViewById(R.id.xpTextView);
                            xpTextView.setText(xp);
                            Log.d("Profile", xp);

                            int lp = response.getInt("lp");
                            ProgressBar lifepointsProgressBar = findViewById(R.id.lifepointsProgressBar);
                            lifepointsProgressBar.setProgress(lp);
                            Log.d("Profile", "" + lp);

                        } catch (JSONException e){
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Profile", "Request failed");
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
                case R.id.modifyUserNameIcon:
                    Log.d("Profile", "Method onClick Modify UserName");

                    TextView userNameTW = findViewById(R.id.usernameTextView);
                    String userName = userNameTW.getText().toString();

                    Log.d("Profile", "username" + userName);

                    try {
                        jsonBody.put("username", userName);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                    // Session_id Insertion into Request
                    try {
                        jsonBody = new JSONObject("{\"session_id\":\""+ SESSION_ID +"\",\"username\":\"" +  userName +  "\"}");
                    } catch (JSONException ex) {
                        Log.d("Profile", "Insert session_id failed");
                        Toast toast = Toast.makeText(getApplicationContext(), "Insertion failed", Toast.LENGTH_SHORT);
                        toast.show();
                    }

                    // Get User Profile Information from Server
                    JsonObjectRequest setUserProfileInformationRequest = new JsonObjectRequest(
                            BASE_URL + SET_USER_PROFILE_INFORMATION_API,
                            jsonBody,
                            new Response.Listener<JSONObject>() {
                                @Override
                                public void onResponse(JSONObject response) {
                                    Log.d("Profile", "Request set profile done");
                                    Log.d("Profile", response.toString());

                                }
                            },
                            new Response.ErrorListener() {
                                @Override
                                public void onErrorResponse(VolleyError error) {
                                    Log.d("Profile", "Request failed");
                                    Toast toast = Toast.makeText(getApplicationContext(), "Request failed", Toast.LENGTH_SHORT);
                                    toast.show();
                                }
                            }
                    );

                    // Add the Request to the Request Queue
                    requestQueue.add(setUserProfileInformationRequest);

                    break;
                case R.id.modifyImageIcon:
                    Log.d("Profile", "Method onClick Modify UserImage");

                    //check runtime permission
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                                == PackageManager.PERMISSION_DENIED){
                            //permission not granted so request it
                            String[] permission = {Manifest.permission.READ_EXTERNAL_STORAGE};
                            //show popup for runtime permission
                            requestPermissions(permission, PERMISSION_CODE);
                        }
                        else {
                            //permission already granted
                            pickImageFromGallery();
                        }
                    }
                    else {
                        //system os is less then marshallow
                        pickImageFromGallery();
                    }

                    break;
            }
        }
    };

    private void pickImageFromGallery() {
        //intent to pick image
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_CODE);
    }

    //handle result of runtime permission


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case PERMISSION_CODE:{
                if (grantResults.length > 0 && grantResults[0] ==
                        PackageManager.PERMISSION_GRANTED) {
                    //permission was granted
                    pickImageFromGallery();
                }
                else {
                    //permission was denied
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();               }
            }
        }
    }

    //handle result of picked image

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == IMAGE_PICK_CODE) {
            //set image to image view
            userImageView.setImageURI(data.getData());

            userImageView.buildDrawingCache();
            Bitmap bitmap = userImageView.getDrawingCache();

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
            byte[] byteformat = stream.toByteArray();
            //get the base64 string
            String encodedString = Base64.encodeToString(byteformat, Base64.NO_WRAP);

            Log.d("user", "encode: " + encodedString);


            try {
                jsonBody.put("img", encodedString);
            } catch (JSONException e) {
                e.printStackTrace();
            }

            // Session_id Insertion into Request
            try {
                jsonBody = new JSONObject("{\"session_id\":\""+ SESSION_ID +"\",\"img\":\"" +  encodedString +  "\"}");
            } catch (JSONException ex) {
                Log.d("Profile", "Insert img failed");
                Toast toast = Toast.makeText(getApplicationContext(), "Insertion failed", Toast.LENGTH_SHORT);
                toast.show();
            }

            // Get User Profile Information from Server
            JsonObjectRequest setUserProfileInformationRequest = new JsonObjectRequest(
                    BASE_URL + SET_USER_PROFILE_INFORMATION_API,
                    jsonBody,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d("Profile", "Request set profile done");
                            Log.d("Profile", response.toString());

                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("Profile", "Request failed");
                            Toast toast = Toast.makeText(getApplicationContext(), "Request failed", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    }
            );

            // Add the Request to the Request Queue
            requestQueue.add(setUserProfileInformationRequest);
        }
    }

}