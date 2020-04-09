package it.mattiafasoli.mostridatasca;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

public class User {
    private String username;
    private String imagestring;
    private Bitmap image;
    private int xp;
    private int lifepoints;

    public User (String username, String imagestring, int xp, int lifepoints) {
        this.username = username;
        this.imagestring = imagestring;
        byte[] decodedString = Base64.decode(imagestring, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        this.image = decodedByte;
        this.xp = xp;
        this.lifepoints = lifepoints;
    }

    public String getUserName() {
        return username;
    }

    public Bitmap getUserImage() {
        return image;
    }

    public int getUserXp() {
        return xp;
    }

    public int getUserLifepoints() {
        return lifepoints;
    }

}