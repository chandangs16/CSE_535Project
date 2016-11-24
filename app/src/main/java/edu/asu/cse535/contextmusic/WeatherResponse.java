package edu.asu.cse535.contextmusic;

/**
 * Created by Shashank on 11/11/2016.
 */

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;

import static android.content.Context.NOTIFICATION_SERVICE;

/*
The Response class extends an AsyncTask class in order to process an asynchronus thread in the background
of the main UI thread. Here we fetch the OMDB API response and instantiate a MovieDescription object with
 a string argument of the JSON Object order to retrieve the fields of the JSON response.
 */
public class WeatherResponse extends AsyncTask<String, String, String> {

    public MainActivity someActivity;
    public double latitude;
    public double longitude;
    public Context context;
    Calendar c = Calendar.getInstance();
    int hour = c.get(Calendar.HOUR_OF_DAY);

    WeatherResponse(MainActivity someActivity, double latitude, double longitude, Context context) {
        this.someActivity = someActivity;
        this.latitude = latitude;
        this.longitude = longitude;
        this.context = context;
    }

    @Override
    protected String doInBackground(String... params) {
        HttpURLConnection connection = null;
        BufferedReader buffReader = null;
        String jsonString = "";

        try {
            URL url = new URL("http://api.openweathermap.org/data/2.5/weather?lat=" + latitude + "&lon=" + longitude + "&appid=845af45e3631fb03e93342ab8d2f7b4c");
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
        this.someActivity.weatherInfo = new WeatherInfo(strJsonObj);
        System.out.println(this.someActivity.weatherInfo.toJsonString());
        Toast.makeText(context, "Weather: " + this.someActivity.weatherInfo.weather.toString(), Toast.LENGTH_SHORT).show();
        Intent intent = new Intent();
        PendingIntent pendingIntent = PendingIntent.getActivity(someActivity,0,intent,0);
        android.support.v4.app.NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(someActivity.getApplicationContext())
                        .setSmallIcon(R.drawable.weather)
                        .setContentTitle("Weather")
                        .setContentText(someActivity.weatherInfo.weather.toString().toUpperCase());

        mBuilder.setContentIntent(pendingIntent);

        NotificationManager nm = (NotificationManager) someActivity.getSystemService(NOTIFICATION_SERVICE);
        nm.notify(1, mBuilder.build());

        if (isBetween(hour, 5, 18)) {

            if (this.someActivity.weatherInfo.weather.toString() == "rainy") {
                RelativeLayout mLinearLayout = (RelativeLayout) someActivity.findViewById(R.id.activity_main);
                mLinearLayout.setBackgroundResource(R.drawable.morning_rainy);
            } else if (this.someActivity.weatherInfo.weather.toString() == "snowing") {
                RelativeLayout mLinearLayout = (RelativeLayout) someActivity.findViewById(R.id.activity_main);
                mLinearLayout.setBackgroundResource(R.drawable.morning_snowy);
            } else if (this.someActivity.weatherInfo.weather.toString() == "cloudy") {
                RelativeLayout mLinearLayout = (RelativeLayout) someActivity.findViewById(R.id.activity_main);
                mLinearLayout.setBackgroundResource(R.drawable.morning_cloudy);
            } else if (this.someActivity.weatherInfo.weather.toString() == "sunny") {
                RelativeLayout mLinearLayout = (RelativeLayout) someActivity.findViewById(R.id.activity_main);
                mLinearLayout.setBackgroundResource(R.drawable.morning_sunny);
            } else {
                RelativeLayout mLinearLayout = (RelativeLayout) someActivity.findViewById(R.id.activity_main);
                mLinearLayout.setBackgroundResource(R.drawable.sunrise);
            }
        } else if (isBetween(hour, 19, 23) || isBetween(hour, 0, 5)) {
            /*RelativeLayout mLinearLayout = (RelativeLayout) findViewById(R.id.activity_main);
            mLinearLayout.setBackgroundResource(R.drawable.night);*/
            try {
                Log.w("check:", this.someActivity.weatherInfo.weather.toString());

                if (this.someActivity.weatherInfo.weather == "rainy") {
                    RelativeLayout mLinearLayout = (RelativeLayout) this.someActivity.findViewById(R.id.activity_main);
                    mLinearLayout.setBackgroundResource(R.drawable.night_rainy);
                } else if (this.someActivity.weatherInfo.weather.toString() == "snowing") {
                    RelativeLayout mLinearLayout = (RelativeLayout) this.someActivity.findViewById(R.id.activity_main);
                    mLinearLayout.setBackgroundResource(R.drawable.night_snowy);
                } else if (this.someActivity.weatherInfo.weather.toString() == "cloudy") {
                    RelativeLayout mLinearLayout = (RelativeLayout) this.someActivity.findViewById(R.id.activity_main);
                    mLinearLayout.setBackgroundResource(R.drawable.night_cloudy);
                } else {
                    RelativeLayout mLinearLayout = (RelativeLayout) this.someActivity.findViewById(R.id.activity_main);
                    mLinearLayout.setBackgroundResource(R.drawable.night);
                }

            } catch (NullPointerException e) {
                Log.v("inside exception:", "sdsds");
            }
        }
    }

        public static boolean isBetween(int x, int lower, int upper) {
            return lower <= x && x <= upper;
        }
}

