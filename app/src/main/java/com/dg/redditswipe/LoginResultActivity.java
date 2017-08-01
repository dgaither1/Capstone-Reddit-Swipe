package com.dg.redditswipe;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import com.dg.redditswipe.services.RedditServiceDelegate;
import com.dg.redditswipe.services.RedditServiceHandler;

public class LoginResultActivity extends AppCompatActivity implements RedditServiceDelegate {

    private TextView loggingYouIn;
    private TextView success;
    private ProgressBar progressBar;
    private AdView adView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_login_result);

        loggingYouIn = (TextView) findViewById(R.id.logging_in);
        success = (TextView) findViewById(R.id.success);
        progressBar = (ProgressBar) findViewById(R.id.progress_bar);
        adView = (AdView) findViewById(R.id.ad_view);


        AdRequest adRequest = new AdRequest.Builder().build();
        adView.loadAd(adRequest);

        if(getIntent()!=null && getIntent().getAction().equals(Intent.ACTION_VIEW)) {
            Uri uri = getIntent().getData();
            if(uri.getQueryParameter(getString(R.string.error_param)) != null) {
                String error = uri.getQueryParameter(getString(R.string.error_param));
                closeWithError(getString(R.string.reddit_logon_error) + " Error = " + error);
            } else {
                String state = uri.getQueryParameter(getString(R.string.state_param));
                if(state.equals(getString(R.string.state))) {
                    String code = uri.getQueryParameter(getString(R.string.code_param));
                    RedditServiceHandler.getAccessToken(getString(R.string.get_access_token), code, this, this);
                }
            }
        }
    }

    private void closeWithError(String errorMessage) {
        Toast.makeText(LoginResultActivity.this, errorMessage, Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    public void onSuccess(String serviceId) {
        if(serviceId.equalsIgnoreCase(getString(R.string.get_access_token))) {
            RedditServiceHandler.getSubreddits(getString(R.string.get_subreddits), this, this);
        } else if (serviceId.equalsIgnoreCase(getString(R.string.get_subreddits))) {

            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0f);
                    fadeOut.setDuration(500);
                    fadeOut.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) { }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            loggingYouIn.setAlpha(0f);
                            progressBar.setAlpha(0f);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) { }
                    });
                    loggingYouIn.startAnimation(fadeOut);
                    progressBar.startAnimation(fadeOut);

                    success.setAlpha(0f);
                    success.setVisibility(View.VISIBLE);

                    AlphaAnimation fadeIn = new AlphaAnimation(0f, 1.0f);
                    fadeIn.setDuration(500);
                    fadeIn.setAnimationListener(new Animation.AnimationListener() {
                        @Override
                        public void onAnimationStart(Animation animation) { }

                        @Override
                        public void onAnimationEnd(Animation animation) {
                            success.setAlpha(1.0f);
                        }

                        @Override
                        public void onAnimationRepeat(Animation animation) { }
                    });
                    success.startAnimation(fadeIn);

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            Intent launchSubredditPost = new Intent(LoginResultActivity.this, SubredditPostActivity.class);
                            startActivity(launchSubredditPost);
                        }
                    }, 2000);
                }
            });

        }
    }

    @Override
    public void onFailure(String serviceId, final String errorMessage) {
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                closeWithError(errorMessage);
            }
        });
    }
}
