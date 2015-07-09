package com.fnp.spotifystreamerstage2.player;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.IBinder;
import android.widget.Toast;

import com.fnp.spotifystreamerstage2.R;

import java.io.IOException;

public class PlayerService extends Service implements MediaPlayer.OnCompletionListener {

    public final static String PLAYER_ACTION = "com.fnp.spotifystreamerstage2.player_action";
    private Intent broadcastIntent;
    private final IBinder mBinder = new PlayerBinder();
    private MediaPlayer mediaPlayer;

    private static final int STATE_PREPARED = 0;
    private static final int STATE_READY = 1;
    private static final int STATE_PAUSE = 2;
    private static final int STATE_STOP = 3;
    private int mCurrentPlayerState;
    private String mCurrentTrackUrl = "";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null){
            if(mediaPlayer == null){
                broadcastIntent = new Intent();
                broadcastIntent.setAction(PLAYER_ACTION);
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setOnCompletionListener(this);
                mCurrentPlayerState = STATE_PREPARED;
            }
            String previewURL = intent.getStringExtra(getString(R.string.preview_url));
            if(previewURL != null){
                if(mCurrentTrackUrl.equals(previewURL) //Just let it continue
                        && mCurrentPlayerState == STATE_READY && mediaPlayer.isPlaying()){
                    return START_STICKY_COMPATIBILITY;
                }
                playSong(previewURL);
            }
        }
        return START_STICKY_COMPATIBILITY;
    }

    public class PlayerBinder extends Binder {
        public PlayerService getService() {
            // Return this instance of LocalService so clients can call public methods
            return PlayerService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public boolean isPlaying(){
        return mCurrentPlayerState == STATE_READY;
    }

    /** Media player actions */

    /**
     * @param url the spotify preview url
     * @return true if playing false if pausing
     */
    public boolean playSong(String url){
        //We changed the song
        if(!mCurrentTrackUrl.equals(url)){
            mediaPlayer.reset();
            setDataSong(url);
            mCurrentTrackUrl = url;
        }

        if(mCurrentPlayerState == STATE_READY && mediaPlayer.isPlaying()){ //Already playing, pause!
            pauseSong();
            return false;
        }
        else{
            resumeSong();
            return true;
        }
    }

    private void setDataSong(String url){
        try {
            mediaPlayer.setDataSource(this, Uri.parse(url));
            mediaPlayer.prepare();
        } catch (IOException e) {
            Toast.makeText(this, getString(R.string.error_player_data), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            return;
        }

        mCurrentPlayerState = STATE_PREPARED;
    }

    private void pauseSong(){
        mediaPlayer.pause();
        mCurrentPlayerState = STATE_PAUSE;
        broadcastIntent.putExtra(getString(R.string.is_playing), false);
        broadcastIntent.putExtra(getString(R.string.unbind_player), false);
        sendBroadcast(broadcastIntent); //Warn our view
    }

    private void resumeSong(){
        mediaPlayer.start();
        mCurrentPlayerState = STATE_READY;
        broadcastIntent.putExtra(getString(R.string.is_playing), true);
        broadcastIntent.putExtra(getString(R.string.unbind_player), false);
        sendBroadcast(broadcastIntent); //Warn our view
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mCurrentPlayerState = STATE_STOP;
        broadcastIntent.putExtra(getString(R.string.is_playing), false);

        //In this case we stop the service if we complete a song, a more advance approach will
        //be to continue playing the next songs and don't stop the player till the user requires
        //it (from a notification or from some indicator inside the app)
        broadcastIntent.putExtra(getString(R.string.unbind_player), true);
        sendBroadcast(broadcastIntent); //Warn our view
        stopSelf();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }
}
