package com.dg.redditswipe;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import com.google.android.gms.ads.MobileAds;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SubredditDataHandler.init();
        setContentView(R.layout.activity_main);
        MobileAds.initialize(this, getString(R.string.admob_app_id));
    }

    public void startSignIn(View view) {
        String url = String.format(getString(R.string.auth_url), getString(R.string.client_id), getString(R.string.state), getString(R.string.redirect_uri));
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(intent);
    }
}
