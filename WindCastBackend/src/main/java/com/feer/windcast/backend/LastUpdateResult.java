package com.feer.windcast.backend;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * Created by Reef on 18/10/2015.
 */
@Entity
public class LastUpdateResult {

    final private static DateTimeFormatter DATE_TIME_FORMATTER = ISODateTimeFormat.ordinalDateTime().withZoneUTC().withOffsetParsed();

    public static DateTime getLastSuccessfulUpdate(LastUpdateResult r) {
        if(r.lastSuccessfulUpdate != null) {
            return DATE_TIME_FORMATTER.parseDateTime(r.lastSuccessfulUpdate);
        }
        return null;
    }

    public static LastUpdateResult setLastSuccessfulUpdate(LastUpdateResult r, DateTime d){
        r.lastSuccessfulUpdate = d.toString(DATE_TIME_FORMATTER);
        return r;
    }

    @Id
    private String state; // State of Australia that the reading is for
    private String lastSuccessfulUpdate; // UTC string for when the the update occurred
    private Boolean lastUpdateWasSuccessful; // Was the lastUpdate attempt successful
    private String scrapePage; // URL for the page scraped.

    public String getLastSuccessfulUpdate() {
        return lastSuccessfulUpdate;
    }

    public void setLastSuccessfulUpdate(String lastSuccessfulUpdate) {
        this.lastSuccessfulUpdate = lastSuccessfulUpdate;
    }

    public Boolean getLastUpdateWasSuccessful() {
        return lastUpdateWasSuccessful;
    }

    public void setLastUpdateWasSuccessful(Boolean lastUpdateWasSuccessful) {
        this.lastUpdateWasSuccessful = lastUpdateWasSuccessful;
    }

    public String getScrapePage() {
        return scrapePage;
    }

    public void setScrapePage(String scrapePage) {
        this.scrapePage = scrapePage;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }
}
