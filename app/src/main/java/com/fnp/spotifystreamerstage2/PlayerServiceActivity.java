package com.fnp.spotifystreamerstage2;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.fnp.spotifystreamerstage2.player.PlayerDialogFragment;
import com.fnp.spotifystreamerstage2.player.PlayerInterface;
import com.fnp.spotifystreamerstage2.player.PlayerService;
import com.fnp.spotifystreamerstage2.player.PlayerTrack;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.Track;

/**
 * This is the parent activity for any activity that needs to bound to
 * {@link com.fnp.spotifystreamerstage2.player.PlayerService}
 * The child activity MUST decide if it wants to listen for PlayerService changes
 *
 * @see com.fnp.spotifystreamerstage2.MainActivity
 * @see com.fnp.spotifystreamerstage2.TopTracksActivity
 */
public class PlayerServiceActivity extends AppCompatActivity implements
        TopTracksFragment.TopTracksCallback, PlayerInterface {

    protected boolean mBound = false;
    protected PlayerService mPlayerService;
    protected String mArtistSelectedName;
    private boolean mIsLargeLayout;
    private int mCurrentTrackPosition;
    protected IntentFilter mIntentFilterReceiver = new IntentFilter();
    protected PlayerReceiver mPlayerReceiver = new PlayerReceiver();

    //Communication between Service -> Activity -> Fragment
    public class PlayerReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                switch (intent.getAction()) {
                    case PlayerService.PLAYER_ACTION:
                        PlayerDialogFragment fragment =
                                (PlayerDialogFragment) getSupportFragmentManager()
                                        .findFragmentByTag(PlayerDialogFragment.TAG);

                        if (intent.hasExtra(getString(R.string.is_playing)) && fragment != null) {
                            fragment.togglePlayButton
                                    (intent.getBooleanExtra(getString(R.string.is_playing), false));
                        }

                        int position = intent.getIntExtra(getString(R.string.track_position), -1);
                        if (position != -1) {
                            mCurrentTrackPosition = position;
                            if (fragment != null) {
                                fragment.updateViewFromService(position);
                            }
                        }

                        int duration = intent.getIntExtra(getString(R.string.track_duration), -1);
                        if(duration != -1 && fragment != null){
                            fragment.updateDuration(duration);
                        }

                        int elapsed = intent.getIntExtra(getString(R.string.track_elapsed), -1);
                        if(elapsed != -1 && fragment != null){
                            fragment.updateElapsed(elapsed);
                        }

                        break;
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIsLargeLayout = getResources().getBoolean(R.bool.dialog_fragment_large_layout);
        if (savedInstanceState != null) {
            mArtistSelectedName = savedInstanceState.getString(getString(R.string.artist_name_id));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putString(getString(R.string.artist_name_id), mArtistSelectedName);
    }

    //Defines callbacks for service binding, passed to bindService()
    protected ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            PlayerService.PlayerBinder binder = (PlayerService.PlayerBinder) service;
            mPlayerService = binder.getService();
            mBound = true;

            PlayerDialogFragment fragment = (PlayerDialogFragment)
                    getSupportFragmentManager().findFragmentByTag(PlayerDialogFragment.TAG);

            // Reconnect to Service when player is opened
            // Example: Going back to the app when we played several songs
            mCurrentTrackPosition = mPlayerService.getCurrentTrackPosition();
            if (fragment != null) {
                fragment.togglePlayButton(mPlayerService.isPlaying());
                fragment.updateViewFromService(mCurrentTrackPosition);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    @Override
    public void onTrackSelected(int position) {
        mCurrentTrackPosition = position;

        showPlayer();

        Intent serviceIntent = new Intent(this, PlayerService.class);
        //Makes it play when starting
        serviceIntent.putParcelableArrayListExtra(getString(R.string.player_tracks),
                getSelectedTracks());
        serviceIntent.putExtra(getString(R.string.track_position), position);
        serviceIntent.setAction(PlayerService.ACTION_SELECT_NEW_SONG);
        startService(serviceIntent);
    }

    private ArrayList<PlayerTrack> getSelectedTracks() {
        ArrayList<PlayerTrack> playerTracks = new ArrayList<>();
        List<Track> topTracksList = MainActivity.getNetworkFragment().getTopTracksList();
        for (Track track : topTracksList) {
            playerTracks.add(new PlayerTrack(track));
        }
        return playerTracks;
    }

    //Position in the list of the track we are playing
    public int getCurrentTrackPosition() {
        return mCurrentTrackPosition;
    }

    //Return the selected artist name in the list (used by our DialogFragment player)
    public String getSelectedArtistName() {
        return mArtistSelectedName;
    }

    private void showPlayer() {
        if (mIsLargeLayout) {
            PlayerDialogFragment.newInstance()
                    .show(getSupportFragmentManager(), PlayerDialogFragment.TAG);
        } else {
            // The device is smaller, so show the fragment fullscreen
            if (getSupportFragmentManager().findFragmentByTag(PlayerDialogFragment.TAG) == null) {
                FragmentTransaction transaction = getSupportFragmentManager()
                        .beginTransaction();
                transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                transaction.replace(R.id.content, PlayerDialogFragment.newInstance(),
                        PlayerDialogFragment.TAG).addToBackStack(null).commit();
            }
        }
    }

    /**
     * From PlayerInterface
     */
    @Override
    public void playSong() {
        Intent serviceIntent = new Intent(this, PlayerService.class);
        if (isPlaying()) {
            serviceIntent.setAction(PlayerService.ACTION_PAUSE);
        } else {
            if (!mPlayerService.hasData()) { //We might have stopped it if we remove the notification
                serviceIntent.putParcelableArrayListExtra(getString(R.string.player_tracks),
                        getSelectedTracks());
                serviceIntent.putExtra(getString(R.string.track_position), mCurrentTrackPosition);
                serviceIntent.setAction(PlayerService.ACTION_SELECT_NEW_SONG);
            } else {
                serviceIntent.setAction(PlayerService.ACTION_PLAY);
            }
        }
        startService(serviceIntent);
    }

    @Override
    public void nextSong() {
        Intent serviceIntent = new Intent(this, PlayerService.class);
        serviceIntent.setAction(PlayerService.ACTION_NEXT);
        serviceIntent.putExtra(getString(R.string.track_position), mCurrentTrackPosition);
        startService(serviceIntent);

    }

    @Override
    public void previousSong() {
        Intent serviceIntent = new Intent(this, PlayerService.class);
        serviceIntent.setAction(PlayerService.ACTION_PREVIOUS);
        serviceIntent.putExtra(getString(R.string.track_position), mCurrentTrackPosition);
        startService(serviceIntent);
    }

    @Override
    public void seekSong(int elapsed){
        mPlayerService.seekSong(elapsed);
    }

    @Override
    public boolean isPlaying() {
        return mPlayerService != null && mPlayerService.isPlaying();
    }
}
