package com.feer.windcast;

import android.annotation.TargetApi;
import android.os.Build;
import android.util.JsonReader;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 *
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class ObservationReader
{
    public static WeatherData ReadJsonStream(InputStream in) throws IOException
    {
        JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
        try{
            WeatherData wd = null;

            reader.beginObject();
            while(reader.hasNext())
            {
                String name = reader.nextName();
                if(name.equals("observations"))
                    wd = ReadObservations(reader);
                else
                    reader.skipValue();
            }
            reader.endObject();
            return wd;
        }finally
        {
            reader.close();
        }
    }

    public static WeatherData ReadObservations(JsonReader reader)  throws IOException
    {
        WeatherData weatherData = null;
        List observationData = null;

        reader.beginObject();
        while(reader.hasNext())
        {
            String name = reader.nextName();
            if(name.equals("header"))
            {
                weatherData = ReadHeaderInfo(reader);
            }
            else if(name.equals("data"))
            {
                observationData = ReadAllObservationData(reader);
            } else
            {
                reader.skipValue();
            }
        }

        reader.endObject();

        if(weatherData != null)
        {
            weatherData.ObservationData = observationData;
        }
        else
        {
            throw new IOException("No header found in JSON stream");
        }

        return weatherData;
    }

    private static List ReadAllObservationData(JsonReader reader) throws IOException
    {
        List obs = new ArrayList<ObservationReading>();
        reader.beginArray();
        while (reader.hasNext()) {
            obs.add(ReadObservation(reader));
        }
        reader.endArray();
        return obs;
    }

    private static ObservationReading ReadObservation(JsonReader reader) throws IOException
    {
        ObservationReading ob = new ObservationReading();
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();

            if(reader.peek().name().equals("NULL"))
            {
                reader.skipValue();
            }else{
                if (name.equals("local_date_time_full")) {
                    String dateString = reader.nextString();
                    SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmss",  Locale.US);
                    try
                    {
                        ob.LocalTime = df.parse(dateString);
                    } catch (ParseException e)
                    {
                        Log.e("WindCast", "unable to parse date string: " + dateString);
                    }

                } else if (name.equals("wind_spd_kmh") ) {
                    ob.WindSpeed_KMH = reader.nextInt();
                }else if (name.equals("wind_dir") ) {
                    ob.CardinalWindDirection = reader.nextString().toLowerCase(Locale.US);
                    ob.WindBearing = ConvertCardinalCharsToBearing(ob.CardinalWindDirection);
                }else if (name.equals("gust_kmh") ) {
                    ob.WindGustSpeed_KMH = reader.nextInt();
                }

                else if (name.equals("air_temp")) {
                    ob.AirTemp_DegCel = (float)reader.nextDouble();
                }else if (name.equals("apparent_t")) {
                    ob.ApparentTemp_DegCel = (float)reader.nextDouble();
                }

                else if (name.equals("swell_dir_worded")) {
                    ob.CardinalSwellDirection = reader.nextString().toLowerCase(Locale.US);
                    ob.SwellBearing = ConvertCardinalCharsToBearing(ob.CardinalSwellDirection);
                }else if (name.equals("swell_height") ) {
                    ob.SwellHeight_meters = reader.nextInt();
                }else if (name.equals("swell_period")) {
                    ob.SwellPeriod_seconds = reader.nextInt();
                }

                else {
                    reader.skipValue();
                }
            }
        }
        reader.endObject();
        return ob;
    }

    /* Convert a string of lowercase chars representing a cardinal direction
    *  into a bearing. 0 = North, 90 = East, 180 = South, 270 = West
    *  Handles empty String and "-" by returning null
     */
    private static Float ConvertCardinalCharsToBearing(String dirs)
    {
        Float bearing = null;
        final int len = dirs.length();
        if(len < 1 || dirs.equals("-"))
        {
            return null;
        }

        if(len == 1) {
            if(dirs.equals("n"))
                bearing = 0.0f;
            else if(dirs.equals("e"))
                bearing = 90.0f;
            else if(dirs.equals("s"))
                bearing = 180.0f;
            else if(dirs.equals("w"))
                bearing = 270.0f;
        }
        else
        if(len == 2) {
            if(dirs.equals("ne"))
                bearing = 45.0f;
            else if(dirs.equals("se"))
                bearing = 135.0f;
            else if(dirs.equals("sw"))
                bearing = 225.0f;
            else if(dirs.equals("nw"))
                bearing = 315.0f;
        } else
        if (len == 3) {
            if(dirs.equals("nne"))
                bearing = 22.5f;
            else if(dirs.equals("ene"))
                bearing = 64.5f;
            else if(dirs.equals("ese"))
                bearing = 112.5f;
            else if(dirs.equals("sse"))
                bearing = 157.5f;
            else if(dirs.equals("ssw"))
                bearing = 202.5f;
            else if(dirs.equals("wsw"))
                bearing = 247.5f;
            else if(dirs.equals("wnw"))
                bearing = 292.5f;
            else if(dirs.equals("nnw"))
                bearing = 337.5f;
        }
        if(bearing == null)
        {
            throw new IllegalArgumentException("Unknown cardinal direction: " + dirs);
        }
        return bearing;
    }


    private static WeatherData ReadHeaderInfo(JsonReader reader) throws IOException
    {
        WeatherData wd = new WeatherData();
        reader.beginArray();
        reader.beginObject();
        while (reader.hasNext()) {
            String name = reader.nextName();
            if (name.equals("name")) {
                wd.WeatherStationName = reader.nextString();
            } else if (name.equals("time_zone")) {
                wd.TimeZone = reader.nextString();
            } else if (name.equals("state")) {
                wd.State = reader.nextString();
            } else {
                reader.skipValue();
            }
        }
        reader.endObject();
        reader.endArray();

        return wd;
    }
}
