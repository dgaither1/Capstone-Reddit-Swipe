package com.dg.redditswipe.services;

import android.content.Context;
import android.util.Base64;
import android.util.Log;

import com.dg.redditswipe.R;
import com.dg.redditswipe.SubredditDataHandler;
import com.dg.redditswipe.data.RedditPostDO;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

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
                .addHeader(context.getString(R.string.user_agent_key), context.getString(R.string.app_name))
                .addHeader(context.getString(R.string.authorization_key), context.getString(R.string.basic) + encodedAuthString)
                .url(context.getString(R.string.access_token_url))
                .post(RequestBody.create(MediaType.parse(context.getString(R.string.media_type_form_urlencoded)),
                        context.getString(R.string.grant_type_and_code) + code +
                                context.getString(R.string.redirect_uri_param_name) + context.getString(R.string.redirect_uri)))
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
                    accessToken = data.optString(context.getString(R.string.access_token));
                    refreshToken = data.optString(context.getString(R.string.refresh_token));

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
                    .addHeader(context.getString(R.string.user_agent_key), context.getString(R.string.app_name))
                    .addHeader(context.getString(R.string.authorization_key), context.getString(R.string.bearer) + accessToken)
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
                        JSONObject data = jsonResponse.optJSONObject(context.getString(R.string.data));
                        JSONArray children = data.optJSONArray(context.getString(R.string.children));

                        if (children.length() == 0) {
                            delegate.onFailure(serviceId, context.getString(R.string.must_be_subscribed));
                        }

                        HashMap<String, Boolean> subredditNames = new HashMap<>();

                        for (int i = 0; i < children.length(); i++) {
                            JSONObject subreddit = children.getJSONObject(i).optJSONObject(context.getString(R.string.data));

                            subredditNames.put(subreddit.optString(context.getString(R.string.display_name)), SubredditDataHandler.isSubredditSelected(subreddit.optString(context.getString(R.string.display_name))));
                        }

                        delegate.onSuccess(serviceId);

                    } catch (JSONException e) {
                        delegate.onFailure(serviceId, context.getString(R.string.cant_pull_subreddits));
                    }

                }
            });
        } else {
            delegate.onFailure(serviceId, context.getString(R.string.connection_lost));
        }
    }

    public static void getRandomPostForSubreddit(final String serviceId, String subredditName, final RedditServiceGetPostDelegate delegate, final Context context) {
        if(accessToken != null && !accessToken.isEmpty()) {

            Request request = new Request.Builder()
                    .addHeader(context.getString(R.string.user_agent_key), context.getString(R.string.app_name))
                    .addHeader(context.getString(R.string.authorization_key), context.getString(R.string.bearer) + accessToken)
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

                    Log.d("DG", "responseBody = " + responseBody);

                    if(response.isSuccessful()) {
                        JSONObject data = null;
                        if (responseBody.startsWith("[")) {
                            try {
                                JSONArray jsonResponse = null;
                                jsonResponse = new JSONArray(responseBody);
                                data = jsonResponse.getJSONObject(0).optJSONObject(context.getString(R.string.data));
                            } catch (JSONException e) {
                                delegate.onGetPostFailure(context.getString(R.string.error_parsing_response));
                            }
                        } else {
                            try {
                                JSONObject jsonResponse = null;
                                jsonResponse = new JSONObject(responseBody);
                                data = jsonResponse.getJSONObject(context.getString(R.string.data));
                            } catch (JSONException e) {
                                delegate.onGetPostFailure(context.getString(R.string.error_parsing_response));
                            }
                        }


                        try {
                            if (data != null) {
                                JSONArray children = data.optJSONArray(context.getString(R.string.children));

                                JSONObject post = children.getJSONObject(0).optJSONObject(context.getString(R.string.data));

                                RedditPostDO postDO = new RedditPostDO();

                                postDO.setKind(children.getJSONObject(0).optString(context.getString(R.string.kind)));
                                postDO.setId(post.optString(context.getString(R.string.id)));
                                postDO.setTitle(post.optString(context.getString(R.string.title)));
                                postDO.setSubreddit(post.optString(context.getString(R.string.subreddit_name_prefixed)));
                                postDO.setScore(post.optLong(context.getString(R.string.score)));
                                postDO.setPoster(post.optString(context.getString(R.string.author)));
                                postDO.setSelfPost(post.optBoolean(context.getString(R.string.is_self)));
                                postDO.setUrl(post.optString(context.getString(R.string.url)));
                                postDO.setNSFW(post.optBoolean(context.getString(R.string.over_18)));

                                if (postDO.isSelfPost()) {
                                    if(post.optString(context.getString(R.string.selftext)).isEmpty()) {
                                        postDO.setBody(post.optString(context.getString(R.string.body)));
                                    } else {
                                        postDO.setBody(post.optString(context.getString(R.string.selftext)));
                                    }
                                } else {
                                    JSONObject preview = post.getJSONObject(context.getString(R.string.preview));
                                    JSONArray images = preview.optJSONArray(context.getString(R.string.images));
                                    JSONObject source = images.getJSONObject(0).optJSONObject(context.getString(R.string.source));
                                    postDO.setImageUrl(source.optString(context.getString(R.string.url)));
                                }

                                Log.d("DG", postDO.toString());

                                delegate.onGetPostSuccess(postDO);
                            } else {
                                delegate.onGetPostFailure(context.getString(R.string.error_parsing_response));
                            }

                        } catch (JSONException e) {
                            delegate.onGetPostFailure(context.getString(R.string.error_parsing_response));
                        }
                    } else {
                        delegate.onGetPostFailure(responseBody);
                    }
                }
            });
        } else {
            delegate.onGetPostFailure(context.getString(R.string.connection_lost));
        }
    }

    public static void voteOnPost(final String serviceId, String postFullname, String vote, final RedditServiceDelegate delegate, Context context) {
        if(accessToken != null && !accessToken.isEmpty()) {

            Request request = new Request.Builder()
                    .addHeader(context.getString(R.string.user_agent_key), context.getString(R.string.app_name))
                    .addHeader(context.getString(R.string.authorization_key), context.getString(R.string.bearer) + accessToken)
                    .url(context.getString(R.string.vote_url))
                    .post(RequestBody.create(MediaType.parse(context.getString(R.string.media_type_form_urlencoded)),
                            context.getString(R.string.dir) + vote + context.getString(R.string.id_param_name) + postFullname + context.getString(R.string.rank)))
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
