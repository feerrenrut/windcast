/*
   For step-by-step instructions on connecting your Android application to this backend module,
   see "App Engine Java Endpoints Module" template documentation at
   https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/HelloEndpoints
*/

package com.feer.windcast.backend;

import com.feer.helperLib.TokenGen;
import com.google.api.server.spi.ServiceException;
import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;
import com.google.api.server.spi.config.Named;

import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
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

    public class AuthorisationException extends ServiceException {
        AuthorisationException(){
            super(401, "The token passed could not be reconstructed with the arguments given.");
        }
    }

    public class TokenExpiredException  extends ServiceException {
        TokenExpiredException()  {
            super(400, "The token passed was not created recently enough. " +
                    "Please recreate the token (and check that your time source is accurate)");
        }
    }

    /**
     * Get list of stations
     */
    @ApiMethod(name = "getStationList")
    public List<StationsInUpdate> getStationList(
            @Named("userIdStr") String userIdStr,
            @Named("time") Long time,
            @Named("token") String token ) throws AuthorisationException, TokenExpiredException {
        final String methodName = "getStationList";

        log.info("getStationList: " + " Token: " + token + "User: " + userIdStr);

        if(! TokenGen.TestToken( methodName, userIdStr, time, token ) ) {
            throw new AuthorisationException();
        }

        if( Math.abs(new Date().getTime() - time) > TimeUnit.HOURS.toMillis(1))
        {
            throw new TokenExpiredException();
        }

        List<StationsInUpdate> l = ofy().load().type(StationsInUpdate.class).list();
        return l;
    }
}
