package it.mattiafasoli.mostridatasca;

import android.app.Activity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class ViewHolder extends RecyclerView.ViewHolder /*implements View.OnClickListener*/ {

    // Ranking Information
    private TextView rankingPosition;

    // User Information
    private TextView userName;
    private ImageView userImage;
    private TextView userXp;
    private ProgressBar userLifepoints;

    // ViewHolder
    private String user;
    private Activity parentActivity;

    public ViewHolder(View itemView, Activity parentActivity) {
        super(itemView);
        this.parentActivity = parentActivity;

        // Set User Information ItemView
        rankingPosition = itemView.findViewById(R.id.rankingPositionTextView);
        userName = itemView.findViewById(R.id.userNameTextView);
        userImage = itemView.findViewById(R.id.userImageImageView);
        userXp = itemView.findViewById(R.id.userXpTextView);
        userLifepoints = itemView.findViewById(R.id.userLifepointsProgressBar);
        //itemView.setOnClickListener(this);
    }


    public void setUser(User user) {
        this.user = user.getUserName();

        // Set User Information ItemView
        rankingPosition.setText(String.valueOf(getAdapterPosition()+1));
        userName.setText(user.getUserName());
        userImage.setImageBitmap(user.getUserImage());
        userXp.setText(String.valueOf(user.getUserXp()));
        userLifepoints.setProgress(user.getUserLifepoints());

    }

    /*
    @Override
    public void onClick(View view) {
        Intent userIntent = new Intent(parentActivity, [Activity].class);
        userIntent.putExtra([Activity].[Variable], user);
        parentActivity.startActivity(userIntent);
    }*/
}