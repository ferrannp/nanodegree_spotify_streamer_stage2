package com.fnp.spotifystreamerstage2.player;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.fnp.spotifystreamerstage2.MainActivity;
import com.fnp.spotifystreamerstage2.NetworkFragment;
import com.fnp.spotifystreamerstage2.PlayerServiceActivity;
import com.fnp.spotifystreamerstage2.R;
import com.squareup.picasso.Picasso;

import java.util.List;

import kaaes.spotify.webapi.android.models.Image;
import kaaes.spotify.webapi.android.models.Track;

public class PlayerDialogFragment extends DialogFragment implements View.OnClickListener,
        SeekBar.OnSeekBarChangeListener{

    public static final String TAG = "PlayerDialogFragment";
    private TextView mAlbumTitle, mTrackTitle, mStartTime, mEndTime;
    private ImageView mAlbumImage;
    private ImageButton mPlayButton;
    private SeekBar mSeekBar;
    private boolean mIgnoreTimer = false;
    private int mTotalDuration;

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
        mSeekBar = (SeekBar) view.findViewById(R.id.seekBar);

        mSeekBar.setOnSeekBarChangeListener(this);

        NetworkFragment networkFragment = MainActivity.getNetworkFragment();
        PlayerServiceActivity activity = ((PlayerServiceActivity) getActivity());

        int currentTrackPosition = activity.getCurrentTrackPosition();
        Track track = networkFragment.getTopTracksList().get(currentTrackPosition);

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
    }

    @Override
    public void onClick(View v) {
        PlayerInterface activity = (PlayerInterface) getActivity();
        switch (v.getId()) {
            case R.id.previous_button:
                activity.previousSong();
                break;
            case R.id.play_button:
                activity.playSong();
                break;
            case R.id.next_button:
                activity.nextSong();
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

    public void updateViewFromService(int position){
        List<Track> topTrackList = MainActivity.getNetworkFragment().getTopTracksList();
        updateDynamicView(topTrackList.get(position));
    }

    /** Seekbar operations and control */

    /**
     * @param duration in milliseconds
     */
    public void updateDuration(int duration){
        mEndTime.setText(String.format("%02d:%02d", ((duration / (1000*60)) % 60),
                (duration / 1000) % 60));

        mTotalDuration = duration;
        mSeekBar.setMax(duration / 1000);
    }

    /**
     * Position of the seekbar updated by {@link PlayerService}
     * @param elapsed time in milliseconds
     */
    public void updateElapsed(int elapsed){
        if(!mIgnoreTimer) {
            mStartTime.setText(String.format("%02d:%02d", ((elapsed / (1000 * 60)) % 60),
                    (elapsed / 1000) % 60));
            mSeekBar.setProgress(elapsed / 1000);
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if(fromUser) {
            int elapsed = progressToMilliseconds();
            Log.d("Player elapsed: ", String.valueOf(elapsed));
            mStartTime.setText(String.format("%02d:%02d", ((elapsed / (1000 * 60)) % 60),
                    (elapsed / 1000) % 60));
        }
    }

    public int progressToMilliseconds() {
        double val=((double)mSeekBar.getProgress())/((double)mSeekBar.getMax());
        return (int) (val * mTotalDuration);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        mIgnoreTimer = true; //Or user will be very angry :)

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        ((PlayerInterface) getActivity()).seekSong(seekBar.getProgress());
        mIgnoreTimer = false;
    }
}
