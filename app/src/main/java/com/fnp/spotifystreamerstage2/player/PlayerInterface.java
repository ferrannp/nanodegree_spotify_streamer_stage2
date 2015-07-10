package com.fnp.spotifystreamerstage2.player;

public interface PlayerInterface {

    void playSong();
    void nextSong();
    void previousSong();
    void seekSong(int elapsed);
    boolean isPlaying();
}
