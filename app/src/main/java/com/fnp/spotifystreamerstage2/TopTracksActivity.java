package com.fnp.spotifystreamerstage2;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;

import java.util.List;

import kaaes.spotify.webapi.android.models.Track;

public class TopTracksActivity extends AppCompatActivity implements NetworkFragment.onTracksResult{

    private String artistId, artistName;
    private TopTracksFragment topTracksFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_tracks);

        artistId = getIntent().getStringExtra(getString(R.string.artist_id));
        artistName = getIntent().getStringExtra(getString(R.string.artist_name_id));

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setSubtitle(artistName);

        topTracksFragment = (TopTracksFragment)
                getSupportFragmentManager().findFragmentById(R.id.fragment);
    }

    @Override
    public void onStart(){
        super.onStart();
        //Listener for our NetworkFragment!
        MainActivity.getNetworkFragment().setOnTracksResult(this);
    }

    @Override
    public void onStop(){
        super.onStop();
        MainActivity.getNetworkFragment().setOnTracksResult(null); //Stop listening
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home: //Back button
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public String getArtistId(){
        return artistId;
    }

    public String getArtistName(){
        return artistName;
    }

    public List<Track> getTopTracksList(){
        return MainActivity.getNetworkFragment().getTopTracksList();
    }

    @Override
    public void onNetworkSuccess() {
        topTracksFragment.onNetworkSuccess();
    }

    @Override
    public void onNetworkError(String message) {
        topTracksFragment.onNetworkError(message);
    }
}
