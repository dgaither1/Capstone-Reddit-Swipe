package com.dg.redditswipe;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

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

public class LoginResultActivity extends AppCompatActivity {

    private OkHttpClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if(getIntent()!=null && getIntent().getAction().equals(Intent.ACTION_VIEW)) {
            Uri uri = getIntent().getData();
            if(uri.getQueryParameter("error") != null) {
                String error = uri.getQueryParameter("error");
                closeWithError("We had a problem logging you in to Reddit. Please try again.  Error = " + error);
            } else {
                String state = uri.getQueryParameter("state");
                if(state.equals(getString(R.string.state))) {
                    String code = uri.getQueryParameter("code");
                    getAccessToken(code);
                }
            }
        }
    }

    private void closeWithError(String errorMessage) {
        Toast.makeText(LoginResultActivity.this, errorMessage, Toast.LENGTH_LONG).show();
        finish();
    }

    private void getAccessToken(String code) {
        client = new OkHttpClient();

        String authString = getString(R.string.client_id) + ":";
        String encodedAuthString = Base64.encodeToString(authString.getBytes(),
                Base64.NO_WRAP);

        Request request = new Request.Builder()
                .addHeader("User-Agent", "RedditSwipe")
                .addHeader("Authorization", "Basic " + encodedAuthString)
                .url(getString(R.string.access_token_url))
                .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"),
                        "grant_type=authorization_code&code=" + code +
                                "&redirect_uri=" + getString(R.string.redirect_uri)))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                closeWithError("We had a problem logging you in to Reddit. Please try again");
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();

                JSONObject data = null;
                try {
                    data = new JSONObject(json);
                    String accessToken = data.optString("access_token");
                    String refreshToken = data.optString("refresh_token");

                    getSubreddits(accessToken);

                } catch (JSONException e) {
                    closeWithError("We had a problem logging you in to Reddit. Please try again");
                }
            }
        });
    }

    private void getSubreddits(String accessToken) {
        Request request = new Request.Builder()
                .addHeader("User-Agent", "RedditSwipe")
                .addHeader("Authorization", "bearer " + accessToken)
                .url(getString(R.string.subreddit_url))
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(Call call, IOException e) {
                Log.e("DG", "ERROR: " + e);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();

                Log.d("DG", json);

                JSONObject jsonResponse = null;
                try {
                    jsonResponse = new JSONObject(json);
                    JSONObject data = jsonResponse.optJSONObject("data");
                    JSONArray children = data.optJSONArray("children");

                    if(children.length() == 0) {
                        closeWithError("You must be subscribed to at least 1 subreddit");
                    }

                    HashMap<String, Boolean> subredditNames = new HashMap<>();

                    for(int i = 0; i < children.length(); i++) {
                        JSONObject subreddit = children.getJSONObject(i).optJSONObject("data");

                        subredditNames.put(subreddit.optString("display_name"), true);
                    }

                    Intent launchSubredditPost = new Intent(LoginResultActivity.this, SubredditPostActivity.class);
                    launchSubredditPost.putExtra("subredditNames", subredditNames);
                    startActivity(launchSubredditPost);


                } catch (JSONException e) {
                    closeWithError("There was an error pulling the subreddits that you are subscribed to");
                }

            }
        });
    }
}
