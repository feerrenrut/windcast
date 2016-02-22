/*
   For step-by-step instructions on connecting your Android application to this backend module,
   see "App Engine Java Endpoints Module" template documentation at
   https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/HelloEndpoints
*/

package com.feer.windcast.backend;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.appengine.api.oauth.OAuthRequestException;
import com.google.appengine.api.users.User;

import java.util.List;
import java.util.logging.Logger;

import static com.googlecode.objectify.ObjectifyService.ofy;


class Constants {
    // just so that calls from the api explorer can be authorised
    public static final String API_EXPLORER_CLIENT_ID = com.google.api.server.spi.Constant.API_EXPLORER_CLIENT_ID;


    public static final String WEB_CLIENT_ID = "537656923472-jgkvfai9gda1piiatt3sgmuco4jpv7hb.apps.googleusercontent.com";
    public static final String ANDROID_CLIENT_ID_RELEASE = "537656923472-vf7c5msr9mq3ca9074r1lbp0hh5dtgnf.apps.googleusercontent.com";
    public static final String ANDROID_CLIENT_ID_DEBUG = "537656923472-nijbkrljrd8dql6lu3ellkgplbe45pej.apps.googleusercontent.com";
    public static final String ANDROID_AUDIENCE = WEB_CLIENT_ID;

    public static final String EMAIL_SCOPE = "https://www.googleapis.com/auth/userinfo.email";
}

/**
 * An endpoint class we are exposing
 */
@Api(
        name = "windcastdata",
        version = "v1",
        scopes = {Constants.EMAIL_SCOPE},
        clientIds = {
                Constants.WEB_CLIENT_ID,
                Constants.ANDROID_CLIENT_ID_DEBUG,
                Constants.ANDROID_CLIENT_ID_RELEASE,
                Constants.API_EXPLORER_CLIENT_ID
        },
        audiences = {Constants.ANDROID_AUDIENCE},
        namespace = @ApiNamespace(
                ownerDomain = "backend.windcast.feer.com",
                ownerName = "backend.windcast.feer.com",
                packagePath = ""
        )
)
public class WindCastDataEndpoint {

    private static final Logger log = Logger.getLogger(WindCastDataEndpoint.class.getName());

    /**
     * Get list of stations
     */
    @ApiMethod(name = "getStationList")
    public List<StationsInUpdate> getStationList(User user) throws OAuthRequestException {
        if(user == null){
            log.warning("Did not get user");
            return null;
        }

        log.info("getStationList request from: " + user.getEmail());
        List<StationsInUpdate> l = ofy().load().type(StationsInUpdate.class).list();
        return l;
    }
}
