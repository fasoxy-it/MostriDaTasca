package it.mattiafasoli.mostridatasca;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
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

import java.io.ByteArrayOutputStream;

public class Login extends AppCompatActivity {

    // Server Request Queue
    private RequestQueue requestQueue;

    // Server Request Insertion Text
    JSONObject jsonBody = new JSONObject();

    // Server Request URL
    private static final String BASE_URL = "https://ewserver.di.unimi.it/mobicomp/mostri/";
    private static final String SET_USER_PROFILE_INFORMATION_API="setprofile.php";

    // User Information
    private static String userId;

    private ImageView userImageImageView;

    private static final int IMAGE_PICK_CODE = 1000;
    private static final int PERMISSION_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("Login", "Method onCreate");
        setContentView(R.layout.activity_login);

        // Get Extra Information from previous Activity
        getExtraInformation();

        // Set User Image Button
        View userImageButton = findViewById(R.id.userImageImageView);
        userImageButton.setOnClickListener(objectClickListener);

        // Set Login Button
        View loginButton = findViewById(R.id.loginButton);
        loginButton.setOnClickListener(objectClickListener);

        // Set userImage ImageView
        userImageImageView = findViewById(R.id.userImageImageView);

    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("Login", "Method onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("Login", "Method onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("Login", "Method onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d("Login", "Method onStop");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("Login", "Method onDestroy");
    }

    // Object Click Listener
    private View.OnClickListener objectClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {

                // onClick Set User Image ImageView
                case R.id.userImageImageView:
                    Log.d("Login", "Method onClick setUserImageImageView");

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

                // onClick Register Button
                case R.id.loginButton:
                    Log.d("Login", "Method onClick Login Button");

                    // Set User Name
                    setUserName();

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
                Log.d("Login", "Insert [session_id + img] failed");
                Toast toast = Toast.makeText(getApplicationContext(), "Insertion failed", Toast.LENGTH_SHORT);
                toast.show();
            }

            // Set User Image into Server
            JsonObjectRequest setUserProfileInformationRequest = new JsonObjectRequest(
                    BASE_URL + SET_USER_PROFILE_INFORMATION_API,
                    jsonBody,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            Log.d("Login", "Request done");

                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.d("Login", "Request failed");
                            Toast toast = Toast.makeText(getApplicationContext(), "Request failed", Toast.LENGTH_SHORT);
                            toast.show();
                        }
                    }
            );

            // Add the Request to the Request Queue
            requestQueue.add(setUserProfileInformationRequest);
        }
    }

    public void getExtraInformation() {

        Log.d("Login", "Method getExtraInformation");

        // Get Extra Information from previous Activity
        Bundle bundle = getIntent().getExtras();

        // Get / Set [session_id]
        userId = bundle.getString("session_id");

    }

    public void setUserName() {

        Log.d("Login", "Method setUserName");

        // Get User Name EditText
        EditText userNameEditText = findViewById(R.id.userNameEditText);
        String userName = userNameEditText.getText().toString();

        // Create the Request Queue
        requestQueue = Volley.newRequestQueue(this);

        // Insertion [session_id + username] into Request
        try {
            jsonBody.put("session_id", userId);
            jsonBody.put("username", userName);
        } catch (JSONException ex) {
            Log.d("Login", "Insert [session_id + username] failed");
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
                        Log.d("Login", "Request done");

                        Intent loginIntent = new Intent(getApplicationContext(), MainActivity.class);
                        loginIntent.putExtra("session_id", userId);
                        startActivity(loginIntent);

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Login", "Request failed");
                        Toast toast = Toast.makeText(getApplicationContext(), "Request failed", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }
        );

        // Add the Request to the Request Queue
        requestQueue.add(setUserNameRequest);

    }
}
