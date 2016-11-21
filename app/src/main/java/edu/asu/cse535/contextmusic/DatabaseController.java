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
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.SQLException;

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

    // Constructors
    public DatabaseController(Context context) {
        super(context, dbName, null, DATABASE_VERSION);
        dbContext = context;
        dbPath = context.getFilesDir().getPath()+"/";
        android.util.Log.d(this.getClass().getSimpleName(),"dbpath: " + dbPath);
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
}
