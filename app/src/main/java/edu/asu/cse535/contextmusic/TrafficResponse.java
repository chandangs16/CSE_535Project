package edu.asu.cse535.contextmusic;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.NotificationCompat;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by Shashank on 11/13/2016.
 */

public class TrafficResponse extends AsyncTask<String, String, String> {

    public MainActivity parentActivity;
    public double latitude;
    public double longitude;
    public Context context;
    private double lat1;
    private double lon1;
    private double lat2;
    private double lon2;
    private double latlen = 111111;

    TrafficResponse(MainActivity someActivity, double latitude, double longitude, Context context) {
        this.parentActivity = someActivity;
        this.latitude = latitude;
        this.longitude = longitude;
        this.context = context;
        calculateBounds(latitude, longitude);

    }

    private void calculateBounds(double latitude, double longitude) {
        this.lat1 = Math.round(50/latlen * 100000)/100000 + latitude;
        this.lat2 = latitude - Math.round(50/latlen * 100000)/ 100000;
        this.lon1 = (Math.round(50/latlen * 100000)/100000 * Math.cos(latitude)) + longitude;
        this.lon2 = longitude - (Math.round(50/latlen * 100000)/100000 * Math.cos(latitude));
    }

    @Override
    protected String doInBackground(String... params) {
        HttpURLConnection connection = null;
        BufferedReader buffReader = null;
        String jsonString = "";

        try {
            URL url = new URL("https://traffic.cit.api.here.com/traffic/6.2/flow.json?app_id=kRkwWeUfBKWLAfy2xre2&app_code=GoAwRFobZ2qCmvKYM6jJvg&bbox="+this.lat1+","+this.lon1+";"+this.lat2+","+this.lon2);
            connection = (HttpURLConnection) url.openConnection();
            connection.connect();
            InputStream inputStream = connection.getInputStream();
            buffReader = new BufferedReader(new InputStreamReader(inputStream));

            StringBuffer strBuff = new StringBuffer();
            String strLine = "";
            while ((strLine = buffReader.readLine()) != null) {
                strBuff.append(strLine);
            }

            jsonString = strBuff.toString();

        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
            try {
                if (buffReader != null) {
                    buffReader.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return jsonString;

    }

    @Override
    protected void onPostExecute(String strJsonObj) {
        // Initial Code.
        super.onPostExecute(strJsonObj);
        this.parentActivity.trafficInfo = new TrafficInfo(strJsonObj);
        System.out.println(this.parentActivity.trafficInfo.toJsonString());
        Toast.makeText(context, "Traffic: " + this.parentActivity.trafficInfo.traffic.toString(), Toast.LENGTH_SHORT).show();

        this.parentActivity.trafficInfo = new TrafficInfo(strJsonObj);
        System.out.println(this.parentActivity.trafficInfo.toJsonString());
        Toast.makeText(context, "Traffic: " + this.parentActivity.trafficInfo.traffic.toString(), Toast.LENGTH_SHORT).show();
        Intent intent = new Intent();
        PendingIntent pendingIntent = PendingIntent.getActivity(parentActivity,0,intent,0);
        android.support.v4.app.NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(parentActivity.getApplicationContext())
                        .setSmallIcon(R.drawable.traffic)
                        .setContentTitle("Traffic")
                        .setContentText(parentActivity.trafficInfo.traffic.toString().toUpperCase());

        mBuilder.setContentIntent(pendingIntent);

        NotificationManager nm = (NotificationManager) parentActivity.getSystemService(NOTIFICATION_SERVICE);
        nm.notify(2, mBuilder.build());
    }
}
