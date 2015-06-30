package com.fnp.spotifystreamerstage2;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Based on Fragments without User Interface
 * https://www.udacity.com/course/viewer#!/c-ud853-nd/l-1623168625/m-1667758616
 * <p/>
 * Using setRetainInstance, our fragment won't be recreated (only its view, but because this is
 * a non UI fragment, that won't be a problem)
 * <p/>
 * With this, even when the Activity gets destroyed (ex: by rotation), our data will be kept
 */
public class NetworkFragment extends Fragment {

    private SpotifyService spotify;
    private List<Artist> mArtistList;
    private List<Track> mTopTracksList;
    private onArtistsResult onArtistsResult;
    private onTracksResult onTracksResult;
    private String mCurrentArtist = "";

    public interface onArtistsResult {
        void onNetworkSuccess();

        void onNetworkError(String message);
    }

    public interface onTracksResult {
        void onNetworkSuccess();

        void onNetworkError(String message);
    }

    public void setOnArtistsResult(onArtistsResult listener) {
        this.onArtistsResult = listener;
    }

    public void setOnTracksResult(onTracksResult listener) {
        this.onTracksResult = listener;
    }

    public NetworkFragment() {
        SpotifyApi api = new SpotifyApi();
        spotify = api.getService();
        mArtistList = new ArrayList<>();
        mTopTracksList = new ArrayList<>();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return null;
    }

    /**
     * Triggers a searchArtists call
     *
     * @param artist the artist query we want to search for
     */
    public void searchArtists(final String artist) {

        mCurrentArtist = artist;

        spotify.searchArtists(artist, new Callback<ArtistsPager>() {
            @Override
            public void success(final ArtistsPager artistsPager, Response response) {
                if (!artist.equals(mCurrentArtist)) {
                    //No need to handle the request
                    return;
                }

                mArtistList.clear(); //Clear old values

                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
                    mArtistList.addAll(artistsPager.artists.items);
                }else{
                    for(Artist artistItem:artistsPager.artists.items){
                        mArtistList.add(artistItem);
                    }
                }

                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        if (onArtistsResult != null) {
                            onArtistsResult.onNetworkSuccess();
                        }
                    }
                });
            }

            @Override
            public void failure(final RetrofitError error) {
                if (!artist.equals(mCurrentArtist)) {
                    //No need to handle the request
                    return;
                }

                //For better error message than generic one
                mArtistList.clear();
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        if (onArtistsResult != null) {
                            onArtistsResult.onNetworkError(error.getMessage());
                        }
                    }
                });
            }
        });
    }

    /**
     * Triggers a getArtistTopTrack call given an artistId
     *
     * @param artistId of the 10 top tracks we want search for
     */
    public void searchTopTracks(final String artistId) {

        HashMap<String, Object> countryMap = new HashMap<>();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        countryMap.put(getString(R.string.query_country), prefs.getString(getString(R.string.pref_country_key),
                getString(R.string.pref_country_default)));

        spotify.getArtistTopTrack(artistId, countryMap, new Callback<Tracks>() {
            @Override
            public void success(final Tracks tracks, Response response) {
                mTopTracksList.clear(); //Clear old values

                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
                    mTopTracksList.addAll(tracks.tracks);
                }else{
                    for(Track track:tracks.tracks){
                        mTopTracksList.add(track);
                    }
                }

                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        if (onTracksResult != null) {
                            onTracksResult.onNetworkSuccess();
                        }
                    }
                });
            }

            @Override
            public void failure(final RetrofitError error) {
                //For better error message than generic one (like Invalid country code)
                final SpotifyError spotifyError = SpotifyError.fromRetrofitError(error);
                mTopTracksList.clear();
                getActivity().runOnUiThread(new Runnable() {
                    public void run() {
                        if (onTracksResult != null) {
                            onTracksResult.onNetworkError(spotifyError.getMessage());
                        }
                    }
                });
            }
        });
    }

    /**
     * @return list of current found artists
     * @see kaaes.spotify.webapi.android.models.Artist
     */
    public List<Artist> getArtistList() {
        return mArtistList;
    }

    /**
     * @return list of current found 10 top tracks
     * @see kaaes.spotify.webapi.android.models.Tracks
     */
    public List<Track> getTopTracksList() {
        return mTopTracksList;
    }

    /**
     * @return the current artist type for the user
     */
    public String getCurrentArtistName (){
        return mCurrentArtist;
    }
}
