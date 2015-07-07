package com.fnp.spotifystreamerstage2;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Menu;
import android.view.MenuItem;

import com.fnp.spotifystreamerstage2.player.PlayerService;


public class MainActivity extends PlayerServiceActivity implements NetworkFragment.onArtistsResult,
        NetworkFragment.onTracksResult, ArtistFragment.ArtistSelectedCallback {

    private static NetworkFragment networkFragment;
    private ArtistFragment artistFragment;
    public static final String networkFragmentTAG = "NetworkFragment";
    private boolean mTwoPane;
    private static final String TOP_TRACKS_FRAGMENT_TAG = "TopTracksFragment";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        networkFragment = (NetworkFragment) getSupportFragmentManager()
                .findFragmentByTag(networkFragmentTAG);

        //Only the first time
        if (networkFragment == null) {
            // add the fragment
            networkFragment = new NetworkFragment();
            getSupportFragmentManager().beginTransaction()
                    .add(networkFragment, networkFragmentTAG).commit();
        }

        //Checking if we are in a Table or not (TwoPane)
        if (findViewById(R.id.top_tracks_container) != null) { //Tablet
            mTwoPane = true;
        } else {
            mTwoPane = false;
            getSupportActionBar().setElevation(0f);
        }

        artistFragment = (ArtistFragment)
                getSupportFragmentManager().findFragmentById(R.id.fragment);
    }

    @Override
    public void onStart() {
        super.onStart();
        //Listeners for our NetworkFragment!
        networkFragment.setOnArtistsResult(this);
        if (mTwoPane) {
            MainActivity.getNetworkFragment().setOnTracksResult(this);
            //Bind to PlayerService
            Intent intent = new Intent(this, PlayerService.class);
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
            //Broadcast listener for PlayerService
            mIntentFilterReceiver.addAction(PlayerService.PLAYER_INIT);
            registerReceiver(mPlayerReceiver, mIntentFilterReceiver);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        //Stop listening
        networkFragment.setOnArtistsResult(null);
        if (mTwoPane) {
            MainActivity.getNetworkFragment().setOnTracksResult(null);
            //Unbind from the PlayerService
            if (mBound) {
                unbindService(mConnection);
                mBound = false;
            }
            //Broadcast listener for PlayerService
            mIntentFilterReceiver.addAction(PlayerService.PLAYER_INIT);
            unregisterReceiver(mPlayerReceiver);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public static NetworkFragment getNetworkFragment() {
        return networkFragment;
    }

    @Override
    public void onArtistsSuccess() {
        artistFragment.onNetworkSuccess();
    }

    @Override
    public void onArtistsError(String message) {
        artistFragment.onNetworkError(message);
    }

    @Override
    public void onArtistSelected(String id, String name) { //For tablet
        mArtistSelectedName = name;

        if (mTwoPane) {
            Bundle args = new Bundle();
            TopTracksFragment fragment = new TopTracksFragment();
            args.putString(getString(R.string.artist_id), id);
            args.putString(getString(R.string.artist_name_id), name); //For toolbar subtitle
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.top_tracks_container, fragment, TOP_TRACKS_FRAGMENT_TAG)
                    .commit();
        } else {
            Intent intent = new Intent(this, TopTracksActivity.class);
            intent.putExtra(getString(R.string.artist_id), id);
            intent.putExtra(getString(R.string.artist_name_id), name); //For toolbar subtitle
            startActivity(intent);
        }
    }

    @Override
    public void onTracksSuccess() {
        if (mTwoPane) {
            getTopTracksFragment().onNetworkSuccess();
        }
    }

    @Override
    public void onTracksError(String message) {
        if (mTwoPane) {
            getTopTracksFragment().onNetworkError(message);
        }
    }

    private TopTracksFragment getTopTracksFragment() {
       return (TopTracksFragment) getSupportFragmentManager()
               .findFragmentByTag(TOP_TRACKS_FRAGMENT_TAG);
    }

    public boolean isTwoPane(){
        return mTwoPane;
    }

    //Simply remove the fragment if exists
    public void clearTracks(){
        Fragment fragment = getTopTracksFragment();
        if(fragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .remove(getTopTracksFragment()).commit();
        }
    }
}
