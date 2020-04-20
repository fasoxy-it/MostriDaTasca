package it.mattiafasoli.mostridatasca;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;

import java.util.Timer;
import java.util.TimerTask;

public class MostriDaTasca extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("MostriDaTasca", "Method onStart");
        setContentView(R.layout.activity_mostri_da_tasca);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                Intent mostridatascaIntent = new Intent(getApplicationContext(), Register.class);
                startActivity(mostridatascaIntent);

            }
        }, 3000);
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("MostriDaTasca", "Method onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("MostriDaTasca", "Method onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("MostriDaTasca", "Method onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d("MostriDaTasca", "Method onStop");

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("MostriDaTasca", "Method onDestroy");
    }
}
