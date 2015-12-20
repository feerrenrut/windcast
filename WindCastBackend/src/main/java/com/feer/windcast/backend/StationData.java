package com.feer.windcast.backend;

/**
 * The object model for the data we are sending through endpoints
 */

public class StationData {

    public enum States
    {
        WA,
        SA,
        ACT,
        QLD,
        NT,
        TAS,
        NSW,
        VIC
    }

    private String stationID;

    // Acronym of the state
    private States state;

    private String dataUrl;

    private String displayName;

    private LatestReading latestReading;

    public States getState() {
        return state;
    }

    public void setState(States state) {
        this.state = state;
    }

    public String getStationID() {
        return stationID;
    }

    public void setStationID(String stationID) {
        this.stationID = stationID;
    }

    public String getDataUrl() {
        return dataUrl;
    }

    public void setDataUrl(String dataUrl) {
        this.dataUrl = dataUrl;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public LatestReading getLatestReading() {
        return latestReading;
    }

    public void setLatestReading(LatestReading latestReading) {
        this.latestReading = latestReading;
    }
}

