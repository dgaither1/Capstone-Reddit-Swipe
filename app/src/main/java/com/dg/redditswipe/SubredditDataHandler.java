package com.dg.redditswipe;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

/**
 * Created by mlc9433 on 7/26/17.
 */

public class SubredditDataHandler {

    private static String deviceId;

    private static HashMap<String, Boolean> subscribedSubreddits;

    public static void init(Context context) {

        deviceId = retrieveDeviceId(context);

        // Access subreddits on database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("subreddits" + deviceId);

        //Read from the database
        myRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                subscribedSubreddits = (HashMap<String, Boolean>) dataSnapshot.getValue();
                if(subscribedSubreddits == null) {
                    subscribedSubreddits = new HashMap<>();
                }
                Log.d("DG", "Value is: " + subscribedSubreddits.toString());
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("DG", "Failed to read value.", error.toException());
            }
        });
    }

    public static Boolean isSubredditSelected(String subredditName) {

        if(subscribedSubreddits == null) {
            subscribedSubreddits = new HashMap<>();
        }

        Boolean isSelected = subscribedSubreddits.get(subredditName);

        if(isSelected == null) {
            isSelected = true;
            addSubredditAndUpdateServer(subredditName, isSelected);
        }

        return isSelected;
    }

    public static void addSubredditAndUpdateServer(String subredditName, Boolean isSelected) {

        if(subscribedSubreddits == null) {
            subscribedSubreddits = new HashMap<>();
        }

        subscribedSubreddits.put(subredditName, isSelected);

        // Update the stored database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("subreddits" + deviceId);

        myRef.setValue(subscribedSubreddits);
    }

    public static HashMap<String, Boolean> getSubscribedSubreddits() {
        return subscribedSubreddits;
    }

    public static void updateSubscribedSubreddits(HashMap<String, Boolean> updatedSubreddits) {
        subscribedSubreddits = updatedSubreddits;

        // Update the stored database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("subreddits" + deviceId);

        myRef.setValue(subscribedSubreddits);
    }

    public static String getRandomSelectedSubreddit() {
        if(subscribedSubreddits != null) {
            HashMap<String, Boolean> modifiedCollection = new HashMap<>(subscribedSubreddits);
            modifiedCollection.values().removeAll(Collections.singleton(false));

            if (!modifiedCollection.isEmpty()) {
                List<String> keysAsList = new ArrayList<>(modifiedCollection.keySet());

                Random generator = new Random();

                return keysAsList.get(generator.nextInt(keysAsList.size()));
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public static String retrieveDeviceId(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        String deviceId = preferences.getString("deviceId", "");
        if(deviceId.equalsIgnoreCase(""))
        {
            deviceId = generateAndStoreDeviceId(context);
        }

        return deviceId;
    }

    public static String generateAndStoreDeviceId(Context context) {

        Random r = new Random();
        String deviceId =  String.valueOf(r.nextInt(999999));

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("deviceId", deviceId);
        editor.apply();

        return null;
    }
}
