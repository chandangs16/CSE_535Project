package edu.asu.cse535.contextmusic;

import android.Manifest;
import android.app.Dialog;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Criteria;
import android.location.LocationManager;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.sql.SQLException;

import static java.lang.Thread.sleep;


public class MainActivity extends AppCompatActivity {

    private Button b_get, b_emo;
    private TrackGPS gps;
    double longitude;
    double latitude;
    WeatherInfo weatherInfo;
    TrafficInfo trafficInfo;

    public DatabaseController dbController;
    private String emotion = "";
    private MusicService musicSrv;
    private Intent playIntent;
    private boolean musicBound = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setPermissions();
        verifyStoragePermissions(this);

        callemotion();

        gps = new TrackGPS(MainActivity.this);
        dbController = new DatabaseController(MainActivity.this, getApplicationContext(), gps);
        dbController.addEmotion(this.emotion);
        b_get = (Button) findViewById(R.id.get_Loc);
        b_get.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Start GPS Service.
                setDefaults();

            }
        });

        b_emo = (Button) findViewById(R.id.get_Emo);
        b_emo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callemotion();
                dbController.addEmotion(MainActivity.this.emotion);

            }
        });

    }

    public void setDefaults() {

        //Toast.makeText(MainActivity.this, "listener", Toast.LENGTH_SHORT).show();


        if (gps.canGetLocation()) {
            //Toast.makeText(MainActivity.this, "inside if", Toast.LENGTH_SHORT).show();
            longitude = gps.getLongitude();
            latitude = gps.getLatitude();

            Log.w("coord -> ", "Latitude ->" + latitude + " Longitude -> " + longitude);
            //Toast.makeText(getApplicationContext(), "Longitude:" + Double.toString(longitude) + "\nLatitude:" + Double.toString(latitude), Toast.LENGTH_SHORT).show();

            // Start Weather Response Service.
            new WeatherResponse(MainActivity.this, latitude, longitude, getApplicationContext()).execute();

            // Start Traffic response Service.
            new TrafficResponse(MainActivity.this, latitude, longitude, getApplicationContext()).execute();

            //new SpeedometerResponse(MainActivity.this, gps, getApplicationContext()).execute();


        } else {
            Log.w("gps: ", "in else part");
            gps.showSettingsAlert();
        }
    }

    public void callemotion() {
        final Dialog dialog = new Dialog(MainActivity.this);
        dialog.setContentView(R.layout.custom_dialogue);
        dialog.setTitle("Title...");

        // set the custom dialog components - text, image and button

        Button dialogButtonHappy = (Button) dialog.findViewById(R.id.dialogButtonHappy);
        // if button is clicked, close the custom dialog
        dialogButtonHappy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emotion = "happy";
                dialog.dismiss();
            }
        });
        Button dialogButtonSad = (Button) dialog.findViewById(R.id.dialogButtonSad);
        // if button is clicked, close the custom dialog
        dialogButtonSad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emotion = "sad";
                dialog.dismiss();
            }
        });
        Button dialogButtonLazy = (Button) dialog.findViewById(R.id.dialogButtonLazy);
        // if button is clicked, close the custom dialog
        dialogButtonLazy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emotion = "lazy";
                dialog.dismiss();
            }
        });
        Button dialogButtonActive = (Button) dialog.findViewById(R.id.dialogButtonActive);
        // if button is clicked, close the custom dialog
        dialogButtonActive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                emotion = "active";
                dialog.dismiss();
            }
        });

        dialog.show();

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        gps.stopUsingGPS();
    }

    void setPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    2);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_NETWORK_STATE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_NETWORK_STATE},
                    3);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET},
                    3);
        }
    }

    public void play(View v) {
        setDefaults();
        dbController.addSong(gps.getLatitude(), gps.getLongitude());
        v.setVisibility(View.GONE);
        View pauseBtn = (findViewById(R.id.button_pause));
        pauseBtn.setVisibility(View.VISIBLE);
        musicSrv.startMusic(dbController.getQueue());
        dbController.addSong(gps.getLatitude(),gps.getLongitude());
    }

    public void pause(View v) {
        v.setVisibility(View.GONE);
        View playBtn = (findViewById(R.id.button_play));
        playBtn.setVisibility(View.VISIBLE);
        musicSrv.pauseMusic();
    }

    public void next(View v) {
        dbController.addSong(gps.getLatitude(),gps.getLongitude());
        musicSrv.nextMusic(dbController.getQueue());

    }

    @Override
    protected void onStart() {
        super.onStart();
        playIntent = new Intent(this, MusicService.class);
        ServiceConnection musicConnection = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
                //get service
                musicSrv = binder.getService();
                //pass list
                musicBound = true;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                musicBound = false;
            }
        };
        bindService(playIntent, musicConnection, Context.BIND_AUTO_CREATE);
        startService(playIntent);
    }

    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    /**
     * Checks if the app has permission to write to device storage
     *
     * If the app does not has permission then the user will be prompted to grant permissions
     *
     * @param activity
     */
    public static void verifyStoragePermissions(MainActivity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }


}