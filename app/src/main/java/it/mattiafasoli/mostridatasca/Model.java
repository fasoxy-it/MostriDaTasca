package it.mattiafasoli.mostridatasca;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class Model {
    private static final Model instance = new Model();

    // User ArrayList
    private ArrayList<User> users;

    // MonsterCandy ArrayList
    private ArrayList<MonsterCandy> monsterscandies;

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

    public MonsterCandy getMonsterCandyByIndex(int index) {
        return monsterscandies.get(index);
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

                String userName = user.getString("username");
                String userImage = user.getString("img");
                int userXp = user.getInt("xp");
                int userLifepoints = user.getInt("lp");

                User newUser = new User(userName, userImage, userXp, userLifepoints);
                users.add(newUser);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    public void depopulateMonstersCandies() {
        for (int i=monsterscandies.size()-1; i>=0; i--){
            monsterscandies.remove(i);
        }
    }

    public void populateMonstersCandies(JSONObject response) {
        try {
            JSONArray array = response.getJSONArray("mapobjects");

            for (int i = 0; i < array.length(); i++) {

                JSONObject monstercandy = array.getJSONObject(i);

                String monstercandyId = monstercandy.getString("id");
                Double monstercandyLat = monstercandy.getDouble("lat");
                Double monstercandyLon = monstercandy.getDouble("lon");
                String monstercandyType = monstercandy.getString("type");
                String monstercandySize = monstercandy.getString("size");
                String monstercandyName = monstercandy.getString("name");

                MonsterCandy newMonsterCandy = new MonsterCandy(monstercandyId, monstercandyLat, monstercandyLon, monstercandyType, monstercandySize, monstercandyName);
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
