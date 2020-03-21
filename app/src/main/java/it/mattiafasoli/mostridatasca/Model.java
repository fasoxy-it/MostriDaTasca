package it.mattiafasoli.mostridatasca;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Model {
    private static final Model instance = new Model();

    private ArrayList<User> users = null;

    private ArrayList<MonsterCandy> monsterscandies = null;

    public static Model getInstance() {
        return instance;
    }

    private Model() {
        users = new ArrayList<User>();
        monsterscandies = new ArrayList<MonsterCandy>();
    }

    public int getUsersSize() {
        return users.size();
    }

    public int getMonstersCandiesSize() {
        return monsterscandies.size();
    }

    public User getUserByIndex(int index) {
        return users.get(index);
    }

    public void depopulateUsers() {
        for (int i=users.size()-1; i>=0; i--){
            users.remove(i);
        }
    }

    public void populateUsers(JSONObject response) {
        try {
            JSONArray array = response.getJSONArray("ranking");

            for (int i = 0; i < array.length(); i++) {

                JSONObject user = array.getJSONObject(i);

                String username = user.getString("username");
                String image = user.getString("img");
                String xp = user.getString("xp");
                int lifepoints = user.getInt("lp");

                User newUser = new User(username, image, xp, lifepoints);
                users.add(newUser);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void depopulateMonsterCandies() {
        for (int i=monsterscandies.size()-1; i>=0; i--){
            monsterscandies.remove(i);
        }
    }

    public void populateMonstersCandies(JSONObject response) {
        try {
            JSONArray array = response.getJSONArray("mapobjects");

            for (int i = 0; i < array.length(); i++) {

                JSONObject monstercandy = array.getJSONObject(i);

                String id = monstercandy.getString("id");
                Double lat = monstercandy.getDouble("lat");
                Double lon = monstercandy.getDouble("lon");
                String type = monstercandy.getString("type");
                String size = monstercandy.getString("size");
                String name = monstercandy.getString("name");

                MonsterCandy newMonsterCandy = new MonsterCandy(id, lat, lon, type, size, name);
                monsterscandies.add(newMonsterCandy);

            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public ArrayList<User> getUsersList() {
        return users;
    }

    public ArrayList<MonsterCandy> getMonstersCandiesList() {
        return monsterscandies;
    }



}
