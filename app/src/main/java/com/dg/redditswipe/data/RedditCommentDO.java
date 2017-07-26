package com.dg.redditswipe.data;

import java.util.List;

/**
 * Created by mlc9433 on 7/26/17.
 */

public class RedditCommentDO {

    private String title;
    private String body;
    private String poster;
    private String subreddit;
    private long score;
    private List<RedditCommentDO> childComments;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
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

    public List<RedditCommentDO> getChildComments() {
        return childComments;
    }

    public void setChildComments(List<RedditCommentDO> childComments) {
        this.childComments = childComments;
    }
}
