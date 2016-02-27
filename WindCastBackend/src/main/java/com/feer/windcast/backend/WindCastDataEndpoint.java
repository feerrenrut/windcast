/*
   For step-by-step instructions on connecting your Android application to this backend module,
   see "App Engine Java Endpoints Module" template documentation at
   https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/HelloEndpoints
*/

package com.feer.windcast.backend;

import com.feer.helperLib.TokenGen;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.Named;
import com.google.appengine.api.oauth.OAuthRequestException;

import java.util.List;
import java.util.logging.Logger;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * An endpoint class we are exposing
 */
@Api(
        name = "windcastdata",
        version = "v1",
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
    public List<StationsInUpdate> getStationList(@Named("token") String token ) throws OAuthRequestException {
        log.info("Called getStationList" + " Token was: " + token);
        final String expectedToken = TokenGen.GetToken();
        log.info("token matches:" + expectedToken.equals(token));
        List<StationsInUpdate> l = ofy().load().type(StationsInUpdate.class).list();
        return l;
    }
}
