package it.mattiafasoli.mostridatasca;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
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
    public RequestQueue requestQueue = null;

    // Server Request Constant URL
    public static final String BASE_URL = "https://ewserver.di.unimi.it/mobicomp/mostri/";
    public static final String USERS_RANKING_API = "ranking.php";

    // Insertion Request Text
    JSONObject jsonBody;
    public static String SESSION_ID = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking);

        // Extra Information
        Bundle bundle = getIntent().getExtras();

        // Get SESSION_ID
        SESSION_ID = bundle.getString("session_id");
        Log.d("Ranking", "SESSION_ID: " +  SESSION_ID);

        getUsersInformation();
    }

    public void getUsersInformation() {

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        final Adapter adapter = new Adapter(this,this, Model.getInstance().getUsersList());
        recyclerView.setAdapter(adapter);

        // Create Server Request Queue
        requestQueue = Volley.newRequestQueue(this);

        // Server Request Insertion SESSION_ID
        try {
            jsonBody = new JSONObject("{\"session_id\":\"" + SESSION_ID + "\"}");
        } catch (JSONException ex) {
            Log.d("Ranking", "Insert SESSION_ID failed");
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
                        Log.d("Ranking", response.toString());

                        // Depopulate Model
                        Model.getInstance().depopulateUsers();

                        // Populate Model
                        Model.getInstance().populateUsers(response);

                        // Update Adapter
                        adapter.notifyDataSetChanged();

                        Log.d("Prova", ""+ Model.getInstance().getUsersList());

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