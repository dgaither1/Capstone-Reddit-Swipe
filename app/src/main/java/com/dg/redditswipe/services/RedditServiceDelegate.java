package com.dg.redditswipe.services;

/**
 * Created by mlc9433 on 7/26/17.
 */

public interface RedditServiceDelegate {

    void onSuccess(String serviceId);
    void onFailure(String serviceId, String errorMessage);
}
