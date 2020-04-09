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

    // SESSION_ID
    public static String userId;
    public static String userNameBefore;
    public static String userNameAfter;
    public static String userXp;
    public static int userLifepoints;
    public static ImageView userImage;

    private static final int IMAGE_PICK_CODE = 1000;
    private static final int PERMISSION_CODE = 1001;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Get Extra Information
        Bundle bundle = getIntent().getExtras();

        userId = bundle.getString("session_id");

        // Create the Request Queue
        requestQueue = Volley.newRequestQueue(this);

        // Get User Information
        getUserInformation();

        // User Image Modify Button
        View userImageModifyButton = findViewById(R.id.userImageModifyImageView);
        userImageModifyButton.setOnClickListener(objectClickListener);

        // User Name Modify Button
        View userNameModifyButton = findViewById(R.id.imageButton);
        userNameModifyButton.setOnClickListener(objectClickListener);


    }

    // Object Click Listener
    private View.OnClickListener objectClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {

                // onClick userImageModifyButton
                case R.id.userImageModifyImageView:
                    Log.d("Profile", "Method onClick Modify UserName");

                    TextView userNameTextView = findViewById(R.id.userName);
                    String userNameString = userNameTextView.getText().toString();

                    Log.d("Profile", "username" + userNameString);

                    try {
                        jsonBody.put("session_id", userId);
                        jsonBody.put("username", userNameString);
                    } catch (JSONException ex) {
                        ex.printStackTrace();
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
                case R.id.imageButton:
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
                            getImageFromGallery();
                        }
                    }
                    else {
                        //system os is less then marshallow
                        getImageFromGallery();
                    }

                    break;
            }
        }
    };

    public void getUserInformation() {

        // Insert [session_id] into Server Request
        try {
            jsonBody.put("session_id", userId);
        } catch (JSONException ex) {
            ex.printStackTrace();
            Log.d("Profile", "Insert [session_id] failed");
            Toast toast = Toast.makeText(getApplicationContext(), "Insertion failed", Toast.LENGTH_SHORT);
            toast.show();
        }

        // Get User Profile Information
        JsonObjectRequest getUserProfileInformationRequest = new JsonObjectRequest(
                BASE_URL + USER_PROFILE_INFORMATION_API,
                jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("Profile", "Request done");

                        try {

                            // User Information

                            // User Name
                            userNameBefore = response.getString("username");
                            TextView userNameTextView = findViewById(R.id.userNameTextView);
                            userNameTextView.setText(userNameBefore);

                            // User Image
                            String userImageString = response.getString("img");
                            ImageView userImageView = findViewById(R.id.userImageView);
                            byte[] decodedString = Base64.decode(userImageString, Base64.DEFAULT);
                            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
                            userImageView.setImageBitmap(decodedByte);

                            // User Xp
                            userXp = response.getString("xp");
                            TextView userXpTextView = findViewById(R.id.userXpTextView);
                            userXpTextView.setText(userXp);

                            // User Lifepoints
                            userLifepoints = response.getInt("lp");
                            ProgressBar userLifepointsProgressBar = findViewById(R.id.userLifepointsProgressBar);
                            userLifepointsProgressBar.setProgress(userLifepoints);

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

    private void getImageFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, IMAGE_PICK_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_CODE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getImageFromGallery();
                } else {
                    Toast toast = Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT);
                    toast.show();
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && requestCode == IMAGE_PICK_CODE) {

            userImage.setImageURI(data.getData());

            userImage.buildDrawingCache();

            Bitmap bitmap = userImage.getDrawingCache();

            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 70, stream);
            byte[] byteformat = stream.toByteArray();

            String encodedString = Base64.encodeToString(byteformat, Base64.NO_WRAP);

            try {
                jsonBody.put("session_id", userId);
                jsonBody.put("img", encodedString);
            } catch (JSONException ex) {
                ex.printStackTrace();
                Log.d("Profile", "Insertion failed");
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
                            Log.d("Profile", "Request done");

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