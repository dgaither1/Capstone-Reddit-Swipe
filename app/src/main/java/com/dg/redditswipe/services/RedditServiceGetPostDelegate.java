package com.dg.redditswipe.services;

import com.dg.redditswipe.data.RedditPostDO;

/**
 * Created by mlc9433 on 7/26/17.
 */

public interface RedditServiceGetPostDelegate {

    void onGetPostSuccess(RedditPostDO post);
    void onGetPostFailure(String errorMessage);
}
