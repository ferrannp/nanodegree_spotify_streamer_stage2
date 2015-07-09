package com.fnp.spotifystreamerstage2;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.fnp.spotifystreamerstage2.player.PlayerService;

public class TopTracksActivity extends PlayerServiceActivity implements
        NetworkFragment.onTracksResult {

    private static final String TOP_TRACK_FRAGMENT = "TopTrackFragment";
    private String mArtistId;
    private TopTracksFragment mTopTracksFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_tracks);

        mArtistId = getIntent().getStringExtra(getString(R.string.artist_id));
        mArtistSelectedName = getIntent().getStringExtra(getString(R.string.artist_name_id));

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setSubtitle(mArtistSelectedName);

        if (savedInstanceState == null) {
            mTopTracksFragment = new TopTracksFragment();

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.content, mTopTracksFragment, TOP_TRACK_FRAGMENT)
                    .commit();
        }else {
            mTopTracksFragment = (TopTracksFragment)
                    getSupportFragmentManager().findFragmentByTag(TOP_TRACK_FRAGMENT);
        }
    }

    @Override
    public void onStart(){
        super.onStart();
        //Listener for our NetworkFragment!
        MainActivity.getNetworkFragment().setOnTracksResult(this);
        //Bind to PlayerService
        Intent intent = new Intent(this, PlayerService.class);
        bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
        //Broadcast listener for PlayerService
        mIntentFilterReceiver.addAction(PlayerService.PLAYER_ACTION);
        registerReceiver(mPlayerReceiver, mIntentFilterReceiver);
    }

    @Override
    public void onStop(){
        super.onStop();
        MainActivity.getNetworkFragment().setOnTracksResult(null); //Stop listening
        //Unbind from the PlayerService
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
        //Broadcast listener for PlayerService
        mIntentFilterReceiver.addAction(PlayerService.PLAYER_ACTION);
        unregisterReceiver(mPlayerReceiver);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: //Back button
                if(getSupportFragmentManager().getBackStackEntryCount() > 0){
                //Our PlayerDialogFragment its opened in mobile
                    getSupportFragmentManager().popBackStack();
                }else {
                    finish();
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public String getArtistId(){
        return mArtistId;
    }

    @Override
    public void onTracksSuccess() {
        if(mTopTracksFragment != null) {
            mTopTracksFragment.onNetworkSuccess();
        }
    }

    @Override
    public void onTracksError(String message) {
        if(mTopTracksFragment != null) {
            mTopTracksFragment.onNetworkError(message);
        }
    }
}
