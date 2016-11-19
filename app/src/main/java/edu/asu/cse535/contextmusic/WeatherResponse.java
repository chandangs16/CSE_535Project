package edu.asu.cse535.contextmusic;

/**
 * Created by Shashank on 11/11/2016.
 */

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
    }

}

