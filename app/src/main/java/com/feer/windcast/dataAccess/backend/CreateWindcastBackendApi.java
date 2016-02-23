package com.feer.windcast.dataAccess.backend;

import com.feer.windcast.backend.windcastdata.Windcastdata;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.extensions.android.json.AndroidJsonFactory;
import com.google.api.client.googleapis.services.AbstractGoogleClientRequest;
import com.google.api.client.googleapis.services.GoogleClientRequestInitializer;

import java.io.IOException;

public class CreateWindcastBackendApi {

    private static final String TAG = "CreateAPI";

    private static final String ServerAddress_realServer =
            "https://windcast-backend.appspot.com/_ah/api/"; // USING REAL SERVER

    // options for running against local devappserver
    // - 10.0.2.2 is localhost's IP address in Android emulator
    // - 10.0.3.2 is localhost's IP address in Genymotion
    // - turn off compression when running against local devappserver

    private static final String ServerAddress_devServer_AndroidEmu =
            "http://10.0.2.2:8080/_ah/api/"; // 10.0.2.2 is localhost's IP address in Android emulator
    private static final String ServerAddress_devServer_Genymotion =
            "http://10.0.3.2:8080/_ah/api/"; //- 10.0.3.2 is localhost's IP address in Genymotion

    private static final String ServerAddress = ServerAddress_devServer_Genymotion;
    static public Windcastdata create() {
        Windcastdata.Builder builder  = new Windcastdata
                .Builder( AndroidHttp.newCompatibleTransport(), new AndroidJsonFactory(), /*credential*/ null)
                .setApplicationName("Windcast")
                .setRootUrl(ServerAddress)
                .setGoogleClientRequestInitializer(new GoogleClientRequestInitializer() {
                    @Override
                    public void initialize(AbstractGoogleClientRequest<?> abstractGoogleClientRequest) throws IOException {
                        // disable compression if using a dev server
                        abstractGoogleClientRequest.setDisableGZipContent(
                                !ServerAddress.equals(ServerAddress_realServer));
                    }
                });

        // end options for devappserver
        Windcastdata d = builder.build();
        return d;
    }
}