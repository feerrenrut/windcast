package com.feer.windcast.dataAccess.backend;

import android.content.Context;
import android.util.Log;

import com.feer.windcast.backend.windcastdata.Windcastdata;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;

import java.io.IOException;


/**
 * Created by Reef on 20/10/2015.
 */
public class CreateWindcastBackendApi {

    private class Constants{
        public static final String WEB_CLIENT_ID = "537656923472-jgkvfai9gda1piiatt3sgmuco4jpv7hb.apps.googleusercontent.com";
        public static final String AUDIENCE = "server:client_id:"+WEB_CLIENT_ID;
    }

    private static final String TAG = "Splashscreen";
    static final int REQUEST_ACCOUNT_PICKER = 2;

    static public GoogleAccountCredential CreateCredential(Context context, String userAccountName)
    {
        Log.i(TAG, "Got account name: '" + userAccountName + "'");
        sCredential = GoogleAccountCredential.usingAudience(context,
                Constants.AUDIENCE);
        sCredential.setSelectedAccountName(userAccountName);
        return sCredential;
    }

    public static Context sContext;
    private  static GoogleAccountCredential sCredential;

    static public Windcastdata create() {


        Windcastdata.Builder builder  = new Windcastdata
                .Builder( AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), sCredential)
                .setApplicationName("Windcast")
                // options for running against local devappserver
                // - 10.0.2.2 is localhost's IP address in Android emulator
                // - 10.0.3.2 is localhost's IP address in Genymotion
                // - turn off compression when running against local devappserver
                .setRootUrl("http://10.0.3.2:8080/_ah/api/") // USING DEV SERVER
                //.setRootUrl("https://windcast-backend.appspot.com/_ah/api/") // USING REAL SERVER
                .setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
                    @Override
                    public void initialize(AbstractGoogleClientRequest<?> abstractGoogleClientRequest) throws IOException {
                        //abstractGoogleClientRequest.setDisableGZipContent(true); // USING DEV SERVER
                        abstractGoogleClientRequest.setDisableGZipContent(false); // USING REAL SERVER
                    }
                });

        // end options for devappserver
        Windcastdata d = builder.build();
        return d;
    }

}