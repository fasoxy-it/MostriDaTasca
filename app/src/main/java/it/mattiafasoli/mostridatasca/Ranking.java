package it.mattiafasoli.mostridatasca;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

public class Ranking extends AppCompatActivity {

    // Server Request Queue
    private RequestQueue requestQueue;

    // Server Request Insertion Text
    JSONObject jsonBody = new JSONObject();

    // Server Request Constant URL
    private static final String BASE_URL = "https://ewserver.di.unimi.it/mobicomp/mostri/";
    private static final String USERS_RANKING_API = "ranking.php";

    // User Information
    private static String userId;

    // RecyclerView
    private RecyclerView recyclerView;
    private Adapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("Ranking", "Method onCreate");
        setContentView(R.layout.activity_ranking);

        // Set RecyclerView
        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Set Adapter
        adapter = new Adapter(this,this, Model.getInstance().getUsersList());
        recyclerView.setAdapter(adapter);

        // Get Extra Information from previous Activity
        getExtraInformation();

        // Get Users Information
        getUsersInformation();

    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("Ranking", "Method onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("Ranking", "Method onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("Ranking", "Method onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d("Ranking", "Method onStop");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("Ranking", "Method onDestroy");
    }

    public void getExtraInformation() {

        Log.d("Ranking", "Method getExtraInformation");

        // Get Extra Information from previous Activity
        Bundle bundle = getIntent().getExtras();

        // Get / Set User Information from previous Activity
        userId = bundle.getString("userId");

    }

    public void getUsersInformation() {

        Log.d("Ranking", "Method getUsersInformation");

        // Create the Request Queue
        requestQueue = Volley.newRequestQueue(this);

        // Insertion [session_id] into Request
        try {
            jsonBody.put("session_id", userId);
        } catch (JSONException ex) {
            ex.printStackTrace();
            Log.d("Ranking", "Insert [session_id] failed");
            Toast toast = Toast.makeText(getApplicationContext(), "Insertion failed", Toast.LENGTH_SHORT);
            toast.show();
        }

        // Get Users Information from Server
        JsonObjectRequest getUsersRankingRequest = new JsonObjectRequest(
                BASE_URL + USERS_RANKING_API,
                jsonBody,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("Ranking", "Request done");

                        // Depopulate Model
                        Model.getInstance().depopulateUsers();
                        Log.d("Ranking", "Method depopulateUsers");


                        // Populate Model
                        Model.getInstance().populateUsers(response);
                        Log.d("Ranking", "Method populateUsers");


                        // Update Adapter
                        adapter.notifyDataSetChanged();

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Log.d("Ranking", "Request failed");
                        Toast toast = Toast.makeText(getApplicationContext(), "Request failed", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                }
        );

        // Add the Request to the Request Queue
        requestQueue.add(getUsersRankingRequest);
    }
}