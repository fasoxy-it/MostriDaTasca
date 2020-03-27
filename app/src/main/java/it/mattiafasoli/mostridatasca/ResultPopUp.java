package it.mattiafasoli.mostridatasca;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

public class ResultPopUp extends AppCompatActivity {

    // User Information
    public static int userXpBefore;
    public static int userLifepointsBefore;
    public static int userXpAfter;
    public static int userLifepointsAfter;

    public static int userXp;
    public static int userLifepoints;

    public static String userDied;

    // MonsterCandy Information
    public static String monstercandyType;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("ResultPopUp", "Method onCreate");
        setContentView(R.layout.activity_result_pop_up);

        // Get Result PopUp Default Settings
        getResultPopUpDefaultSettings();

        // Get Extra Information
        getExtraInformation();

        // Close Button
        View closeButton = findViewById(R.id.closeButton);
        closeButton.setOnClickListener(objectClickListener);

        // Result TextView
        TextView victoryResultTextView = findViewById(R.id.victoryResultTextView);
        TextView defeatResultTextView = findViewById(R.id.defeatResultTextView);
        TextView eatenResultTextView = findViewById(R.id.eatenResultTextView);

        if (monstercandyType.equals("MO")) {
            if (userDied.equals("false")) {
                victoryResultTextView.setVisibility(View.VISIBLE);
            } else if (userDied.equals("true")) {
                defeatResultTextView.setVisibility(View.VISIBLE);
            }
        } else if (monstercandyType.equals("CA")) {
            eatenResultTextView.setVisibility(View.VISIBLE);
        }

        // Result Xp TextView
        TextView resultxpTextView = findViewById(R.id.resultxpTextView);

        if (userXp >= 0) {
            resultxpTextView.setText("+" + String.valueOf(userXp));
        } else if (userXp < 0) {
            resultxpTextView.setText(String.valueOf(userXp));
        }

        // Result Life Points TextView
        TextView resultlifepointsTextView = findViewById(R.id.resultlifepointsTextView);

        if (userLifepoints >= 0) {
            resultlifepointsTextView.setText("+" + String.valueOf(userLifepoints));
        } else if (userLifepoints < 0) {
            resultlifepointsTextView.setText(String.valueOf(userLifepoints));
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d("ResultPopUp", "Method onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d("ResultPopUp", "Method onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d("ResultPopUp", "Method onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
        Log.d("ResultPopUp", "Method onStop");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("ResultPopUp", "Method onDestroy");
    }

    // Object Click Listener
    private View.OnClickListener objectClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {

                // onClick Close Button
                case R.id.closeButton:
                    Log.d("ResultPopUp", "Method onClick CloseButton");
                    ResultPopUp.super.onBackPressed();
                    break;

            }
        }
    };

    public void getResultPopUpDefaultSettings() {

        Log.d("ResultPopUp", "Method getResultPopUpDefaultSettings");

        // PopUp Default Settings
        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int width = displayMetrics.widthPixels;
        int height = displayMetrics.heightPixels;

        getWindow().setLayout((int) (width*0.5089058524), (int) (height*0.2303523035));

        WindowManager.LayoutParams params = getWindow().getAttributes();
        params.gravity = Gravity.CENTER;
        params.x = 0;
        params.y = -150;

        getWindow().setAttributes(params);

    }

    public void getExtraInformation() {

        Log.d("ResultPopUp", "Method getExtraInformation");

        // Extra Information
        Bundle bundle = getIntent().getExtras();

        // User Information
        userXpBefore = bundle.getInt("userXpBefore");
        userLifepointsBefore = bundle.getInt("userLifepointsBefore");
        userXpAfter = bundle.getInt("userXpAfter");
        userLifepointsAfter = bundle.getInt("userLifepointsAfter");
        userDied = bundle.getString("userDied");

        userXp = userXpAfter - userXpBefore;
        userLifepoints = userLifepointsAfter - userLifepointsBefore;

        // MonsterCandy Information
        monstercandyType = bundle.getString("monstercandyType");


    }

}


