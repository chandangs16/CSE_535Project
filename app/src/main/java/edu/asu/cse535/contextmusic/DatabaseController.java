/*
 * Copyright 2016 Sohan Madhav Bangaru,  
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); 
 * you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at  http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software distributed 
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR
 *  CONDITIONS OF ANY KIND, either express or implied. See the License for the 
 * specific language governing permissions and limitations under the License.  
 *
 * SER598 Mobile Systems (Spring 2016) 
 * @author Sohan Madhav Bangaru  mailto:sohan.bangaru@asu.edu 
 *                               Graduate Student, Software Engineering, CIDSE, ASU  
 * @version 3/28/16 10:07 AM
 */

package edu.asu.cse535.contextmusic;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.location.Criteria;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * This Class is used to initialize Database for the Application.
 */
public class DatabaseController extends SQLiteOpenHelper {

    // Class Members
    private static final boolean debugon = false;
    private static final int DATABASE_VERSION = 3;
    private static String dbName = "app_context";
    private String dbPath;
    private SQLiteDatabase modelDB;
    private final Context dbContext;
    private MainActivity parentActivity;
    private TrackGPS gps;
    private ArrayList<String> choiceList = new ArrayList<>();
    public ConcurrentLinkedQueue<String> musicQueue = new ConcurrentLinkedQueue<String>();
    private String emotion = "happy";
    double longitude;
    double latitude;


//    public Handler databaseHandler = new Handler() {
//        @Override
//        public void handleMessage(Message msg) {
//            Toast.makeText(parentActivity, "Call for Handler Function",Toast.LENGTH_SHORT).show();
//        }
//    };

    public Handler databaseHandler = new Handler();
    public Runnable addMusicDataToQueue = new Runnable() {
        @Override
        public void run() {
            // TODO Auto-generated method stub
            Log.v("Thread -Check", "Running the Thread - Sohan");
            //musicQueue.add("FirstFile");
            //musicQueue.add("SecondFile");
            Random random = new Random();
            int randomIndex = random.nextInt(choiceList.size());
            musicQueue.add(choiceList.get(randomIndex));
            Log.w("music queue -> " , musicQueue.peek() + "~~~~" + musicQueue.size());
            choiceList = new ArrayList<>();
        }
    };


    // Constructors
    public DatabaseController(MainActivity parentActivity, Context context, TrackGPS gps) {
        super(context, dbName, null, DATABASE_VERSION);

        dbContext = context;
        dbPath = context.getFilesDir().getPath()+"/";
        android.util.Log.d(this.getClass().getSimpleName(),"dbpath: " + dbPath);
        musicQueue.add("second");

        this.parentActivity = parentActivity;
        this.gps = gps;
//        new WeatherResponse(latitude, longitude).execute();
//        new TrafficResponse(latitude, longitude).execute();
    }

    public ConcurrentLinkedQueue<String> getQueue() {
        return musicQueue;
    }

    /**
     * This Method is used to exclusively create Database for Movies.
     * @throws IOException  : IO Exception type.
     */
    public void createDB() throws IOException {
        this.getReadableDatabase();
        try {
            copyDB();
        } catch (IOException e) {
            android.util.Log.w(this.getClass().getSimpleName(),
                    "createDB Error copying database " + e.getMessage());
        }
    }

    /**
     * This method is used to copy the Database to the Device's physical File System
     * @throws IOException : IO Exception
     */
    public void copyDB() throws IOException{
        try {
            if(!checkDB()){
                // only copy the database if it doesn't already exist in my database directory
                debug("ModelsDB --> copyDB", "checkDB returned false, starting copy");
                InputStream inputStream =  dbContext.getResources().openRawResource(R.raw.app_context);

                // make sure the database path exists. if not, create it.
                File file = new File(dbPath);

                if(!file.exists()){
                    file.mkdirs();
                }

                String outputPath=  dbPath  +  dbName +".db";
                OutputStream output = new FileOutputStream(outputPath);
                byte[] buffer = new byte[1024];
                int length;

                while ((length = inputStream.read(buffer))>0){
                    output.write(buffer, 0, length);
                }
                output.flush();
                output.close();
                inputStream.close();
            }
        } catch (IOException e) {
            android.util.Log.w("ModelsDB --> copyDB", "IOException: "+e.getMessage());
        }
    }

    /**
     * This Method is used to Open Database for Read / Write Operations.
     * @return : returns the handle of the opened database.
     * @throws SQLException : SQL Exception if occured.
     */
    public SQLiteDatabase openDB() throws SQLException {
        String myPath = dbPath + dbName + ".db";
        if(checkDB()) {
            modelDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
            debug("ModelsDB --> openDB", "opened db at path: " + modelDB.getPath());
        }else{
            try {
                this.copyDB();
                modelDB = SQLiteDatabase.openDatabase(myPath, null, SQLiteDatabase.OPEN_READWRITE);
            }catch(Exception ex) {
                android.util.Log.w(this.getClass().getSimpleName(),"unable to copy and open db: "+ex.getMessage());
            }
        }
        return modelDB;
    }

    /**
     * This Method is used to check whether the Movies Database is in right health otherwise is
     * replaced with a copy present in the bundle.
     * @return : Health of Database.
     */
    private boolean checkDB(){    //does the database exist and is it initialized?
        boolean ret = false;
        try{
            String path = dbPath + dbName + ".db";
            debug("modelsdb --> checkModelDB: path to db is", path);
            File aFile = new File(path);
            if(aFile.exists()) {
                ret = true;
            }
        } catch(Exception e) {
            android.util.Log.w("ModelsDB->checkModelDB",e.getMessage());
        }
        return ret;
    }

    public void addSong(double latitude, double longitude) {
        emotionalResponse();
        new SpeedometerResponse(this.parentActivity, this.gps, this.parentActivity.getApplicationContext()).execute();
        new WeatherResponse(latitude, longitude).execute();
        new TrafficResponse(latitude, longitude).execute();
    }

    public void queryDatabaseController(String queryType, String params) {
//        ArrayList<String> songList = new ArrayList<String>();
        String query;
        Log.w("Params" , params);
        switch(queryType){
            case "weather":
                query = "select m.title from music m, context c where m.itemid = c.itemid and c.weather = ? order by RANDOM() LIMIT 1;";
                break;
            case "traffic":
                query = "select m.title from music m, context c where m.itemid = c.itemid and c.trafficconditions = ? order by RANDOM() LIMIT 1;";
                break;
            case "emotion":
                query = "select m.title from music m, context c where m.itemid = c.itemid and c.mood= ? order by RANDOM() LIMIT 1;";
                break;
            case "driving":
                query = "select m.title from music m, context c where m.itemid = c.itemid and c.drivingstyle = ? order by RANDOM() LIMIT 1;";
                break;
            default:
                query = "";
                break;

        }
        Log.w("Query:", query);
        try {
            SQLiteDatabase modelDb = this.openDB();
            Cursor cursor = modelDb.rawQuery(query,new String[]{ params});
            while(cursor.moveToNext()) {
                String songName = cursor.getString(0);
                choiceList.add(songName);


                // Logic to play the music player.
                // MPI for ArrayList

            }
            this.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

    }

    // Default Override Methods
    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    @Override
    public synchronized void close() {
        if(modelDB != null) {
            modelDB.close();
        }
        super.close();
    }

    private void debug(String hdr, String msg){
        if(debugon){
            android.util.Log.d(hdr,msg);
        }
    }

    private class WeatherResponse extends AsyncTask<String, String, String> {

        public double latitude;
        public double longitude;
        public Context context;

        WeatherResponse( double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
//            this.context = context;
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
            WeatherInfo weatherInfo = new WeatherInfo(strJsonObj);
            String weatherQuery = "weather";
            String weatherParam = weatherInfo.weather;
            queryDatabaseController(weatherQuery, weatherParam);
        }

    }

    private class TrafficResponse extends AsyncTask<String, String, String> {

        public double latitude;
        public double longitude;
        public Context context;
        private double lat1;
        private double lon1;
        private double lat2;
        private double lon2;
        private double latlen = 111111;

        TrafficResponse(double latitude, double longitude) {
            this.latitude = latitude;
            this.longitude = longitude;
//            this.context = context;
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
            super.onPostExecute(strJsonObj);
            TrafficInfo trafficInfo = new TrafficInfo(strJsonObj);
            String trafficQuery = "traffic";
            String trafficParam = trafficInfo.traffic;
            queryDatabaseController(trafficQuery, trafficParam);
            DatabaseController.this.databaseHandler.post(DatabaseController.this.addMusicDataToQueue);
        }
    }

    private class SpeedometerResponse extends AsyncTask<String, String, String> implements IBaseGpsListener{

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
        public void onPostExecute(String speed) {
            super.onPostExecute(speed);
            SpeedInfo speedInfo = new SpeedInfo(speed);
            String drivingStyleQueryType = "driving";
            String drivingStyleParams = speedInfo.getDrivingStyle();
            queryDatabaseController(drivingStyleQueryType, drivingStyleParams);
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

    public void addEmotion(String emotion)  {
        this.emotion = emotion;
    }

    public void emotionalResponse() {
        String emotionQuery = "emotion";
        queryDatabaseController(emotionQuery, this.emotion);
    }


}
