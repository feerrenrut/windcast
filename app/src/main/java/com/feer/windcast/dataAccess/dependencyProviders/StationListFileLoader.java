package com.feer.windcast.dataAccess.dependencyProviders;

import android.content.Context;
import android.util.Log;

import com.feer.windcast.backend.windcastdata.model.StationsInUpdateCollection;
import com.google.api.client.json.JsonParser;
import com.google.api.client.json.jackson2.JacksonFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Used to save the station list to and read from a file.
 */
public class StationListFileLoader {
    private static final String TAG = "StationListFileLoader";
    final String FILENAME = "STATIONS_IN_LAST_UPDATE";
    final Context mContext;
    public StationListFileLoader(Context context) {
        mContext = context;
    }

    public void writeToFile(StationsInUpdateCollection stations){
        FileOutputStream fileOutputStream;
        try {
            fileOutputStream = mContext.openFileOutput(FILENAME, Context.MODE_PRIVATE);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Unable to open file: " + e.toString());
            return;
        }
        try {
            fileOutputStream.write(stations.toString().getBytes("UTF-8"));
        } catch (IOException e) {
            Log.e(TAG, "Unable to write file: " + e.toString());
            return;
        }
    }

    public StationsInUpdateCollection readFile(){
        FileInputStream fileInputStream;
        try {
            fileInputStream = mContext.openFileInput(FILENAME);
        } catch (FileNotFoundException e) {
            Log.e(TAG, "Unable to open file: " + e.toString());
            return null;
        }
        JsonParser jsonParser;
        try {
            jsonParser = JacksonFactory.getDefaultInstance().createJsonParser(fileInputStream);
            return jsonParser.parse(StationsInUpdateCollection.class);
        } catch (IOException e) {
            Log.e(TAG, "Unable to write file: " + e.toString());
            return null;
        }
    }

}
