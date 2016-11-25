package edu.asu.cse535.contextmusic;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by Shashank on 11/11/2016.
 */

public class WeatherInfo {

    public String weather;

    public WeatherInfo(String jsonString){
        try{
            System.out.println(jsonString);
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray jsonArray = jsonObject.getJSONArray("weather");

            switch(jsonArray.getJSONObject(0).getString("main")){
                case "Thunderstorm" :
                    this.weather = "rainy";
                    break;
                case "Rain" :
                    this.weather = "rainy";
                    break;
                case "Snow":
                    this.weather = "snowing";
                    break;
                case "Clouds":
                    this.weather = "cloudy";
                case "Clear":
                    this.weather = "sunny";
                    break;
                case "Fog":
                    this.weather = "fog";
                    break;
                default: this.weather = "sunny";
                    break;
            };


        } catch (JSONException e) {
            System.out.println("error fetching the information for " +
                    this.getClass().getSimpleName());
        }
    }

    public String toJsonString() {
        String ret = "";
        try {
            JSONObject jo = new JSONObject();
            jo.put("weather", this.weather);
        }catch (Exception ex){
            android.util.Log.w(this.getClass().getSimpleName(),
                    "error converting to/from json");
        }
        return ret;
    }
}
