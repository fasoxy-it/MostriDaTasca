package it.mattiafasoli.mostridatasca;

import android.app.Activity;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class ViewHolder extends RecyclerView.ViewHolder /*implements View.OnClickListener*/ {

    private TextView rankingposition;
    private TextView username;
    private ImageView userimage;
    private TextView userxp;
    private ProgressBar userlifepoints;
    private String user;
    private Activity parentActivity;

    public ViewHolder(View itemView, Activity parentActivity) {
        super(itemView);
        this.parentActivity = parentActivity;

        rankingposition = itemView.findViewById(R.id.rankingPositionTextView);
        username = itemView.findViewById(R.id.usernameTextView);
        userimage = itemView.findViewById(R.id.userImage);
        userxp = itemView.findViewById(R.id.xpTextView);
        userlifepoints = itemView.findViewById(R.id.lifepointsProgressBar);
        //itemView.setOnClickListener(this);
    }


    public void setUser(User user) {
        this.user = user.getUsername();
        rankingposition.setText(String.valueOf(getAdapterPosition()+1));
        username.setText(user.getUsername());
        userimage.setImageBitmap(user.getImage());
        userxp.setText(user.getXp());
        userlifepoints.setProgress(user.getLifepoints());
    }

    /*
    @Override
    public void onClick(View view) {
        Intent intentDetail = new Intent(parentActivity, Main3Activity.class);
        intentDetail.putExtra(Main3Activity.STUDENTID_EXTRA, student);
        parentActivity.startActivity(intentDetail);
    }*/
}