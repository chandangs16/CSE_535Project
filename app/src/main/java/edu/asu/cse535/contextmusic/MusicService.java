package edu.asu.cse535.contextmusic;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;

public class MusicService extends Service {

    MediaPlayer mediaPlayer;
    int musiclength = 0;
    boolean isPaused = false;
    private final IBinder musicBind = new MusicBinder();
    public MusicService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return musicBind;
    }

    @Override
    public void onCreate() {
        Toast.makeText(this, "hah", Toast.LENGTH_SHORT).show();
        mediaPlayer = new MediaPlayer().create(this, R.raw.second);
        mediaPlayer.setLooping(false);

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        return super.onStartCommand(intent, flags, startId);
    }

    public void pauseMusic() {
        mediaPlayer.pause();
        musiclength = mediaPlayer.getCurrentPosition();
        isPaused = true;
    }


    public void startMusic(final ConcurrentLinkedQueue<String> musicQueue) {

        if(isPaused == true) {
            mediaPlayer.seekTo(musiclength);
            mediaPlayer.start();
            isPaused = false;
        }
        else{
            Toast.makeText(this, "started", Toast.LENGTH_SHORT).show();
            mediaPlayer.stop();
            String nextSong = musicQueue.poll();
            String audioFilePath = Environment.getExternalStorageDirectory() + "/Music/" + nextSong + ".mp3";
            audioFilePath = audioFilePath.replace(" ", "_");
            Log.w("Audio File Selected" , audioFilePath);
            try {
//                FileInputStream is = new FileInputStream(audioFilePath);
                Log.w("hrere", audioFilePath);
                mediaPlayer.reset();
                mediaPlayer.setDataSource(audioFilePath);
                mediaPlayer.prepare();
                mediaPlayer.start();
                mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        nextMusic(musicQueue);
                    }
                });
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    @Override
    public void onDestroy() {
        Toast.makeText(this, "stopped", Toast.LENGTH_SHORT).show();
        mediaPlayer.stop();
    }

    public void nextMusic(ConcurrentLinkedQueue<String> musicQueue) {
        isPaused = false;
        Toast.makeText(this, "started", Toast.LENGTH_SHORT).show();
//        mediaPlayer.stop();
        String nextSong = musicQueue.poll();
        String audioFilePath = Environment.getExternalStorageDirectory() + "/Music/" + nextSong + ".mp3";
        audioFilePath = audioFilePath.replace(" ", "_");
        Log.w("here",audioFilePath);
        try {
            mediaPlayer.reset();
            mediaPlayer.setDataSource(audioFilePath);
            mediaPlayer.prepare();
            mediaPlayer.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public class MusicBinder extends Binder {
        MusicService getService() {
            return MusicService.this;
        }
    }

    @Override
    public boolean onUnbind(Intent intent){
        mediaPlayer.stop();
        mediaPlayer.release();
        return false;
    }
}

