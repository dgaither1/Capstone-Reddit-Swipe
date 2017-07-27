package com.dg.redditswipe.services;

import android.content.Context;
import android.content.Intent;
import android.util.Base64;
import android.util.Log;

import com.dg.redditswipe.LoginResultActivity;
import com.dg.redditswipe.R;
import com.dg.redditswipe.SubredditDataHandler;
import com.dg.redditswipe.SubredditPostActivity;
import com.dg.redditswipe.data.RedditPostDO;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;

/**
 * Created by mlc9433 on 7/26/17.
 */

public class RedditServiceHandler {

    private static OkHttpClient client;
    private static String accessToken;
    private static String refreshToken;

    public static void getAccessToken(final String serviceId, String code, final RedditServiceDelegate delegate, final Context context) {
        client = new OkHttpClient();

        String authString =  context.getString(R.string.client_id) + ":";
        String encodedAuthString = Base64.encodeToString(authString.getBytes(),
                Base64.NO_WRAP);

        Request request = new Request.Builder()
                .addHeader("User-Agent", "RedditSwipe")
                .addHeader("Authorization", "Basic " + encodedAuthString)
                .url(context.getString(R.string.access_token_url))
                .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"),
                        "grant_type=authorization_code&code=" + code +
                                "&redirect_uri=" + context.getString(R.string.redirect_uri)))
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                delegate.onFailure(serviceId, context.getString(R.string.reddit_logon_error));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String json = response.body().string();

                JSONObject data = null;
                try {
                    data = new JSONObject(json);
                    accessToken = data.optString("access_token");
                    refreshToken = data.optString("refresh_token");

                    delegate.onSuccess(serviceId);

                } catch (JSONException e) {
                    delegate.onFailure(serviceId, context.getString(R.string.reddit_logon_error));
                }
            }
        });

    }

    public static void getSubreddits(final String serviceId, final RedditServiceDelegate delegate, final Context context) {

        if(accessToken != null && !accessToken.isEmpty()) {

            Request request = new Request.Builder()
                    .addHeader("User-Agent", "RedditSwipe")
                    .addHeader("Authorization", "bearer " + accessToken)
                    .url(context.getString(R.string.subreddit_url))
                    .get()
                    .build();

            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(Call call, IOException e) {
                    delegate.onFailure(serviceId, e.getMessage());
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

                        if (children.length() == 0) {
                            delegate.onFailure(serviceId, "You must be subscribed to at least 1 subreddit");
                        }

                        HashMap<String, Boolean> subredditNames = new HashMap<>();

                        for (int i = 0; i < children.length(); i++) {
                            JSONObject subreddit = children.getJSONObject(i).optJSONObject("data");

                            subredditNames.put(subreddit.optString("display_name"), SubredditDataHandler.isSubredditSelected(subreddit.optString("display_name")));
                        }

                        delegate.onSuccess(serviceId);

                    } catch (JSONException e) {
                        delegate.onFailure(serviceId, "There was an error pulling the subreddits that you are subscribed to");
                    }

                }
            });
        } else {
            delegate.onFailure(serviceId, "Your connection to Reddit was lost");
        }
    }

    public static void getRandomPostForSubreddit(final String serviceId, String subredditName, final RedditServiceGetPostDelegate delegate, Context context) {
        if(accessToken != null && !accessToken.isEmpty()) {

            Request request = new Request.Builder()
                    .addHeader("User-Agent", "RedditSwipe")
                    .addHeader("Authorization", "bearer " + accessToken)
                    .url(context.getString(R.string.specific_subreddit_url) + subredditName + context.getString(R.string.random))
                    .get()
                    .build();

            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(Call call, IOException e) {
                    delegate.onGetPostFailure(e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body().string();

                    if(response.isSuccessful()) {
                        JSONObject data = null;
                        if (responseBody.startsWith("[")) {
                            try {
                                JSONArray jsonResponse = null;
                                jsonResponse = new JSONArray(responseBody);
                                data = jsonResponse.getJSONObject(0).optJSONObject("data");
                            } catch (JSONException e) {
                                delegate.onGetPostFailure("There was an error parsing the response from Reddit");
                            }
                        } else {
                            try {
                                JSONObject jsonResponse = null;
                                jsonResponse = new JSONObject(responseBody);
                                data = jsonResponse.getJSONObject("data");
                            } catch (JSONException e) {
                                delegate.onGetPostFailure("There was an error parsing the response from Reddit");
                            }
                        }


                        try {
                            if (data != null) {
                                JSONArray children = data.optJSONArray("children");

                                JSONObject post = children.getJSONObject(0).optJSONObject("data");

                                RedditPostDO postDO = new RedditPostDO();

                                postDO.setKind(children.getJSONObject(0).optString("kind"));
                                postDO.setId(post.optString("id"));
                                postDO.setTitle(post.optString("title"));
                                postDO.setSubreddit(post.optString("subreddit_name_prefixed"));
                                postDO.setScore(post.optLong("score"));
                                postDO.setPoster(post.optString("author"));
                                postDO.setSelfPost(post.optBoolean("is_self"));
                                postDO.setUrl(post.optString("url"));

                                if (postDO.isSelfPost()) {
                                    postDO.setBody(post.optString("selftext"));
                                } else {
                                    JSONObject preview = post.getJSONObject("preview");
                                    JSONArray images = preview.optJSONArray("images");
                                    JSONObject source = images.getJSONObject(0).optJSONObject("source");
                                    postDO.setImageUrl(source.optString("url"));
                                }

                                Log.d("DG", postDO.toString());

                                delegate.onGetPostSuccess(postDO);
                            } else {
                                delegate.onGetPostFailure("There was an error parsing the response from Reddit");
                            }

                        } catch (JSONException e) {
                            delegate.onGetPostFailure("There was an error parsing the response from Reddit");
                        }
                    } else {
                        delegate.onGetPostFailure(responseBody);
                    }
                }
            });
        } else {
            delegate.onGetPostFailure("Your connection to Reddit was lost");
        }
    }

    public static void voteOnPost(final String serviceId, String postFullname, String vote, final RedditServiceDelegate delegate, Context context) {
        if(accessToken != null && !accessToken.isEmpty()) {

            Request request = new Request.Builder()
                    .addHeader("User-Agent", "RedditSwipe")
                    .addHeader("Authorization", "bearer " + accessToken)
                    .url(context.getString(R.string.vote_url))
                    .post(RequestBody.create(MediaType.parse("application/x-www-form-urlencoded"),
                            "dir=" + vote + "&id=" + postFullname + "&rank=99"))
                    .build();

            client.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(Call call, IOException e) {
                    delegate.onFailure(serviceId, e.getMessage());
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    if(response.isSuccessful()) {

                        delegate.onSuccess(serviceId);
                    } else {
                        String responseBody = response.body().string();
                        delegate.onFailure(serviceId, responseBody);
                    }
                }
            });
        }
    }
}
