package com.dg.redditswipe;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.dg.redditswipe.services.RedditServiceDelegate;
import com.dg.redditswipe.services.RedditServiceHandler;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class LoginResultActivity extends AppCompatActivity implements RedditServiceDelegate {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        if(getIntent()!=null && getIntent().getAction().equals(Intent.ACTION_VIEW)) {
            Uri uri = getIntent().getData();
            if(uri.getQueryParameter("error") != null) {
                String error = uri.getQueryParameter("error");
                closeWithError("We had a problem logging you in to Reddit. Please try again.  Error = " + error);
            } else {
                String state = uri.getQueryParameter("state");
                if(state.equals(getString(R.string.state))) {
                    String code = uri.getQueryParameter("code");
                    RedditServiceHandler.getAccessToken("getAccessToken", code, this, this);
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
        if(serviceId.equalsIgnoreCase("getAccessToken")) {
            RedditServiceHandler.getSubreddits("getSubreddits", this, this);
        } else if (serviceId.equalsIgnoreCase("getSubreddits")) {
            Intent launchSubredditPost = new Intent(LoginResultActivity.this, SubredditPostActivity.class);
            startActivity(launchSubredditPost);
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
