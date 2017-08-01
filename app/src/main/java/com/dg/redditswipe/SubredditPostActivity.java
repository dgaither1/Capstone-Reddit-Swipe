package com.dg.redditswipe;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GestureDetectorCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.dg.redditswipe.data.RedditPostDO;
import com.dg.redditswipe.services.RedditServiceDelegate;
import com.dg.redditswipe.services.RedditServiceGetPostDelegate;
import com.dg.redditswipe.services.RedditServiceHandler;
import com.dg.redditswipe.swipe.OnSwipeTouchListener;
import com.squareup.picasso.Picasso;

/**
 * Created by mlc9433 on 5/30/17.
 */

public class SubredditPostActivity extends AppCompatActivity implements RedditServiceGetPostDelegate, RedditServiceDelegate {

    private RedditPostDO post;
    private TextView title;
    private TextView body;
    private ProgressBar progressBar;
    private CardView content;
    private ImageView imagePreview;
    private ImageView upvote;
    private ImageView downvote;
    private CardView voteSection;
    private RelativeLayout parent;
    private GestureDetectorCompat detector;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subreddit_post);

        parent = (RelativeLayout) findViewById(R.id.parent);
        title = (TextView) findViewById(R.id.title);
        body = (TextView) findViewById(R.id.body);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        content = (CardView) findViewById(R.id.content);
        imagePreview = (ImageView) findViewById(R.id.image_preview);
        upvote = (ImageView) findViewById(R.id.upvote);
        downvote = (ImageView) findViewById(R.id.downvote);
        voteSection = (CardView) findViewById(R.id.vote_section);

        if(savedInstanceState != null && savedInstanceState.getParcelable(getString(R.string.post)) != null) {
            this.onGetPostSuccess((RedditPostDO) savedInstanceState.getParcelable(getString(R.string.post)));
        } else {
            loadNewPost();
        }
    }

    private void loadNewPost() {
        String subreddit = SubredditDataHandler.getRandomSelectedSubreddit();

        if(subreddit != null) {
            progressBar.setVisibility(View.VISIBLE);
            body.setVisibility(View.VISIBLE);
            content.setVisibility(View.INVISIBLE);
            imagePreview.setVisibility(View.GONE);
            voteSection.setVisibility(View.GONE);
            setTitle("");
            RedditServiceHandler.getRandomPostForSubreddit(getString(R.string.get_random_post), subreddit, this, this);
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

                dialog.show(fragmentManager, getString(R.string.fragment_tag));
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

        if(post.getTitle() != null && !post.getTitle().isEmpty() && !post.isNSFW()) {


            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    title.setText(post.getTitle());

                    if (post.getBody() != null && !post.getBody().isEmpty()) {
                        body.setText(post.getBody());
                    } else {
                        body.setVisibility(View.GONE);
                    }

                    if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {
                        Picasso.with(SubredditPostActivity.this).load(post.getImageUrl()).into(imagePreview);

                        imagePreview.setVisibility(View.VISIBLE);

                    }

                    setTitle(post.getSubreddit());

                    content.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(post.getUrl()));
                            startActivity(browserIntent);
                        }
                    });

                    upvote.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            RedditServiceHandler.voteOnPost(getString(R.string.vote_on_post), post.getKind() + "_" + post.getId(), "1", SubredditPostActivity.this, SubredditPostActivity.this);
                            loadNewPost();
                            Toast.makeText(SubredditPostActivity.this, R.string.upvoted, Toast.LENGTH_SHORT).show();
                        }
                    });

                    downvote.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            RedditServiceHandler.voteOnPost(getString(R.string.vote_on_post), post.getKind() + "_" + post.getId(), "-1", SubredditPostActivity.this, SubredditPostActivity.this);
                            loadNewPost();
                            Toast.makeText(SubredditPostActivity.this, R.string.downvoted, Toast.LENGTH_SHORT).show();
                        }
                    });


                    OnSwipeTouchListener onSwipeListener = new OnSwipeTouchListener() {

                        @Override
                        public boolean onSwipe(Direction direction) {
                            if(direction == Direction.up) {
                                RedditServiceHandler.voteOnPost(getString(R.string.vote_on_post), post.getKind() + "_" + post.getId(), "1", SubredditPostActivity.this, SubredditPostActivity.this);
                                loadNewPost();
                                Toast.makeText(SubredditPostActivity.this, R.string.upvoted, Toast.LENGTH_SHORT).show();
                            } else if (direction == Direction.down) {
                                RedditServiceHandler.voteOnPost(getString(R.string.vote_on_post), post.getKind() + "_" + post.getId(), "-1", SubredditPostActivity.this, SubredditPostActivity.this);
                                loadNewPost();
                                Toast.makeText(SubredditPostActivity.this, R.string.downvoted, Toast.LENGTH_SHORT).show();
                            }

                            return super.onSwipe(direction);
                        }
                    };

                    detector = new GestureDetectorCompat(SubredditPostActivity.this, onSwipeListener);

                    parent.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View view, MotionEvent motionEvent) {
                            return detector.onTouchEvent(motionEvent);
                        }
                    });

                    parent.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                        }
                    });

                    voteSection.setVisibility(View.VISIBLE);
                    content.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);
                }
            });

        } else {
            loadNewPost();
        }
    }

    @Override
    public void onGetPostFailure(String errorMessage) {
        Log.d("DG", "Post failed to load with errorMessage = " + errorMessage);

        loadNewPost();

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putParcelable(getString(R.string.post), this.post);

        super.onSaveInstanceState(outState);
    }

    @Override
    public void onSuccess(String serviceId) {

    }

    @Override
    public void onFailure(String serviceId, String errorMessage) {

    }

}
