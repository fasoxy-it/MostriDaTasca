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
    private static int userXpBefore;
    private static int userLifepointsBefore;
    private static int userXpAfter;
    private static int userLifepointsAfter;

    private static int userXp;
    private static int userLifepoints;

    private static String userDied;

    // MonsterCandy Information
    private static String monstercandyType;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("ResultPopUp", "Method onCreate");
        setContentView(R.layout.activity_result_pop_up);

        // Get Result PopUp Layout
        getResultPopUpLayout();

        // Get Extra Information from previous Activity
        getExtraInformation();

        // Set Result PopUp Information
        setResultPopUpInformation();

        // Set Close Button
        View closeButton = findViewById(R.id.closeButton);
        closeButton.setOnClickListener(objectClickListener);

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

    public void getResultPopUpLayout() {

        Log.d("ResultPopUp", "Method getResultPopUpLayout");

        // Result PopUp Layout
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

        // Get Extra Information from previous Activity
        Bundle bundle = getIntent().getExtras();

        // Get / Set User Information from previous Activity
        userXpBefore = bundle.getInt("userXpBefore");
        userLifepointsBefore = bundle.getInt("userLifepointsBefore");
        userXpAfter = bundle.getInt("userXpAfter");
        userLifepointsAfter = bundle.getInt("userLifepointsAfter");
        userDied = bundle.getString("userDied");

        // Get / Set MonsterCandy Information from previous Activity
        monstercandyType = bundle.getString("monstercandyType");

    }

    public void setResultPopUpInformation() {

        Log.d("ResultPopUp", "Method setResultInformation");

        // Set Result TextView
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

        // Set Result User Xp
        userXp = userXpAfter - userXpBefore;

        // Set Result User LifePoints
        userLifepoints = userLifepointsAfter - userLifepointsBefore;

        // Set Result User Xp TextView
        TextView resultXpTextView = findViewById(R.id.resultxpTextView);

        if (userXp >= 0) {
            resultXpTextView.setText("+" + String.valueOf(userXp));
        } else if (userXp < 0) {
            resultXpTextView.setText(String.valueOf(userXp));
        }

        // Set Result User LifePoints TextView
        TextView resultLifepointsTextView = findViewById(R.id.resultlifepointsTextView);

        if (userLifepoints >= 0) {
            resultLifepointsTextView.setText("+" + String.valueOf(userLifepoints));
        } else if (userLifepoints < 0) {
            resultLifepointsTextView.setText(String.valueOf(userLifepoints));
        }

    }

}


