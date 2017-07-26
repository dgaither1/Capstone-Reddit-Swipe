package com.dg.redditswipe.data;

import java.util.List;

/**
 * Created by mlc9433 on 7/26/17.
 */

public class RedditPostDO {

    private String title;
    private String imageUrl;
    private String body;
    private String url;
    private String poster;
    private String subreddit;
    private long score;
    private boolean isSelfPost;
    private List<RedditCommentDO> comments;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public String getSubreddit() {
        return subreddit;
    }

    public void setSubreddit(String subreddit) {
        this.subreddit = subreddit;
    }

    public long getScore() {
        return score;
    }

    public void setScore(long score) {
        this.score = score;
    }

    public boolean isSelfPost() {
        return isSelfPost;
    }

    public void setSelfPost(boolean selfPost) {
        isSelfPost = selfPost;
    }

    public List<RedditCommentDO> getComments() {
        return comments;
    }

    public void setComments(List<RedditCommentDO> comments) {
        this.comments = comments;
    }

}
