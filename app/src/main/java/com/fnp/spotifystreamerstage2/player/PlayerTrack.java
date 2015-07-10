package com.fnp.spotifystreamerstage2.player;

import android.os.Parcel;
import android.os.Parcelable;

import kaaes.spotify.webapi.android.models.Track;

/**
 * We use this class in our {@link PlayerService}. In this case, we don't want to relay on our
 * {@link com.fnp.spotifystreamerstage2.NetworkFragment} because a service can live much longer
 * than any Activity or Fragment
 */
public class PlayerTrack implements Parcelable {

    private String image;
    private String name;
    private String album_name;
    private String preview_url;

    public PlayerTrack(Track track){
        if(track.album.images.size() > 2){
            image = track.album.images.get(0).url;
        }else {
            image = track.album.images.get(1).url;
        }
        album_name = track.album.name;
        name = track.name;
        preview_url = track.preview_url;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(image);
        dest.writeString(album_name);
        dest.writeString(name);
        dest.writeString(preview_url);
    }

    public static final Parcelable.Creator<PlayerTrack> CREATOR
            = new Parcelable.Creator<PlayerTrack>() {
        public PlayerTrack createFromParcel(Parcel in) {
            return new PlayerTrack(in);
        }

        public PlayerTrack[] newArray(int size) {
            return new PlayerTrack[size];
        }
    };

    private PlayerTrack(Parcel in) {
        image = in.readString();
        album_name = in.readString();
        name = in.readString();
        preview_url = in.readString();
    }

    public String getImage() {
        return image;
    }

    public String getAlbum_name() {
        return album_name;
    }

    public String getName() {
        return name;
    }

    public String getPreview_url() {
        return preview_url;
    }
}
