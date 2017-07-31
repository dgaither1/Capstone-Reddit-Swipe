package com.dg.redditswipe.widget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
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

    private void loadNewPost(Context context) {
        String subreddit = SubredditDataHandler.getRandomSelectedSubreddit();

        if(subreddit != null) {
            RedditServiceHandler.getRandomPostForSubreddit("getRandomPostForSubReddit", subreddit, this, context);
        }
    }


    @Override
    public void onGetPostSuccess(final RedditPostDO post) {
        if(post.getTitle() != null && !post.getTitle().isEmpty() && !post.isNSFW()) {

            remoteViews.setTextViewText(R.id.title, post.getTitle());

            if (post.getBody() != null && !post.getBody().isEmpty()) {
                remoteViews.setTextViewText(R.id.body, post.getBody());
            }

            if (post.getImageUrl() != null && !post.getImageUrl().isEmpty()) {

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Picasso.with(WidgetProvider.this.context).load(post.getImageUrl()).into(remoteViews, R.id.image_view, WidgetProvider.this.widgetIds);
                    }
                });
            }

            Intent intent = new Intent(context, WidgetProvider.class);
            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, this.widgetIds);
//            PendingIntent pendingIntent = PendingIntent.getBroadcast(context,
//                    0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
//            remoteViews.setOnClickPendingIntent(R.id.actionButton, pendingIntent);

            final int count = this.widgetIds.length;

            for (int i = 0; i < count; i++) {
                int widgetId = this.widgetIds[i];
                appWidgetManager.updateAppWidget(widgetId, remoteViews);
            }

        } else {
            loadNewPost(this.context);
        }
    }

    @Override
    public void onGetPostFailure(String errorMessage) {
        Log.d("DG", "Post failed to load with errorMessage = " + errorMessage);

        loadNewPost(this.context);
    }

    @Override
    public void onSuccess(String serviceId) {

    }

    @Override
    public void onFailure(String serviceId, String errorMessage) {

    }
}
