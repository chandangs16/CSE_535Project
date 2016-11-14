package edu.asu.cse535.contextmusic;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by Shashank on 11/13/2016.
 */

public class TrafficInfo {

    public String traffic;
    private double jamFactor = 0;
    public TrafficInfo(String jsonString) {
        try{

            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray jsonArray = jsonObject.getJSONArray("RWS");

            for(int i =0; i< jsonArray.length(); i++) {
                JSONObject jObject = jsonArray.getJSONObject(i);
                JSONArray rw = jObject.getJSONArray("RW");
                for(int j=0; j < rw.length(); j++) {
                    JSONObject rwObj = rw.getJSONObject(j);
                    JSONArray fis = rwObj.getJSONArray("FIS");
                    for(int k=0; k< fis.length(); k++) {
                        JSONObject fisObj = fis.getJSONObject(k);
                        JSONArray fi = fisObj.getJSONArray("FI");
                        for(int m = 0; m< fi.length(); m++) {
                            JSONObject fiObj = fi.getJSONObject(m);
                            JSONArray cf = fiObj.getJSONArray("CF");
                            for(int n = 0; n< cf.length(); n++){
                                JSONObject cfObj = cf.getJSONObject(n);
                                jamFactor += cfObj.getDouble("JF");
                            }
                        }
                    }
                }
            }


            if(jamFactor <= 50) {
                traffic = "free road";
            }
            else if(jamFactor > 50 && jamFactor <= 80) {
                traffic = "lots of cars";
            }
            else if(jamFactor > 80) {
                traffic = "traffic jam";
            }
            else{
                traffic = null;
            }

        } catch (JSONException e) {
            System.out.println("error fetching the information for " +
                    this.getClass().getSimpleName());
        }
    }

    public String toJsonString() {
        String ret = "";
        try {
            JSONObject jo = new JSONObject();
            jo.put("traffic", this.traffic);
        } catch (Exception ex) {
            android.util.Log.w(this.getClass().getSimpleName(),
                    "error converting to/from json");
        }
        return ret;
    }
}