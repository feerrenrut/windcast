package com.feer.windcast;

import java.net.URL;
import java.util.List;

/**
 * Describes weather data for a particular location
 */
public class WeatherData
{

    String ID;

    URL url;
    /* The name of the observation station
     */
    String WeatherStationName;

    /* The observation data for this location */
    List ObservationData;
}
