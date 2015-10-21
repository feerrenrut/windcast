package com.feer.windcast.backend;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;


/**
 * The object model for the data we are sending through endpoints
 */
@Entity
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

    @Id
    private String stationID;

    // Acronym of the state
    private States state;

    private String dataUrl;

    private String displayName;

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


}

