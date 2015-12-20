package com.feer.windcast;

import java.util.Date;
/**
 * Holds data for a weather observation at a particular time
 */
public class ObservationReading implements IObservationReading {
    /* The time of the observation, in local time
     */
    private Date LocalTime;

    private WindObservation Wind_Observation;

    @Override
    public Date getLocalTime() {
        return LocalTime;
    }

    public void setLocalTime(Date localTime) {
        LocalTime = localTime;
    }

    @Override
    public IWindObservation getWind_Observation() {
        return Wind_Observation;
    }

    public void setWind_Observation(WindObservation wind_Observation) {
        Wind_Observation = wind_Observation;
    }
}
