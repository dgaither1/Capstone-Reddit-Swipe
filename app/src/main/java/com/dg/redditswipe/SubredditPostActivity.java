package com.dg.redditswipe;

import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.dg.redditswipe.data.RedditPostDO;
import com.dg.redditswipe.services.RedditServiceDelegate;
import com.dg.redditswipe.services.RedditServiceGetPostDelegate;
import com.dg.redditswipe.services.RedditServiceHandler;
import com.squareup.picasso.Picasso;

import java.util.HashMap;

/**
 * Created by mlc9433 on 5/30/17.
 */

public class SubredditPostActivity extends AppCompatActivity implements RedditServiceGetPostDelegate, RedditServiceDelegate {

    private RedditPostDO post;
    private TextView title;
    private TextView body;
    private ProgressBar progressBar;
    private LinearLayout content;
    private ImageView imagePreview;
    private ImageView upvote;
    private ImageView downvote;
    private LinearLayout voteSection;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subreddit_post);

        title = (TextView) findViewById(R.id.title);
        body = (TextView) findViewById(R.id.body);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        content = (LinearLayout) findViewById(R.id.content);
        imagePreview = (ImageView) findViewById(R.id.image_preview);
        upvote = (ImageView) findViewById(R.id.upvote);
        downvote = (ImageView) findViewById(R.id.downvote);
        voteSection = (LinearLayout) findViewById(R.id.vote_section);

        if(savedInstanceState != null && savedInstanceState.getParcelable("post") != null) {
            this.onGetPostSuccess((RedditPostDO) savedInstanceState.getParcelable("post"));
        } else {
            loadNewPost();
        }
    }

    private void loadNewPost() {
        String subreddit = SubredditDataHandler.getRandomSelectedSubreddit();

        if(subreddit != null) {
            progressBar.setVisibility(View.VISIBLE);
            content.setVisibility(View.INVISIBLE);
            imagePreview.setVisibility(View.GONE);
            voteSection.setVisibility(View.GONE);
            setTitle("");
            RedditServiceHandler.getRandomPostForSubreddit("getRandomPostForSubReddit", subreddit, this, this);
        }
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
    public void onBackPressed() {
        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory( Intent.CATEGORY_HOME );
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(homeIntent);
    }

    @Override
    public void onGetPostSuccess(final RedditPostDO post) {
        this.post = post;

        runOnUiThread(new Runnable() {
            @Override
            public void run() {

                title.setText(post.getTitle());

                if(post.getBody() != null && !post.getBody().isEmpty()) {
                    body.setText(post.getBody());
                } else {
                    body.setVisibility(View.GONE);
                }

                if(post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
                    Picasso.with(SubredditPostActivity.this).load(post.getImageUrl()).into(imagePreview);

                    imagePreview.setVisibility(View.VISIBLE);

                    imagePreview.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(post.getUrl()));
                            startActivity(browserIntent);
                        }
                    });
                }

                setTitle(post.getSubreddit());

                upvote.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        RedditServiceHandler.voteOnPost("voteOnPost", post.getKind() + "_" + post.getId(), "1", SubredditPostActivity.this, SubredditPostActivity.this);
                        loadNewPost();
                    }
                });

                downvote.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        RedditServiceHandler.voteOnPost("voteOnPost", post.getKind() + "_" + post.getId(), "-1", SubredditPostActivity.this, SubredditPostActivity.this);
                        loadNewPost();
                    }
                });

                voteSection.setVisibility(View.VISIBLE);
                content.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onGetPostFailure(String errorMessage) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                progressBar.setVisibility(View.GONE);

                title.setText("This post could not be loaded");
            }
        });

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable("post", this.post);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onSuccess(String serviceId) {

    }

    @Override
    public void onFailure(String serviceId, String errorMessage) {

    }


}
