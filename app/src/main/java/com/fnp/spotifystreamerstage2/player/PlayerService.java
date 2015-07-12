package com.fnp.spotifystreamerstage2.player;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.fnp.spotifystreamerstage2.R;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.IOException;
import java.util.List;

public class PlayerService extends Service implements MediaPlayer.OnCompletionListener,
        MediaPlayer.OnPreparedListener{

    public final static String PLAYER_ACTION = "com.fnp.spotifystreamerstage2.player_action";
    private final IBinder mBinder = new PlayerBinder();
    private MediaPlayer mMediaPlayer;
    private MediaSessionCompat mSession;
    private MediaControllerCompat mController;
    private List<PlayerTrack> mPlayerTrackList;
    private Target mTarget;

    private static final int STATE_IDLE = -1;
    private static final int STATE_PREPARED = 0;
    private static final int STATE_READY = 1;
    private static final int STATE_PAUSE = 2;
    private static final int STATE_STOP = 3;
    private int mCurrentPlayerState = STATE_IDLE;
    private String mCurrentTrackUrl = "";
    private int mCurrentTrackPosition;

    public static final String ACTION_SELECT_NEW_SONG = "new_song_selected";
    public static final String ACTION_PLAY = "action_play";
    public static final String ACTION_PAUSE = "action_pause";
    public static final String ACTION_STOP = "action_stop";
    public static final String ACTION_NEXT = "action_next";
    public static final String ACTION_PREVIOUS = "action_previous";

    //For the Fragment SeekBar
    private Handler mPlayerHandler = new Handler();
    private Runnable mPlayerTimer = new Runnable() {
        @Override
        public void run() {
            if (mMediaPlayer != null) {
                Intent intent = new Intent();
                intent.setAction(PLAYER_ACTION);
                intent.putExtra(getString(R.string.track_elapsed),
                        mMediaPlayer.getCurrentPosition());
                sendBroadcast(intent);
                if (mCurrentPlayerState == STATE_READY && mMediaPlayer.isPlaying()) {
                    mPlayerHandler.postDelayed(this, 500);
                }
            }
        }
    };

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(intent != null){
            if(intent.hasExtra(getString(R.string.player_tracks))){ //User click into a track
                mPlayerTrackList = intent
                        .getParcelableArrayListExtra(getString(R.string.player_tracks));
            }

            int position = intent.getIntExtra(getString(R.string.track_position), -1);
            if(position != -1){
                mCurrentTrackPosition = position;
                mCurrentTrackUrl = mPlayerTrackList.get(mCurrentTrackPosition).getPreview_url();
            }

            if(mMediaPlayer == null) {
                initMedia();
            }

            handleIntent(intent);
        }
        return START_STICKY_COMPATIBILITY;
    }

    private void handleIntent(Intent intent){
        String action = intent.getAction();

        if(action.equalsIgnoreCase(ACTION_SELECT_NEW_SONG)){
            if(mCurrentPlayerState == STATE_READY || mCurrentPlayerState == STATE_PAUSE){
                mMediaPlayer.stop();
                mCurrentPlayerState = STATE_STOP;
            }
            mController.getTransportControls().play();
        }
        else if(action.equalsIgnoreCase(ACTION_PLAY)) {
            mController.getTransportControls().play();
        } else if(action.equalsIgnoreCase(ACTION_PAUSE)) {
            mController.getTransportControls().pause();
        } else if(action.equalsIgnoreCase(ACTION_PREVIOUS)) {
            mController.getTransportControls().skipToPrevious();
        } else if(action.equalsIgnoreCase(ACTION_NEXT )) {
            mController.getTransportControls().skipToNext();
        } else if( action.equalsIgnoreCase(ACTION_STOP)) {
            mController.getTransportControls().stop();
        }
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

    public boolean hasData(){
        return mPlayerTrackList != null;
    }

    public int getCurrentTrackPosition(){
        return mCurrentTrackPosition;
    }

    /** Media player actions */

    private void initMedia(){
        mMediaPlayer = new MediaPlayer();
        mSession = new MediaSessionCompat(getApplicationContext(), "Spotify Sample Session",
                new ComponentName(this, PlayerService.class), null);

        try {
            mController = new MediaControllerCompat(getApplicationContext(), mSession.getSessionToken());
        } catch (RemoteException e) {
            e.printStackTrace();
        }

        //Listeners
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnCompletionListener(this);

        mSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onPlay() {
                super.onPlay();
                Log.d("PlayerService", "onPlay");
                playSong(mCurrentTrackUrl);
                buildNotification(generateAction(android.R.drawable.ic_media_pause,
                        getString(R.string.action_pause), ACTION_PAUSE));
            }

            @Override
            public void onPause() {
                super.onPause();
                Log.d("PlayerService", "onPause");
                playSong(mCurrentTrackUrl);
                buildNotification(generateAction(android.R.drawable.ic_media_play,
                        getString(R.string.action_play), ACTION_PLAY));
            }

            @Override
            public void onSkipToNext() {
                super.onSkipToNext();
                Log.d("PlayerService", "onSkipToNext");
                if (mCurrentTrackPosition < (mPlayerTrackList.size() - 1)) {
                    String url = mPlayerTrackList.get(++mCurrentTrackPosition).getPreview_url();
                    mCurrentPlayerState = STATE_STOP;
                    playSong(url);
                    buildNotification(generateAction(android.R.drawable.ic_media_pause,
                            getString(R.string.action_pause), ACTION_PAUSE));

                    //Let activity know for the fragment to update its view
                    Intent intent = new Intent();
                    intent.setAction(PLAYER_ACTION);
                    intent.putExtra(getString(R.string.is_playing), true);
                    intent.putExtra(getString(R.string.track_position), mCurrentTrackPosition);
                    sendBroadcast(intent);
                }
                //Else do nothing (more ideal should be to disable the buttons)
            }

            @Override
            public void onSkipToPrevious() {
                super.onSkipToPrevious();
                Log.d("PlayerService", "onSkipToPrevious");
                if (mCurrentTrackPosition != 0) {
                    String url = mPlayerTrackList.get(--mCurrentTrackPosition).getPreview_url();
                    mCurrentPlayerState = STATE_STOP;
                    playSong(url);
                    buildNotification(generateAction(android.R.drawable.ic_media_pause,
                            getString(R.string.action_pause), ACTION_PAUSE));

                    //Let activity know for the fragment to update its view
                    Intent intent = new Intent();
                    intent.setAction(PLAYER_ACTION);
                    intent.putExtra(getString(R.string.is_playing), true);
                    intent.putExtra(getString(R.string.track_position), mCurrentTrackPosition);
                    sendBroadcast(intent);
                }
                //Else do nothing (more ideal should be to disable the buttons)
            }

            @Override
            public void onStop() {
                super.onStop();
                Log.d("PlayerService", "Stop service...");

                mPlayerHandler.removeCallbacksAndMessages(true);

                if (mMediaPlayer != null && mCurrentPlayerState == STATE_READY) {
                    mMediaPlayer.stop();
                }
                mCurrentPlayerState = STATE_STOP;
                if (mMediaPlayer != null) {
                    mMediaPlayer.reset();
                    mMediaPlayer.release();
                    mMediaPlayer = null;
                }
                Intent intent = new Intent();
                intent.setAction(PLAYER_ACTION);
                intent.putExtra(getString(R.string.is_playing), false);
                intent.putExtra(getString(R.string.track_elapsed), 0);
                sendBroadcast(intent);
                stopSelf(); //Stop service
            }
        });
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mCurrentPlayerState = STATE_PREPARED;
        Intent intent = new Intent();
        intent.setAction(PLAYER_ACTION);
        intent.putExtra(getString(R.string.track_duration), mMediaPlayer.getDuration());
        sendBroadcast(intent);
        playSong(mCurrentTrackUrl);
    }

    @Override
    public void onCompletion(MediaPlayer mp) {
        mPlayerHandler.removeCallbacksAndMessages(true);

        //If a song finishes, fire next one
        if(mCurrentTrackPosition < (mPlayerTrackList.size() - 1)) {
            mController.getTransportControls().skipToNext();
        }else{ //No next one
            mCurrentPlayerState = STATE_STOP;
            Intent intent = new Intent();
            intent.setAction(PLAYER_ACTION);
            intent.putExtra(getString(R.string.is_playing), false);
            sendBroadcast(intent);
            buildNotification(generateAction(android.R.drawable.ic_media_play,
                    getString(R.string.action_play), ACTION_PLAY));
        }
    }

    /**
     * @param url the spotify preview url
     */
    public void playSong(String url){
        //We changed the song
        if(mCurrentPlayerState == STATE_IDLE || mCurrentPlayerState == STATE_STOP){
            mMediaPlayer.reset();
            mCurrentTrackUrl = url;
            setDataSong(mCurrentTrackUrl);
            return;
        }

        if(mCurrentPlayerState == STATE_READY && mMediaPlayer.isPlaying()){ //Already playing, pause!
            pauseSong();
        }
        else{
            resumeSong();
        }
    }

    private void setDataSong(String url){
        try {
            mMediaPlayer.setDataSource(this, Uri.parse(url));
            mMediaPlayer.prepare();

            Intent intent = new Intent();
            intent.setAction(PLAYER_ACTION);
            intent.putExtra(getString(R.string.track_duration), mMediaPlayer.getDuration());
            sendBroadcast(intent);
        } catch (IOException e) {
            Toast.makeText(this, getString(R.string.error_player_data), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            return;
        }

        mCurrentPlayerState = STATE_PREPARED;
    }

    private void pauseSong(){
        mMediaPlayer.pause();
        mCurrentPlayerState = STATE_PAUSE;
        mPlayerHandler.removeCallbacksAndMessages(true);

        Intent intent = new Intent();
        intent.setAction(PLAYER_ACTION);
        intent.putExtra(getString(R.string.is_playing), false);
        intent.putExtra(getString(R.string.track_position), mCurrentTrackPosition);
        sendBroadcast(intent);
    }

    private void resumeSong(){
        mMediaPlayer.start();
        mCurrentPlayerState = STATE_READY;
        mPlayerHandler.post(mPlayerTimer);

        Intent intent = new Intent();
        intent.setAction(PLAYER_ACTION);
        intent.putExtra(getString(R.string.is_playing), true);
        intent.putExtra(getString(R.string.track_position), mCurrentTrackPosition);
        sendBroadcast(intent);
    }

    public void seekSong(int elapsed){
        if(mCurrentPlayerState == STATE_READY && mMediaPlayer.isPlaying()) {
            mPlayerHandler.removeCallbacksAndMessages(true);
            mMediaPlayer.seekTo(elapsed * 1000);
            mPlayerHandler.postDelayed(mPlayerTimer, 500);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if(mMediaPlayer != null) {
            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;
        }
        if(mPlayerHandler != null){
            mPlayerHandler.removeCallbacksAndMessages(true);
            mPlayerHandler = null;
        }
    }

    /** Notificatin part */

    private void buildNotification(NotificationCompat.Action action) {
        PlayerTrack playerTrack = mPlayerTrackList.get(mCurrentTrackPosition);

        NotificationCompat.MediaStyle style = new NotificationCompat.MediaStyle();

        Intent intent = new Intent(getApplicationContext(), PlayerService.class);
        intent.setAction(ACTION_STOP);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1,
                intent, 0);
        final NotificationCompat.Builder builder = new NotificationCompat.Builder(this);

        builder.setContentTitle(playerTrack.getName());
        builder.setContentText(playerTrack.getAlbum_name());
        builder.setDeleteIntent(pendingIntent);
        builder.setShowWhen(false);
        builder.setStyle(style);

        builder.setSmallIcon(android.R.drawable.ic_media_play);
        builder.addAction(generateAction(android.R.drawable.ic_media_previous,
                getString(R.string.action_previous), ACTION_PREVIOUS));
        builder.addAction(action);
        builder.addAction(generateAction(android.R.drawable.ic_media_next,
                getString(R.string.action_next), ACTION_NEXT));
        style.setShowActionsInCompactView(0, 1, 2);

        //Notification visibility (show controls on the lock screen or not)
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean show = prefs.getBoolean(getString(R.string.pref_notif_key), true);
        if(show){
            builder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        }
        else{
            builder.setVisibility(NotificationCompat.VISIBILITY_PRIVATE);
        }

        Picasso.Builder picassoBuilder = new Picasso.Builder(this);

        mTarget = new Target() {
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                builder.setLargeIcon(bitmap);
                NotificationManager notificationManager = (NotificationManager)
                        getSystemService(Context.NOTIFICATION_SERVICE);
                notificationManager.notify(1, builder.build());
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable) {
            }

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable) {
            }
        };

        picassoBuilder.build()
                .load(playerTrack.getImage()).into(mTarget);
    }

    private NotificationCompat.Action generateAction(int icon, String title, String intentAction) {
        Intent intent = new Intent(getApplicationContext(), PlayerService.class );
        intent.setAction(intentAction);
        PendingIntent pendingIntent = PendingIntent.getService(getApplicationContext(), 1, intent, 0);
        return new NotificationCompat.Action.Builder(icon, title, pendingIntent ).build();
    }

    @Override
    public boolean onUnbind(Intent intent) {
        if(mSession != null){
            mSession.release();
        }
        return super.onUnbind(intent);
    }
}
