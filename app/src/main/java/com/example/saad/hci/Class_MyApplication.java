package com.example.saad.hci;

import android.app.Application;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class Class_MyApplication extends Application {
    public ArrayList<Class_HeritageSite>    all_HeritageSites   = new ArrayList<>();
    public ArrayList<String>                all_searchables     = new ArrayList<>();
    public float[]                          curLoc              = new float[] {0, 0};

    @Override
    public void onCreate()
    {
        super.onCreate();

        // create HeritageSite variables by reading JSON data from file.
        populateAllHeritageSites();
    }

    public void populateAllHeritageSites() {
        try {
            AssetManager am = getAssets();
            InputStream is = am.open("HeritageSiteData.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(is));
            String line, strJson = "";

            while ((line = reader.readLine()) != null)
                strJson += line;

            JSONObject jsonRootObject = new JSONObject(strJson);
            JSONArray jsonArray = jsonRootObject.optJSONArray("data");
            all_HeritageSites.ensureCapacity(jsonArray.length());

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.optJSONObject(i);
                all_HeritageSites.add(new Class_HeritageSite(jsonObject));
                all_searchables.add(all_HeritageSites.get(i).getName());
            }

        } catch (Exception e) {
            Log.d("mylog", e.toString());
        }
    }



    public void setLoc(float[] newLoc) {curLoc = newLoc;}
}
