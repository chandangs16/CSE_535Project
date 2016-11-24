package edu.asu.cse535.contextmusic;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;

/**
 * Created by Shashank on 11/24/2016.
 */

public class SpeedometerResponse extends AsyncTask<String, String, String> implements IBaseGpsListener{

    public MainActivity parentActivity;
    TrackGPS gps;
    public Context context;
    public float currentSpeed;

    public SpeedometerResponse(MainActivity parentActivity, TrackGPS gps, Context context) {
        this.parentActivity = parentActivity;
        this.gps = gps;
        this.context = context;
    }

    private void updateSpeed(CLocation location) {
        // TODO Auto-generated method stub
        float nCurrentSpeed = 0;

        if(location != null)
        {
            location.setUseMetricunits(this.useMetricUnits());
            currentSpeed = location.getSpeed();
        }



    }

    private boolean useMetricUnits() {
        // TODO Auto-generated method stub
        return true;
    }

    @Override
    protected String doInBackground(String... strings) {
        float speedTest = 0;
        this.updateSpeed(null);
        for(int i = 0; i < 10; i++) {
            Criteria criteria = new Criteria();
            criteria.setAccuracy(Criteria.ACCURACY_FINE);
            criteria.setPowerRequirement(Criteria.POWER_LOW);

            String best = this.gps.locationManager.getBestProvider(criteria, true);
            this.updateSpeed(new CLocation(this.gps.locationManager.getLastKnownLocation(best)));

            Log.w("Speed -> ", String.valueOf(currentSpeed));
            speedTest += currentSpeed;
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return String.valueOf(speedTest/10);
    }

    @Override
    public void onLocationChanged(Location location) {
        if(location != null)
        {
            CLocation myLocation = new CLocation(location, this.useMetricUnits());
            this.updateSpeed(myLocation);
        }
    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    public void onProviderEnabled(String provider) {

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onGpsStatusChanged(int event) {

    }
}
