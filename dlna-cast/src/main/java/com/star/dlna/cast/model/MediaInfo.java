package com.star.dlna.cast.model;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 媒体信息
 */
public class MediaInfo implements Parcelable {

    private String id;
    private String title;
    private String artist;
    private String album;
    private Uri uri;
    private String mimeType;
    private long duration;
    private String thumbnailUrl;
    private MediaType mediaType;

    public enum MediaType {
        VIDEO,
        AUDIO,
        IMAGE
    }

    public MediaInfo() {
    }

    protected MediaInfo(Parcel in) {
        id = in.readString();
        title = in.readString();
        artist = in.readString();
        album = in.readString();
        uri = in.readParcelable(Uri.class.getClassLoader());
        mimeType = in.readString();
        duration = in.readLong();
        thumbnailUrl = in.readString();
        mediaType = MediaType.values()[in.readInt()];
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(artist);
        dest.writeString(album);
        dest.writeParcelable(uri, flags);
        dest.writeString(mimeType);
        dest.writeLong(duration);
        dest.writeString(thumbnailUrl);
        dest.writeInt(mediaType != null ? mediaType.ordinal() : -1);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<MediaInfo> CREATOR = new Creator<MediaInfo>() {
        @Override
        public MediaInfo createFromParcel(Parcel in) {
            return new MediaInfo(in);
        }

        @Override
        public MediaInfo[] newArray(int size) {
            return new MediaInfo[size];
        }
    };

    // Builder 模式
    public static class Builder {
        private MediaInfo mediaInfo = new MediaInfo();

        public Builder setId(String id) {
            mediaInfo.id = id;
            return this;
        }

        public Builder setTitle(String title) {
            mediaInfo.title = title;
            return this;
        }

        public Builder setArtist(String artist) {
            mediaInfo.artist = artist;
            return this;
        }

        public Builder setAlbum(String album) {
            mediaInfo.album = album;
            return this;
        }

        public Builder setUri(Uri uri) {
            mediaInfo.uri = uri;
            return this;
        }

        public Builder setMimeType(String mimeType) {
            mediaInfo.mimeType = mimeType;
            return this;
        }

        public Builder setDuration(long duration) {
            mediaInfo.duration = duration;
            return this;
        }

        public Builder setThumbnailUrl(String thumbnailUrl) {
            mediaInfo.thumbnailUrl = thumbnailUrl;
            return this;
        }

        public Builder setMediaType(MediaType mediaType) {
            mediaInfo.mediaType = mediaType;
            return this;
        }

        public MediaInfo build() {
            return mediaInfo;
        }
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    public String getMimeType() {
        return mimeType;
    }

    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    public void setMediaType(MediaType mediaType) {
        this.mediaType = mediaType;
    }

    @NonNull
    @Override
    public String toString() {
        return "MediaInfo{" +
                "title='" + title + '\'' +
                ", uri=" + uri +
                ", mediaType=" + mediaType +
                '}';
    }
}
