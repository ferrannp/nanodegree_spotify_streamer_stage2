package com.fnp.spotifystreamerstage2.player;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.fnp.spotifystreamerstage2.MainActivity;
import com.fnp.spotifystreamerstage2.NetworkFragment;
import com.fnp.spotifystreamerstage2.PlayerServiceActivity;
import com.fnp.spotifystreamerstage2.R;
import com.squareup.picasso.Picasso;

import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;

public class PlayerDialogFragment extends DialogFragment {
    private int mCurrentTrackPosition;

    public static PlayerDialogFragment newInstance() {
        return new PlayerDialogFragment();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt(getString(R.string.track_position), mCurrentTrackPosition);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player, container, false);

        TextView mArtistTitle = (TextView) view.findViewById(R.id.artist_textview);
        TextView mAlbumTitle = (TextView) view.findViewById(R.id.album_textview);
        ImageView mAlbumImage = (ImageView) view.findViewById(R.id.album_imageview);

        NetworkFragment networkFragment = MainActivity.getNetworkFragment();
        PlayerServiceActivity activity = ((PlayerServiceActivity) getActivity());

        Track track;
        if(activity.isPlayerServiceBound()) {
            mCurrentTrackPosition = activity.getCurrentTrackPosition();
            track = networkFragment.getTopTracksList().get(mCurrentTrackPosition);
        }else{
            //If player service is not bound yet means we are restoring from a configuration change
            mCurrentTrackPosition = savedInstanceState.getInt(getString(R.string.track_position));
            track = networkFragment.getTopTracksList()
                    .get(mCurrentTrackPosition);
        }

        //Load cover
        Image image = track.album.images.get(0);
        Picasso.with(activity)
                .load(image.url)
                .into(mAlbumImage);

        //Load data (text)
        mArtistTitle.setText(activity.getSelectedArtistName());
        mAlbumTitle.setText(track.album.name);


        return view;
    }
}
