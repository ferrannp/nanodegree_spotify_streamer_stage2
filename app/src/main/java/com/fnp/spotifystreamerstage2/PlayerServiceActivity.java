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
import android.view.Menu;
import android.view.MenuItem;

import com.fnp.spotifystreamerstage2.player.PlayerDialogFragment;
import com.fnp.spotifystreamerstage2.player.PlayerInterface;
import com.fnp.spotifystreamerstage2.player.PlayerService;

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
    private boolean mShowNowPlayingButton = false;

    public class PlayerReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent != null){
                switch (intent.getAction()){
                    case PlayerService.PLAYER_ACTION:
                        PlayerDialogFragment fragment =
                                (PlayerDialogFragment) getSupportFragmentManager()
                                        .findFragmentByTag(PlayerDialogFragment.TAG);

                        if(fragment != null) {
                            fragment.togglePlayButton
                                    (intent.getBooleanExtra(getString(R.string.is_playing), false));
                        }

                        mShowNowPlayingButton =
                                intent.getBooleanExtra(getString(R.string.is_playing), false);
                        invalidateOptionsMenu(); //For the "Now Playing" button on the ActionBar
                        break;
                }
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIsLargeLayout = getResources().getBoolean(R.bool.dialog_fragment_large_layout);
        if(savedInstanceState != null){
            mCurrentTrackPosition = savedInstanceState.getInt(getString(R.string.track_position));
            mArtistSelectedName = savedInstanceState.getString(getString(R.string.artist_name_id));
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putInt(getString(R.string.track_position), mCurrentTrackPosition);
        savedInstanceState.putString(getString(R.string.artist_name_id), mArtistSelectedName);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        menu.findItem(R.id.action_player).setVisible(false); //Hidden by default
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mShowNowPlayingButton) {
            menu.findItem(R.id.action_player).setVisible(true);
        }
        else {
            menu.findItem(R.id.action_player).setVisible(false);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_player) {
            PlayerDialogFragment.newInstance()
                    .show(getSupportFragmentManager(), PlayerDialogFragment.TAG);
            return true;
        }

        return super.onOptionsItemSelected(item);
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
            //Reconnect to Service when player is opened (
            // Example: Configuration change, going back to the app...
            if(fragment != null) {
                fragment.togglePlayButton(mPlayerService.isPlaying());
            }

            mShowNowPlayingButton = mPlayerService.isPlaying();
            invalidateOptionsMenu(); //For the "Now Playing" button on the ActionBar
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    @Override
    public void onTrackSelected(int position) {
        mCurrentTrackPosition = position;
        String trackUrl = MainActivity.getNetworkFragment()
                .getTopTracksList().get(position).preview_url;

        if(mIsLargeLayout) {
            PlayerDialogFragment.newInstance()
                    .show(getSupportFragmentManager(), PlayerDialogFragment.TAG);
        }else{
            // The device is smaller, so show the fragment fullscreen
            FragmentTransaction transaction = getSupportFragmentManager()
                    .beginTransaction();
            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
            transaction.replace(R.id.content, PlayerDialogFragment.newInstance(),
                    PlayerDialogFragment.TAG).addToBackStack(null).commit();
        }

        Intent serviceIntent = new Intent(this, PlayerService.class);
        //Makes it play when starting
        serviceIntent.putExtra(getString(R.string.preview_url), trackUrl);
        startService(serviceIntent);
    }

    //Position in the list of the track we are playing
    public int getCurrentTrackPosition(){
        return mCurrentTrackPosition;
    }

    //Return the selected artist name in the list (used by our DialogFragment player)
    public String getSelectedArtistName(){
        return mArtistSelectedName;
    }

    /** From PlayerInterface */
    @Override
    public void playSong(int position) {
        mCurrentTrackPosition = position;

        //Maybe the service was already stopped (in this case, we stop if we played a full song)
        String trackUrl = MainActivity.getNetworkFragment()
                .getTopTracksList().get(position).preview_url;

        if(!mBound){
            Intent serviceIntent = new Intent(this, PlayerService.class);
            //Makes it play when starting
            serviceIntent.putExtra(getString(R.string.preview_url), trackUrl);
            startService(serviceIntent);
        }else {
            mPlayerService.playSong(trackUrl); //Immediately start playing :)
        }
    }

    @Override
    public boolean isPlaying(){
        return mPlayerService != null && mPlayerService.isPlaying();
    }
}
