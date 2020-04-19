package it.mattiafasoli.mostridatasca;

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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

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
    private RequestQueue requestQueue;

    // Server Request Insertion Text
    JSONObject jsonBody = new JSONObject();

    // Server Request URL
    private static final String BASE_URL = "https://ewserver.di.unimi.it/mobicomp/mostri/";
    private static final String GET_USER_PROFILE_INFORMATION_API = "getprofile.php";
    private static final String SET_USER_PROFILE_INFORMATION_API="setprofile.php";

    // User Information
    private static String userId;
    private static Bitmap userImage;
    private static String userName;
    private static int userXp;
    private static int userLifepoints;

    ImageView userImageImageView;
    EditText userNameEditText;

    private static final int IMAGE_PICK_CODE = 1000;
    private static final int PERMISSION_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Get Extra Information from previous Activity
        getExtraInformation();

        // Set User Information
        getUserInformation();

        // Set User Name Modify Button
        View userNameModifyButton = findViewById(R.id.userNameModifyButton);
        userNameModifyButton.setOnClickListener(objectClickListener);

        // Set User Image Modify Button
        View userImageModifyButton = findViewById(R.id.userImageImageView);
        userImageModifyButton.setOnClickListener(objectClickListener);

    }

    // Object Click Listener
    private View.OnClickListener objectClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {

                // onClick Modify User Name Button
                case R.id.userNameModifyButton:
                    Log.d("MainActivity", "Method onClick userNameModifyButton");

                    setUserName();

                    break;

                // onClick Modify User Image Button
                case R.id.userImageImageView:
                    Log.d("MainActivity", "Method onClick userImageModifyButton");

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

    public void getExtraInformation() {

        Log.d("Profile", "Method getExtraInformation");

        // Get Extra Information from previous Activity
        Bundle bundle = getIntent().getExtras();

        // Get / Set User Information from previous Activity
        userId = bundle.getString("userId");

    }

    public void getUserInformation() {

        Log.d("Profile", "Method getUserInformation");

        // Create the Request Queue
        requestQueue = Volley.newRequestQueue(this);

        // Insertion [session_id] into Request
        try {
            jsonBody.put("session_id", userId);
        } catch (JSONException ex) {
            ex.printStackTrace();
            Log.d("Profile", "Insert [session_id] failed");
            Toast toast = Toast.makeText(getApplicationContext(), "Insertion failed", Toast.LENGTH_SHORT);
            toast.show();
        }

        // Get User Information from Server
        JsonObjectRequest getUserInformationRequest = new JsonObjectRequest(
                BASE_URL + GET_USER_PROFILE_INFORMATION_API,
                jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("Profile", "Request done");

                        try {

                            // Get / Set User Name
                            userName = response.getString("username");

                            // Set User Name EditText
                            userNameEditText = findViewById(R.id.userNameEditText);
                            userNameEditText.setText(userName);

                            // Get / Set User Image
                            String userImageString = response.getString("img");
                            byte[] decodedString = Base64.decode(userImageString, Base64.DEFAULT);
                            userImage = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

                            // Set User Image ImageView
                            userImageImageView = findViewById(R.id.userImageImageView);
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

    public void setUserName() {

        Log.d("Profile", "Method setUserName");

        // Get User Name EditText
        userNameEditText = findViewById(R.id.userNameEditText);
        userName = userNameEditText.getText().toString();

        // Create the Request Queue
        requestQueue = Volley.newRequestQueue(this);

        // Insertion [session_id + username] into Request
        try {
            jsonBody.put("session_id", userId);
            jsonBody.put("username", userName);
        } catch (JSONException ex) {
            ex.printStackTrace();
            Log.d("Profile", "Insert [session_id + username] failed");
            Toast toast = Toast.makeText(getApplicationContext(), "Insertion failed", Toast.LENGTH_SHORT);
            toast.show();
        }

        // Set User Name into Server
        JsonObjectRequest setUserNameRequest = new JsonObjectRequest(
                BASE_URL + SET_USER_PROFILE_INFORMATION_API,
                jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("Profile", "Request done");
                        Toast toast = Toast.makeText(getApplicationContext(), "User Name changed", Toast.LENGTH_SHORT);
                        toast.show();

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
        requestQueue.add(setUserNameRequest);

    }

    private void pickImageFromGallery() {

        Log.d("Profile", "Method pickImageFromGallery");

        //intent to pick image
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_CODE);
    }

    //handle result of runtime permission


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (grantResults.length > 0 && grantResults[0] ==
                PackageManager.PERMISSION_GRANTED) {
            //permission was granted
            pickImageFromGallery();
        }
        else {
            //permission was denied
            Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
        }
    }

    //handle result of picked image

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == IMAGE_PICK_CODE) {

            // Set User Image ImageView
            userImageImageView.setImageURI(data.getData());

            userImageImageView.buildDrawingCache();
            Bitmap userImage = userImageImageView.getDrawingCache();

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            userImage.compress(Bitmap.CompressFormat.JPEG, 70, stream);
            byte[] byteformat = stream.toByteArray();

            // Get [base64] User Image String
            String encodedString = Base64.encodeToString(byteformat, Base64.NO_WRAP);

            // Create the Request Queue
            requestQueue = Volley.newRequestQueue(this);

            // Insertion [session_id + img] into Request
            try {
                jsonBody.put("session_id", userId);
                jsonBody.put("img", encodedString);
            } catch (JSONException ex) {
                Log.d("Profile", "Insert [session_id + img] failed");
                Toast toast = Toast.makeText(getApplicationContext(), "Insertion failed", Toast.LENGTH_SHORT);
                toast.show();
            }

            // Set User Image from Server
            JsonObjectRequest setUserImageRequest = new JsonObjectRequest(
                    BASE_URL + SET_USER_PROFILE_INFORMATION_API,
                    jsonBody,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d("Profile", "Request done");
                            Toast toast = Toast.makeText(getApplicationContext(), "User Image changed", Toast.LENGTH_SHORT);
                            toast.show();

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
            requestQueue.add(setUserImageRequest);
        }
    }




}