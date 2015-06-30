package com.fnp.spotifystreamerstage2;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;


public class MainActivity extends AppCompatActivity implements NetworkFragment.onArtistsResult{

    private static NetworkFragment networkFragment;
    private ArtistFragment artistFragment;
    public static final String networkFragmentTAG = "NetworkFragment";

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

        artistFragment = (ArtistFragment)
                getSupportFragmentManager().findFragmentById(R.id.fragment);

    }

    @Override
    public void onStart(){
        super.onStart();
        networkFragment.setOnArtistsResult(this); //Listener for our NetworkFragment!
    }

    @Override
    public void onStop(){
        super.onStop();
        networkFragment.setOnArtistsResult(null); //Stop listening
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

    public static NetworkFragment getNetworkFragment(){
        return networkFragment;
    }

    @Override
    public void onNetworkSuccess() {
        artistFragment.onNetworkSuccess();
    }

    @Override
    public void onNetworkError(String message) {
        artistFragment.onNetworkError(message);
    }
}
