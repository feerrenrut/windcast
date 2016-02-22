package com.feer.windcast;

import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import com.feer.windcast.dataAccess.backend.CreateWindcastBackendApi;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;


public class SplashScreen extends Activity {

    private static final String PREF_ACCOUNT_NAME = "PREF_ACCOUNT_NAME";
    private final String TAG = "SplashScreen";
    SharedPreferences settings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        settings = getSharedPreferences("windcastAccount", 0);
        final String accountName = settings.getString(PREF_ACCOUNT_NAME, null);

        if (accountName != null) {
            ContinueSplashScreen(accountName);
        } else {
            // Not signed in, show login window or request an account.
            Log.i(TAG, "Choosing account.");
            chooseAccount();
        }
    }

    private void ContinueSplashScreen(final String accountName) {
        CreateWindcastBackendApi.sContext = this.getApplicationContext();
        CreateWindcastBackendApi.CreateCredential(this.getApplicationContext(), accountName );

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

    void startApp()
    {
        Log.i(TAG, "Starting app.");
        SplashScreen.this.startActivity(new Intent(SplashScreen.this, MainActivity.class));
        SplashScreen.this.finish();
    }

    static final int REQUEST_ACCOUNT_PICKER = 2;

    void chooseAccount() {
        GoogleAccountCredential credential = CreateWindcastBackendApi.CreateCredential(this, "");
        startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
    }

    // setSelectedAccountName definition
    private void setSelectedAccountName(String accountName) {
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(PREF_ACCOUNT_NAME, accountName);
        editor.commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case REQUEST_ACCOUNT_PICKER:
                if (data != null && data.getExtras() != null) {
                    String accountName =
                            data.getExtras().getString(
                                    AccountManager.KEY_ACCOUNT_NAME);
                    if (accountName != null) {

                        Log.i(TAG, "Got account name: " + accountName );
                        setSelectedAccountName(accountName);
                        SharedPreferences.Editor editor = settings.edit();
                        editor.putString(PREF_ACCOUNT_NAME, accountName);
                        editor.commit();
                        // User is authorized.
                        ContinueSplashScreen(accountName);
                    }
                }
                break;
        }
    }
}
