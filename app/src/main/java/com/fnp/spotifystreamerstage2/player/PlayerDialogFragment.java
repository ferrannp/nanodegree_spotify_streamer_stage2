package com.fnp.spotifystreamerstage2.player;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.fnp.spotifystreamerstage2.MainActivity;
import com.fnp.spotifystreamerstage2.NetworkFragment;
import com.fnp.spotifystreamerstage2.PlayerServiceActivity;
import com.fnp.spotifystreamerstage2.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;

public class PlayerDialogFragment extends DialogFragment implements View.OnClickListener{

    public static final String TAG = "PlayerDialogFragment";
    private int mCurrentTrackPosition;
    private TextView mAlbumTitle, mTrackTitle, mStartTime, mEndTime;
    private ImageView mAlbumImage;
    private ImageButton mPlayButton;

    public static PlayerDialogFragment newInstance() {
        return new PlayerDialogFragment();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player, container, false);

        TextView mArtistTitle = (TextView) view.findViewById(R.id.artist_textview);
        mAlbumTitle = (TextView) view.findViewById(R.id.album_textview);
        mAlbumImage = (ImageView) view.findViewById(R.id.album_imageview);
        mTrackTitle = (TextView) view.findViewById(R.id.track_textview);
        mStartTime = (TextView) view.findViewById(R.id.start_time_textview);
        mEndTime = (TextView) view.findViewById(R.id.endt_time_textview);
        mPlayButton = (ImageButton) view.findViewById(R.id.play_button);

        NetworkFragment networkFragment = MainActivity.getNetworkFragment();
        PlayerServiceActivity activity = ((PlayerServiceActivity) getActivity());

        mCurrentTrackPosition = activity.getCurrentTrackPosition();
        Track track = networkFragment.getTopTracksList().get(mCurrentTrackPosition);

        mArtistTitle.setText(activity.getSelectedArtistName());
        updateDynamicView(track);

        togglePlayButton(activity.isPlaying());

        //Listeners
        view.findViewById(R.id.previous_button).setOnClickListener(this);
        view.findViewById(R.id.play_button).setOnClickListener(this);
        view.findViewById(R.id.next_button).setOnClickListener(this);

        return view;
    }

    private void updateDynamicView(Track track){
        //Load cover
        Image image = track.album.images.get(0);
        Picasso.with(getActivity())
                .load(image.url)
                .into(mAlbumImage);

        //Load data (text)
        mAlbumTitle.setText(track.album.name);
        mTrackTitle.setText(track.name);
        mStartTime.setText("00:00"); //Start from 0
        mEndTime.setText("--:--"); //We will get it from PlayerService when we load the preview_url
    }

    @Override
    public void onClick(View v) {
        PlayerInterface activity = (PlayerInterface) getActivity();
        List<Track> topTrackList = MainActivity.getNetworkFragment().getTopTracksList();
        switch (v.getId()) {
            case R.id.previous_button:
                mCurrentTrackPosition--;
                if(mCurrentTrackPosition < 0){
                    mCurrentTrackPosition = topTrackList.size() - 1;
                }
                updateDynamicView(topTrackList.get(mCurrentTrackPosition));
                activity.playSong(mCurrentTrackPosition);
                break;
            case R.id.play_button:
                activity.playSong(mCurrentTrackPosition);

                break;
            case R.id.next_button:
                mCurrentTrackPosition = (mCurrentTrackPosition + 1)
                        % MainActivity.getNetworkFragment().getTopTracksList().size();
                updateDynamicView(topTrackList.get(mCurrentTrackPosition));
                activity.playSong(mCurrentTrackPosition);
                break;
        }
    }

    public void togglePlayButton(boolean isPlaying) {
        if(isPlaying){
            mPlayButton.setImageDrawable(getResources()
                    .getDrawable(android.R.drawable.ic_media_pause));
        }else{
            mPlayButton.setImageDrawable(getResources()
                    .getDrawable(android.R.drawable.ic_media_play));
        }
    }
}
