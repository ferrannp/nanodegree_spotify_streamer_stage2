package com.fnp.spotifystreamerstage2.player;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import com.fnp.spotifystreamerstage2.R;

public class PlayerService extends Service {

    public final static String PLAYER_INIT = "com.fnp.spotifystreamerstage2.player.init";

    private final IBinder mBinder = new PlayerBinder();
    private int mCurrentPosition;
    private boolean isPlaying;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null){
            mCurrentPosition = intent.getIntExtra(getString(R.string.track_position), 0);
            Intent broadcastIntent = new Intent();
            broadcastIntent.setAction(PLAYER_INIT);
            sendBroadcast(broadcastIntent);
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
        return isPlaying;
    }

    public int getCurrentPosition() {
        return mCurrentPosition;
    }
}
