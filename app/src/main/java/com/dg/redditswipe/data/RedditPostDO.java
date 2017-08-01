package com.dg.redditswipe.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mlc9433 on 7/26/17.
 */

public class RedditPostDO implements Parcelable {

    private String title;
    private String imageUrl;
    private String body;
    private String url;
    private String poster;
    private String subreddit;
    private String kind;
    private String id;
    private long score;
    private boolean isSelfPost;
    private boolean isNSFW;
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

    public String getKind() {
        return kind;
    }

    public void setKind(String kind) {
        this.kind = kind;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public boolean isNSFW() {
        return isNSFW;
    }

    public void setNSFW(boolean nsfw) {
        isNSFW = nsfw;
    }

    public List<RedditCommentDO> getComments() {
        return comments;
    }

    public void setComments(List<RedditCommentDO> comments) {
        this.comments = comments;
    }

    @Override
    public String toString() {
        return "RedditPostDO{" +
                "title='" + title + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", body='" + body + '\'' +
                ", url='" + url + '\'' +
                ", poster='" + poster + '\'' +
                ", subreddit='" + subreddit + '\'' +
                ", kind='" + kind + '\'' +
                ", id='" + id + '\'' +
                ", score=" + score +
                ", isSelfPost=" + isSelfPost +
                ", isNSFW=" + isNSFW +
                ", comments=" + comments +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(title);
        parcel.writeString(imageUrl);
        parcel.writeString(body);
        parcel.writeString(url);
        parcel.writeString(poster);
        parcel.writeString(subreddit);
        parcel.writeString(kind);
        parcel.writeString(id);
        parcel.writeLong(score);
        parcel.writeByte((byte) (isSelfPost ? 1: 0));
        parcel.writeByte((byte) (isNSFW ? 1: 0));
        parcel.writeTypedList(comments);
    }

    public RedditPostDO() {

    }

    private RedditPostDO(Parcel in) {
        title = in.readString();
        imageUrl = in.readString();
        body = in.readString();
        url = in.readString();
        poster = in.readString();
        subreddit = in.readString();
        kind = in.readString();
        id = in.readString();
        score = in.readLong();
        isSelfPost = in.readByte() == 1;
        isNSFW = in.readByte() == 1;

        comments = new ArrayList<>();
        in.readTypedList(comments, RedditCommentDO.CREATOR);
    }


    public static final Parcelable.Creator<RedditPostDO> CREATOR = new Parcelable.Creator<RedditPostDO>() {

        @Override
        public RedditPostDO createFromParcel(Parcel parcel) {
            return new RedditPostDO(parcel);
        }

        @Override
        public RedditPostDO[] newArray(int i) {
            return new RedditPostDO[i];
        }
    };
}
