package com.dg.redditswipe;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.util.Log;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Created by mlc9433 on 5/30/17.
 */

public class SubredditSelectDialogFragment extends AppCompatDialogFragment {

    private String[] names;
    private boolean[] checked;

    public static SubredditSelectDialogFragment newInstance() {
        SubredditSelectDialogFragment fragment = new SubredditSelectDialogFragment();

        Bundle args = new Bundle();
        fragment.setArguments(args);

        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        HashMap<String, Boolean> subreddits = SubredditDataHandler.getSubscribedSubreddits();

        if(subreddits != null && subreddits.size() > 0) {
            names = new String[subreddits.size()];
            checked = new boolean[subreddits.size()];
            int index = 0;

            Iterator iterator = subreddits.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry pair = (Map.Entry) iterator.next();
                names[index] = (String) pair.getKey();
                checked[index] = (boolean) pair.getValue();
                index++;
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

            builder.setTitle(R.string.select_your_subreddits).setMultiChoiceItems(names, checked, new DialogInterface.OnMultiChoiceClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int index, boolean isChecked) {
                }
            });

            return builder.create();
        } else {
            return super.onCreateDialog(savedInstanceState);
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {

        HashMap<String, Boolean> updatedSubreddits = new HashMap<>();

        for(int i = 0; i < names.length; i++) {
            updatedSubreddits.put(names[i], checked[i]);
        }

        SubredditDataHandler.updateSubscribedSubreddits(updatedSubreddits);

        super.onDismiss(dialog);
    }
}
