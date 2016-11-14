package edu.asu.cse535.contextmusic;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by Shashank on 11/13/2016.
 */

public class TrafficResponse extends AsyncTask<String, String, String> {

    public MainActivity someActivity;
    public double latitude;
    public double longitude;
    public Context context;
    private double lat1;
    private double lon1;
    private double lat2;
    private double lon2;
    private double latlen = 111111;

    TrafficResponse(MainActivity someActivity, double latitude, double longitude, Context context) {
        this.someActivity = someActivity;
        this.latitude = latitude;
        this.longitude = longitude;
        this.context = context;
        calculateBounds(33.424564, -111.928001);

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

        super.onPostExecute(strJsonObj);
        this.someActivity.trafficInfo = new TrafficInfo(strJsonObj);
        System.out.println(this.someActivity.trafficInfo.toJsonString());
        Toast.makeText(context, "Traffic: " + this.someActivity.trafficInfo.traffic.toString(), Toast.LENGTH_SHORT).show();
    }
}
