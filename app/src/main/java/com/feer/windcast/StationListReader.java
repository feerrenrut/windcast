package com.feer.windcast;

import android.annotation.TargetApi;
import android.os.Build;
import android.util.JsonReader;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;

/**
 * Reads weather stations from a strictly ordered config file
 * The file should have the following contents / order
 * {
 *     state : string,
 *     weatherStations:
 *     [
 *          {name:string, url:string}
 *     ]
 * }
 *
 * Any order of info will cause an exception to be thrown
 */
@TargetApi(Build.VERSION_CODES.HONEYCOMB)
public class StationListReader
{

    private static String GetStringOrThrow(String stringParamName, JsonReader reader) throws IllegalArgumentException, IOException
    {
        try
        {
            NextParamNameIsOrThrow(stringParamName, reader);
        }
        catch (IllegalArgumentException e)
        {
            throw new IllegalArgumentException("Could not get String Parameter.", e);
        }
        return reader.nextString();
    }

    private static void NextParamNameIsOrThrow(String paramName, JsonReader reader) throws IOException
    {
        String name = reader.nextName();
        if(!name.equals(paramName))
        {
            throw new IllegalArgumentException(
                    new StringBuilder()
                            .append("The data or ordering of the json file was not as expected. ")
                            .append("Unable to get parameter: ")
                            .append(paramName).toString()
            );
        }
    }
    public static ArrayList<WeatherStation> GetWeatherStationList(BufferedInputStream bis)
    {
        ArrayList<WeatherStation> stations = new ArrayList<WeatherStation>();

        try
        {
            JsonReader reader = new JsonReader(new InputStreamReader(bis, "UTF-8"));
            reader.beginObject();

            String state = GetStringOrThrow("state", reader);

            NextParamNameIsOrThrow("weatherStations", reader);
            {
                ReadWeatherStationsArray(stations, state,  reader);
            }
            reader.endObject();


        } catch (IOException e)
        {
            Log.e("DATA", e.getMessage());
        }
        return stations;
    }

    private static void ReadWeatherStationsArray(ArrayList<WeatherStation> stations, String state, JsonReader reader) throws IOException
    {
        reader.beginArray();
        while(reader.hasNext())
        {
            WeatherStation station = getWeatherStation(reader);
            if(station.Name != null && station.url != null)
            {
                station.State = state;
                stations.add(station);
            }
        }
        reader.endArray();
    }

    private static WeatherStation getWeatherStation(JsonReader reader) throws IOException
    {
        WeatherStation station = new WeatherStation();
        reader.beginObject();
        while(reader.hasNext())
        {
            station.Name = GetStringOrThrow("name", reader);

            String urlString = GetStringOrThrow("url", reader);
            if (urlString != null)
            {
                urlString = ConvertToJSONURL(urlString);
                station.url = new URL(urlString);
            }
        }
        reader.endObject();
        return station;
    }

    public static String ConvertToJSONURL(String urlString)
    {
        urlString = urlString.replaceAll("shtml", "json");
        urlString = urlString.replaceAll("products", "fwo");
        return urlString;
    }
}
