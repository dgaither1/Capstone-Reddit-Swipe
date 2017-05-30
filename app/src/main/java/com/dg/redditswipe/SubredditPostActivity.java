package com.dg.redditswipe;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import java.util.HashMap;

/**
 * Created by mlc9433 on 5/30/17.
 */

public class SubredditPostActivity extends AppCompatActivity {

    private HashMap<String, Boolean> subreddits;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        subreddits = (HashMap<String, Boolean>) getIntent().getSerializableExtra("subredditNames");
//         getArguments().getSerializable("args");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.subreddit_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_select_subreddits:
                Toast.makeText(this, "Select subreddits was clicked", Toast.LENGTH_SHORT).show();

                FragmentManager fragmentManager = getSupportFragmentManager();
                SubredditSelectDialogFragment dialog = SubredditSelectDialogFragment.newInstance(subreddits);

                dialog.show(fragmentManager, "tagSelection");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }
}
