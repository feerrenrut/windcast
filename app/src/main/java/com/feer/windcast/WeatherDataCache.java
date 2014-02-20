package com.feer.windcast;

import android.annotation.TargetApi;
import android.content.res.Resources;
import android.os.Build;
import android.util.JsonReader;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

/**
 *
 */
public class WeatherDataCache
{
    Resources m_res;
    WeatherDataCache(Resources res)
    {
        m_res = res;
    }

    public boolean ShouldUseStaticData = false;

    public WeatherData GetWeatherDataFor(URL url)
    {
        WeatherData wd = null;
        try
        {
            BufferedInputStream bis;
            if(!ShouldUseStaticData)
            {
                URLConnection urlConnection = url.openConnection();
                InputStream is = urlConnection.getInputStream();
                bis = new BufferedInputStream(is);
            }
            else
            {
                InputStream is = m_res.openRawResource(R.raw.test_data);
                bis = new BufferedInputStream(is);
            }
            wd = ObservationReader.ReadJsonStream(bis);

        } catch (MalformedURLException e)
        {
            Log.e("DATA", e.getMessage());
        } catch (IOException e)
        {
            Log.e("DATA", e.getMessage());
        }
        return wd;
    }

    private static ArrayList<WeatherStation> sm_stations = new ArrayList<WeatherStation>();
    private static boolean sm_initialised = false;

    public ArrayList<WeatherStation> GetWeatherStations()
    {
        if(!sm_initialised)
        {
            sm_stations = readWeatherStations();
            sm_initialised = true;
        }

        return sm_stations;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private ArrayList<WeatherStation> readWeatherStations()
    {
        InputStream is = m_res.openRawResource(R.raw.all_wa_stations);
        BufferedInputStream bis = new BufferedInputStream(is);

        ArrayList<WeatherStation> stations = new ArrayList<WeatherStation>();

        try
        {
            JsonReader reader = new JsonReader(new InputStreamReader(bis, "UTF-8"));
            reader.beginObject();

            String name = reader.nextName();
            if(name.equals("weatherStations"))
            {
                reader.beginArray();
                while(reader.hasNext())
                {
                    WeatherStation station = new WeatherStation();
                    reader.beginObject();
                    while(reader.hasNext())
                    {
                        name = reader.nextName();
                        if(name.equals("name"))
                        {
                            station.Name = reader.nextString();
                        }
                        else if(name.equals("url"))
                        {
                            String urlString = reader.nextString();
                            if (urlString != null)
                            {
                                urlString = urlString.replaceAll("shtml", "json");
                                urlString = urlString.replaceAll("products", "fwo");
                                station.url = new URL(urlString);
                            }
                        }
                        else
                        {
                            reader.skipValue();
                        }
                    }
                    reader.endObject();
                    if(station.Name != null && station.url != null)
                    {
                        stations.add(station);
                    }
                }
                reader.endArray();
            }
            else
            {
                reader.skipValue();
            }
            reader.endObject();


        } catch (IOException e)
        {
            Log.e("DATA", e.getMessage());
        }
        return stations;
    }
}
