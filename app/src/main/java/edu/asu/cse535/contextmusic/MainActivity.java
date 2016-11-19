package edu.asu.cse535.contextmusic;

import android.Manifest;
import android.app.Service;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    private Button b_get, b_emo;
    private TrackGPS gps;
    double longitude;
    double latitude;
    WeatherInfo weatherInfo;
    TrafficInfo trafficInfo;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    1);
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                    2);
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_NETWORK_STATE)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_NETWORK_STATE},
                    3);
        }

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.INTERNET},
                    3);
        }




        b_get = (Button)findViewById(R.id.get_Loc);

        b_get.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                gps = new TrackGPS(MainActivity.this);


                Toast.makeText(MainActivity.this, "listener", Toast.LENGTH_SHORT).show();
                if(gps.canGetLocation()){
                    Toast.makeText(MainActivity.this, "inside if", Toast.LENGTH_SHORT).show();
                    longitude = gps.getLongitude();
                    latitude = gps .getLatitude();

                    Log.w("coord -> ", "Latitude ->" + latitude + " Longitude -> " + longitude);
                    Toast.makeText(getApplicationContext(),"Longitude:"+Double.toString(longitude)+"\nLatitude:"+Double.toString(latitude),Toast.LENGTH_SHORT).show();

                    new WeatherResponse(MainActivity.this, latitude, longitude, getApplicationContext()).execute();
                    new TrafficResponse(MainActivity.this, latitude, longitude, getApplicationContext()).execute();
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


            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        gps.stopUsingGPS();
    }
}