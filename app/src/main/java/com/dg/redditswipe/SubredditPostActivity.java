package com.dg.redditswipe;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dg.redditswipe.data.RedditPostDO;
import com.dg.redditswipe.services.RedditServiceDelegate;
import com.dg.redditswipe.services.RedditServiceGetPostDelegate;
import com.dg.redditswipe.services.RedditServiceHandler;

import java.util.HashMap;

/**
 * Created by mlc9433 on 5/30/17.
 */

public class SubredditPostActivity extends AppCompatActivity implements RedditServiceGetPostDelegate {

    private TextView title;
    private TextView body;
    private ProgressBar progressBar;
    private LinearLayout content;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subreddit_post);

        title = (TextView) findViewById(R.id.title);
        body = (TextView) findViewById(R.id.body);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        content = (LinearLayout) findViewById(R.id.content);
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

                FragmentManager fragmentManager = getSupportFragmentManager();
                SubredditSelectDialogFragment dialog = SubredditSelectDialogFragment.newInstance();

                dialog.show(fragmentManager, "tagSelection");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    @Override
    protected void onResume() {
        super.onResume();
        String subreddit = SubredditDataHandler.getRandomSelectedSubreddit();

        if(subreddit != null) {
            progressBar.setVisibility(View.VISIBLE);
            content.setVisibility(View.INVISIBLE);
            RedditServiceHandler.getRandomPostForSubreddit("getRandomPostForSubReddit", subreddit, this, this);
        }
    }

    @Override
    public void onBackPressed() {
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory( Intent.CATEGORY_HOME );
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeIntent);
    }

    @Override
    public void onGetPostSuccess(final RedditPostDO post) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                title.setText(post.getTitle());

                if(post.getBody() != null && !post.getBody().isEmpty()) {
                    body.setText(post.getBody());
                } else {
                    body.setVisibility(View.GONE);
                }


                content.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onGetPostFailure(String errorMessage) {
        progressBar.setVisibility(View.GONE);
    }
}
