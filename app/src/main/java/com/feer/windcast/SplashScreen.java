package com.feer.windcast;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.feer.windcast.dataAccess.BackgroundTask;
import com.feer.windcast.dataAccess.BackgroundTaskManager;
import com.feer.windcast.dataAccess.LoadedWeatherCache;
import com.feer.windcast.dataAccess.WeatherDataCache;


public class SplashScreen extends Activity {

    private final String TAG = "SplashScreen";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        
        WeatherStationsService.startAction_UpdateWeatherStations(this);

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    Thread.sleep(1000l);
                } catch (InterruptedException e) {
                    Log.e(TAG, "Error while sleeping with splash screen: " + e.toString());
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                super.onPostExecute(aVoid);
                SplashScreen.this.startActivity(new Intent(SplashScreen.this, MainActivity.class));
                SplashScreen.this.finish();
            }
        }.execute();
    }
}
