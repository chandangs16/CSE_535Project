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


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setPermissions();

        callemotion();

        gps = new TrackGPS(MainActivity.this);
        dbController = new DatabaseController(MainActivity.this, getApplicationContext(), gps);
        dbController.addEmotion(this.emotion);
        b_get = (Button) findViewById(R.id.get_Loc);
        b_get.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // Start GPS Service.

                Toast.makeText(MainActivity.this, "listener", Toast.LENGTH_SHORT).show();


                if (gps.canGetLocation()) {
                    Toast.makeText(MainActivity.this, "inside if", Toast.LENGTH_SHORT).show();
                    longitude = gps.getLongitude();
                    latitude = gps.getLatitude();

                    Log.w("coord -> ", "Latitude ->" + latitude + " Longitude -> " + longitude);
                    Toast.makeText(getApplicationContext(), "Longitude:" + Double.toString(longitude) + "\nLatitude:" + Double.toString(latitude), Toast.LENGTH_SHORT).show();

                    dbController.addSong(gps.getLatitude(), gps.getLongitude());
                    // Start Weather Response Service.
                    new WeatherResponse(MainActivity.this, latitude, longitude, getApplicationContext()).execute();

                    // Start Traffic response Service.
                    new TrafficResponse(MainActivity.this, latitude, longitude, getApplicationContext()).execute();

                    //new SpeedometerResponse(MainActivity.this, gps, getApplicationContext()).execute();


                }
                else
                {
                    Log.w("gps: ","in else part");
                    gps.showSettingsAlert();
                }

            }
        });

        b_emo = (Button)findViewById(R.id.get_Emo);
        b_emo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                callemotion();
                dbController.addEmotion(MainActivity.this.emotion);

            }
        });

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


}