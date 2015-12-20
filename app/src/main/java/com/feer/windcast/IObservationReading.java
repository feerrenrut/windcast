package com.feer.windcast;

import java.util.Date;

/**
 * Created by Reef on 23/10/2015.
 */
public interface IObservationReading {
    Date getLocalTime();

    IWindObservation getWind_Observation();
}
