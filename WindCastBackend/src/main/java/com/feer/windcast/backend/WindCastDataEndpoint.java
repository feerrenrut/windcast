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

/**
 * An endpoint class we are exposing
 */
@Api(
        name = "windcastdata",
        version = "v1",
        clientIds = {"123456923472-vf7c5msr9mq3ca9074r1lbp0hh5dtgnf.apps.googleusercontent.com"},//{"537656923472-vf7c5msr9mq3ca9074r1lbp0hh5dtgnf.apps.googleusercontent.com"},
        scopes = "https://www.googleapis.com/auth/userinfo.email",
        audiences = "123456923472-vf7c5msr9mq3ca9074r1lbp0hh5dtgnf.apps.googleusercontent.com", //"537656923472-vf7c5msr9mq3ca9074r1lbp0hh5dtgnf.apps.googleusercontent.com",
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
        //if(user == null) throw new OAuthRequestException("Unauthorised request.");
        List<StationsInUpdate> l = ofy().load().type(StationsInUpdate.class).list();
        return l;
    }
}
