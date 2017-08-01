package com.dg.redditswipe.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mlc9433 on 7/26/17.
 */

public class RedditCommentDO implements Parcelable {

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

    public RedditCommentDO() {

    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(title);
        parcel.writeString(body);
        parcel.writeString(poster);
        parcel.writeString(subreddit);
        parcel.writeLong(score);
        parcel.writeTypedList(childComments);
    }

    private RedditCommentDO(Parcel in) {
        title = in.readString();
        body = in.readString();
        poster = in.readString();
        subreddit = in.readString();
        score = in.readLong();

        childComments = new ArrayList<>();
        in.readTypedList(childComments, RedditCommentDO.CREATOR);
    }


    public static final Parcelable.Creator<RedditCommentDO> CREATOR = new Parcelable.Creator<RedditCommentDO>() {

        @Override
        public RedditCommentDO createFromParcel(Parcel parcel) {
            return new RedditCommentDO(parcel);
        }

        @Override
        public RedditCommentDO[] newArray(int i) {
            return new RedditCommentDO[i];
        }
    };
}
