package com.feer.windcast.backend.cron;

import com.feer.windcast.backend.LastUpdateResult;
import com.feer.windcast.backend.StationListReader;

import org.joda.time.DateTime;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static com.googlecode.objectify.ObjectifyService.ofy;

/**
 * Created by Reef on 18/10/2015.
 * see http://rominirani.com/2009/11/16/episode-9-using-the-cron-service-to-run-scheduled-tasks/
 */
public class GAEJCronServletUpdateStations extends HttpServlet {
    private static final Logger log = Logger.getLogger(GAEJCronServletUpdateStations.class.getName());
    private static final HashMap<String, String> StateURLMap = new HashMap<String, String>(){{
        put("WA", "http://www.bom.gov.au/wa/observations/waall.shtml");
        put("NSW", "http://www.bom.gov.au/nsw/observations/nswall.shtml");
        put("VIC", "http://www.bom.gov.au/vic/observations/vicall.shtml");
        put("QLD", "http://www.bom.gov.au/qld/observations/qldall.shtml");
        put("SA", "http://www.bom.gov.au/sa/observations/saall.shtml");
        put("TAS", "http://www.bom.gov.au/tas/observations/tasall.shtml");
        put("ACT", "http://www.bom.gov.au/act/observations/canberra.shtml");
        put("NT", "http://www.bom.gov.au/nt/observations/ntall.shtml");
    }};

    public void doGet(HttpServletRequest req, HttpServletResponse resp) {
        for (Map.Entry<String, String> entry : StateURLMap.entrySet() ) {
            updateCacheForState(entry.getValue(), entry.getKey());
        }
        resp.setStatus(HttpServletResponse.SC_OK);
    }

    public void updateCacheForState(String scrapeURL, String state) {
        LastUpdateResult res = new LastUpdateResult();
        res.setState(state);
        res.setScrapePage(scrapeURL);
        res.setLastUpdateWasSuccessful(false);
        try {
            URL url = new URL(scrapeURL);
            StationListReader.GetWeatherStationsFromURL(url, state);
            LastUpdateResult.setLastSuccessfulUpdate(res, DateTime.now());
            res.setLastUpdateWasSuccessful(true);
        } catch (MalformedURLException urlEx)
        {
            log.log(Level.WARNING, "Couldn't create URL ", urlEx);
        } catch (Exception e)
        {
            log.log(Level.WARNING, "Error getting station list: ", e);
        }
        ofy().save().entity(res);
    }
}
