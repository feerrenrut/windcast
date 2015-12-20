package com.feer.windcast.backend;

import com.googlecode.objectify.annotation.Entity;
import com.googlecode.objectify.annotation.Id;

import java.util.List;

/**
 * Created by Reef on 24/10/2015.
 */
@Entity
public class StationsInUpdate {

    @Id
    private String State;

    private List<StationData> Stations;

    public String getState() {
        return State;
    }

    public void setState(String state) {
        State = state;
    }

    public List<StationData> getStations() {
        return Stations;
    }

    public void setStations(List<StationData> stations) {
        Stations = stations;
    }
}
