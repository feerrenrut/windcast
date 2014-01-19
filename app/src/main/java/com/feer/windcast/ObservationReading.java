package com.feer.windcast;

import java.util.Date;

/**
 * Holds data for a weather observation at a particular time
 */
public class ObservationReading
{
    /* The time of the observation, in local time
     */
    Date LocalTime;

    /* The temperature of the air, in degrees celsius
     */
    float AirTemp_DegCel;

    /* The apparent temperature, in degrees celsius
     */
    float ApparentTemp_DegCel;

    /* The set of possible wind directions.
     */
    enum CardinalDirections
    {
        NORTH,
        SOUTH,
        EAST,
        WEST,
    }

    /* The direction the wind is coming from
     */
    CardinalDirections[] WindDirection;

    /* The speed of the wind in KiloMeters per Hour
     */
    float WindSpeed_KMH;

    /* The wind gust speed in KiloMeters per Hour
     */
    float WindGustSpeed_KMH;

    /* The direction of the ocean swell
     */
    CardinalDirections[] SwellDirection;

    /* The height in meters of the ocean swell
     */
    float SwellHeight_meters;

    /* The swell period in seconds
     */
    float SwellPeriod_seconds;
}
