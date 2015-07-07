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
import com.fnp.spotifystreamerstage2.player.PlayerService;

/**
 * This is the parent activity for any activity that needs to bound to
 * {@link com.fnp.spotifystreamerstage2.player.PlayerService}
 * The child activity MUST decide if it wants to listen for PlayerService changes
 *
 * @see com.fnp.spotifystreamerstage2.MainActivity
 * @see com.fnp.spotifystreamerstage2.TopTracksActivity
 */
public class PlayerServiceActivity extends AppCompatActivity implements TopTracksFragment.TopTracksCallback {

    protected boolean mBound = false;
    protected PlayerService mPlayerService;
    protected String mArtistSelectedName;
    protected PlayerReceiver mPlayerReceiver = new PlayerReceiver();
    protected IntentFilter mIntentFilterReceiver = new IntentFilter();
    private boolean mIsLargeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mIsLargeLayout = getResources().getBoolean(R.bool.dialog_fragment_large_layout);
    }

    public class PlayerReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent != null){
                switch (intent.getAction()){
                    case PlayerService.PLAYER_INIT:
                        if(mIsLargeLayout) {
                            PlayerDialogFragment.newInstance()
                                    .show(getSupportFragmentManager(), "PlayerDialog");
                        }else{
                            // The device is smaller, so show the fragment fullscreen
                            FragmentTransaction transaction = getSupportFragmentManager()
                                    .beginTransaction();
                            transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
                            transaction.replace(R.id.content, PlayerDialogFragment.newInstance())
                                    .addToBackStack(null).commit();
                        }
                        break;
                }
            }
        }
    }

    //Defines callbacks for service binding, passed to bindService()
    protected ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            PlayerService.PlayerBinder binder = (PlayerService.PlayerBinder) service;
            mPlayerService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mBound = false;
        }
    };

    @Override
    public void onTrackSelected(int position) {
        //Start service
        Intent intent = new Intent(this, PlayerService.class);
        intent.putExtra(getString(R.string.track_position), position);
        startService(intent);
    }

    //Position in the list of the track we are playing
    public int getCurrentTrackPosition(){
        return mPlayerService.getCurrentPosition();
    }

    //Return the selected artist name in the list (used by our DialogFragment player)
    public String getSelectedArtistName(){
        return mArtistSelectedName;
    }

    //Check if PlayerService is already bound
    public boolean isPlayerServiceBound(){
        return mBound;
    }
}
