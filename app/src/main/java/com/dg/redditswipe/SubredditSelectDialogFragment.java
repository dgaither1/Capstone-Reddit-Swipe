package com.dg.redditswipe;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDialogFragment;
import android.util.Log;

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

    public static SubredditSelectDialogFragment newInstance(HashMap<String, Boolean> subreddits) {
        SubredditSelectDialogFragment fragment = new SubredditSelectDialogFragment();

        Bundle args = new Bundle();
        args.putSerializable("args", subreddits);
        fragment.setArguments(args);

        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        HashMap<String, Boolean> subreddits = (HashMap<String, Boolean>) getArguments().getSerializable("args");

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

            builder.setTitle("Select your subreddits").setMultiChoiceItems(names, checked, new DialogInterface.OnMultiChoiceClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int index, boolean isChecked) {
                    checked[index] = !checked[index];
                }
            });

            return builder.create();
        } else {
            return super.onCreateDialog(savedInstanceState);
        }
    }
}
