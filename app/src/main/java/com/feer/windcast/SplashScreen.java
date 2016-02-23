package com.feer.windcast;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

public class SplashScreen extends Activity {
    private final String TAG = "SplashScreen";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        startWeatherStationUpdateAndSleep();
    }

    private void startWeatherStationUpdateAndSleep() {
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
                startApp();
            }
        }.execute();
    }

    void startApp() {
        Log.i(TAG, "Starting app.");
        SplashScreen.this.startActivity(new Intent(SplashScreen.this, MainActivity.class));
        SplashScreen.this.finish();
    }
}
