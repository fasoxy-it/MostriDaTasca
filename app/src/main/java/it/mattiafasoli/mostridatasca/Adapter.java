package it.mattiafasoli.mostridatasca;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class Adapter extends RecyclerView.Adapter<ViewHolder> {

    // ViewHolder
    private LayoutInflater inflater;
    private Activity parentActivity;

    // User ArrayList
    private ArrayList<User> users;

    public Adapter(Context context, Activity parentActivity, ArrayList<User>users) {
        this.inflater = LayoutInflater.from(context);
        this.parentActivity = parentActivity;
        this.users = users;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = inflater.inflate(R.layout.single_row, parent, false);
        return new ViewHolder(view, parentActivity);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        // Set User Information
        User user = Model.getInstance().getUserByIndex(position);
        holder.setUser(user);

    }

    @Override
    public int getItemCount() {
        return Model.getInstance().getUsersSize();
    }
}