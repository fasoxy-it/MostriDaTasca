package it.mattiafasoli.mostridatasca;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

public class User {
    private String username;
    private String imagestring;
    private Bitmap image;
    private String xp;
    private int lifepoints;

    public User (String username, String imagestring, String xp, int lifepoints) {
        this.username = username;
        this.imagestring = imagestring;
        byte[] decodedString = Base64.decode(imagestring, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        this.image = decodedByte;
        this.xp = xp;
        this.lifepoints = lifepoints;
    }


    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Bitmap getImage() {
        return image;
    }

    public void setImage(String imagestring) { this.imagestring = imagestring; }

    public String getXp() {
        return xp;
    }

    public void setXp(String xp) {
        this.xp = xp;
    }

    public int getLifepoints() {
        return lifepoints;
    }

    public void setLifepoints(int lifepoints) {
        this.lifepoints = lifepoints;
    }

}