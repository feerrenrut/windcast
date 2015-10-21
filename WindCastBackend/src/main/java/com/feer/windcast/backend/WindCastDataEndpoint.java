/*
   For step-by-step instructions on connecting your Android application to this backend module,
   see "App Engine Java Endpoints Module" template documentation at
   https://github.com/GoogleCloudPlatform/gradle-appengine-templates/tree/master/HelloEndpoints
*/

package com.feer.windcast.backend;

import com.google.api.server.spi.config.Api;
import com.google.api.server.spi.config.ApiMethod;
import com.google.api.server.spi.config.ApiNamespace;

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
     * Get data latest reading
     */
    @ApiMethod(name = "getLatestObservation")
    public List<LatestReading> getLatestObservations() {
        List<LatestReading> l = ofy().load().type(LatestReading.class).list();
        return l;
    }

    /**
     * Get list of stations
     */
    @ApiMethod(name = "getStationList")
    public List<StationData> getStationList() {
        List<StationData> l = ofy().load().type(StationData.class).list();
        return l;
    }
}
