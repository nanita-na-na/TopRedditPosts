package com.example.toppostsreddit;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

public class RedditPost implements Parcelable{
    private String author;
    private String timeAgo;
    private String thumbnailUrl;
    private int numComments;

    public RedditPost(String author, String timeAgo, String thumbnailUrl, int numComments) {
        this.author = author;
        this.timeAgo = timeAgo;
        this.thumbnailUrl = thumbnailUrl;
        this.numComments = numComments;
    }

    protected RedditPost(Parcel in) {
        author = in.readString();
        timeAgo = in.readString();
        thumbnailUrl = in.readString();
        numComments = in.readInt();
    }

    public static final Parcelable.Creator<RedditPost> CREATOR = new Parcelable.Creator<RedditPost>() {
        @Override
        public RedditPost createFromParcel(Parcel in) {
            return new RedditPost(in);
        }

        @Override
        public RedditPost[] newArray(int size) {
            return new RedditPost[size];
        }
    };

    public String getAuthor() {
        return author;
    }

    public String getTimeAgo() {
        return timeAgo;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public int getNumComments() {
        return numComments;
    }

    public String getImageUrl() {
        // Возвращение URL изображения в большем формате
        // код для возврата URL изображения

        // TODO
        return null;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(@NonNull Parcel dest, int flags) {
        dest.writeString(author);
        dest.writeString(timeAgo);
        dest.writeString(thumbnailUrl);
        dest.writeInt(numComments);
    }
}
