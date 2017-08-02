package com.dg.redditswipe.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.RemoteViews;

import com.dg.redditswipe.R;
import com.dg.redditswipe.SubredditDataHandler;
import com.dg.redditswipe.data.RedditPostDO;
import com.dg.redditswipe.services.RedditServiceDelegate;
import com.dg.redditswipe.services.RedditServiceGetPostDelegate;
import com.dg.redditswipe.services.RedditServiceHandler;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Transformation;

/**
 * Created by mlc9433 on 7/31/17.
 */

public class WidgetProvider extends AppWidgetProvider implements RedditServiceGetPostDelegate, RedditServiceDelegate {

    RemoteViews remoteViews;
    Context context;
    int[] widgetIds;
    private AppWidgetManager appWidgetManager;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        this.context = context;
        this.widgetIds = appWidgetIds;
        this.appWidgetManager = appWidgetManager;
        this.remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget);

        loadNewPost(this.context);

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);

        if(intent.getAction().equalsIgnoreCase("upvote")) {

            RedditPostDO post = intent.getParcelableExtra("post");

            if(post != null) {
                RedditServiceHandler.voteOnPost(context.getString(R.string.vote_on_post), post.getKind() + "_" + post.getId(), "1", this, context);
            }

        } else if(intent.getAction().equalsIgnoreCase("downvote")) {
            RedditPostDO post = intent.getParcelableExtra("post");

            if(post != null) {
                RedditServiceHandler.voteOnPost(context.getString(R.string.vote_on_post), post.getKind() + "_" + post.getId(), "-1", this, context);
            }
        }

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        ComponentName thisAppWidget = new ComponentName(context.getPackageName(), WidgetProvider.class.getName());
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(thisAppWidget);

        onUpdate(context, appWidgetManager, appWidgetIds);

    }

    private void loadNewPost(Context context) {
        String subreddit = SubredditDataHandler.getRandomSelectedSubreddit();

        if(subreddit != null) {
            RedditServiceHandler.getRandomPostForSubreddit("getRandomPostForSubReddit", subreddit, this, context);
        } else {
            onGetPostFailure(context.getString(R.string.subreddits_not_found));
        }
    }


    @Override
    public void onGetPostSuccess(final RedditPostDO post) {
        if(post.getTitle() != null && !post.getTitle().isEmpty() && !post.isNSFW()) {

            remoteViews.setTextViewText(R.id.body, "");
            remoteViews.setImageViewBitmap(R.id.image_view, null);

            remoteViews.setTextViewText(R.id.title, post.getTitle());

            if (post.getBody() != null && !post.getBody().isEmpty()) {
                remoteViews.setTextViewText(R.id.body, post.getBody());
            }

            if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Picasso.with(WidgetProvider.this.context).load(post.getImageUrl()).transform(transformation).into(remoteViews, R.id.image_view, WidgetProvider.this.widgetIds);
                    }
                });
            }

            Intent upvoteIntent = new Intent(context, WidgetProvider.class);
            upvoteIntent.setAction("upvote");
            upvoteIntent.putExtra("post", post);
            PendingIntent upvotePendingIntent = PendingIntent.getBroadcast(context, widgetIds[0], upvoteIntent, 0);
            remoteViews.setOnClickPendingIntent(R.id.upvote, upvotePendingIntent);

            Intent downvoteIntent = new Intent(context, WidgetProvider.class);
            downvoteIntent.setAction("downvote");
            downvoteIntent.putExtra("post", post);
            PendingIntent downvotePendingIntent = PendingIntent.getBroadcast(context, widgetIds[0], downvoteIntent, 0);
            remoteViews.setOnClickPendingIntent(R.id.downvote, downvotePendingIntent);

            Intent refreshIntent = new Intent(context, WidgetProvider.class);
            refreshIntent.setAction("refresh");
            refreshIntent.putExtra("post", post);
            PendingIntent refreshPendingIntent = PendingIntent.getBroadcast(context, widgetIds[0], refreshIntent, 0);
            remoteViews.setOnClickPendingIntent(R.id.refresh, refreshPendingIntent);

            final int count = this.widgetIds.length;

            for (int i = 0; i < count; i++) {
                int widgetId = this.widgetIds[i];
                appWidgetManager.updateAppWidget(widgetId, remoteViews);
            }

        } else {
            onGetPostFailure(context.getString(R.string.post_unavailable));
        }
    }

    @Override
    public void onGetPostFailure(String errorMessage) {
        Log.d("DG", "Post failed to load with errorMessage = " + errorMessage);

        remoteViews.setTextViewText(R.id.title, context.getString(R.string.please_login));
        remoteViews.setTextViewText(R.id.body, "");
        remoteViews.setImageViewBitmap(R.id.image_view, null);

        Intent refreshIntent = new Intent(context, WidgetProvider.class);
        refreshIntent.setAction("refresh");
        PendingIntent refreshPendingIntent = PendingIntent.getBroadcast(context, 0, refreshIntent, 0);
        remoteViews.setOnClickPendingIntent(R.id.refresh, refreshPendingIntent);

        final int count = this.widgetIds.length;

        for (int i = 0; i < count; i++) {
            int widgetId = this.widgetIds[i];
            appWidgetManager.updateAppWidget(widgetId, remoteViews);
        }
    }

    @Override
    public void onSuccess(String serviceId) {

    }

    @Override
    public void onFailure(String serviceId, String errorMessage) {

    }

    private Transformation transformation = new Transformation() {
        @Override
        public Bitmap transform(Bitmap source) {
            int targetWidth = context.getResources().getDisplayMetrics().widthPixels;
            if (source.getWidth() > targetWidth)
            {
                double aspectRatio = (double) source.getHeight() / (double) source.getWidth();
                int targetHeight = (int) (targetWidth * aspectRatio);
                Bitmap result = Bitmap.createScaledBitmap(source, targetWidth, targetHeight, false);
                if (result != source)
                {
                    source.recycle();
                }
                return result;
            }
            return source;
        }

        @Override
        public String key() {
            return "transformation" + " desiredWidth";
        }
    };
}
