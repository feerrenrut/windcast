package com.feer.windcast;

import android.content.res.Resources;
import android.os.AsyncTask;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    private static final String TAG = "WeatherDataCache";

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
                Log.w(TAG, "Using static test data");
                InputStream is = m_res.openRawResource(R.raw.test_observation_data_badgingarra);
                bis = new BufferedInputStream(is);
            }
            wd = ObservationReader.ReadJsonStream(bis);

        } catch (MalformedURLException e)
        {
            Log.e(TAG, e.getMessage());
        } catch (IOException e)
        {
            Log.e(TAG, e.getMessage());
        }
        return wd;
    }

    private static ArrayList<WeatherStation> sm_stations = new ArrayList<WeatherStation>();
    private static boolean sm_initialised = false;

    public ArrayList<WeatherStation> GetWeatherStations()
    {
        if(!sm_initialised)
        {
            InputStream is = m_res.openRawResource(R.raw.all_wa_stations);
            BufferedInputStream bis = new BufferedInputStream(is);

            sm_stations = StationListReader.GetWeatherStationList(bis);
            Collections.sort(sm_stations);

            new AsyncTask<Void, Void, Void>()
            {

                @Override
                protected Void doInBackground(Void... params)
                {
                    try
                    {
                        ArrayList<WeatherStation> fromHtml = null;
                        URL url = new URL("http://www.bom.gov.au/wa/observations/waall.shtml");
                        fromHtml = GetWeatherStationsFromURL(url);
                        Collections.sort(fromHtml);

                        AssertEqual(sm_stations, fromHtml);
                        Log.e(TAG, "Compare complete!");
                    } catch (Exception e)
                    {
                        Log.e(TAG, "Couldnt create URL "+e.toString());
                    }
                    return null;
                }
            }.execute();

            sm_initialised = true;
        }

        return sm_stations;
    }

    private ArrayList<WeatherStation> GetWeatherStationsFromURL(URL fromUrl) throws Exception
    {
        BufferedReader buf;
        try
        {
            buf = new BufferedReader(
                    new InputStreamReader(
                            fromUrl.openStream()));
        } catch (IOException e)
        {
            Log.e(TAG, "Unable to get content from url: " + fromUrl.toString());
            throw new Exception("Cant get content", e);
        }

        ArrayList<WeatherStation> weatherStations = new ArrayList<WeatherStation>();

        Pattern p = Pattern.compile("<th id=\\\"t.*-station-.*\" \\w*=\\\"\\w*\\\"><a \\w*=\\\"([\\/\\w.]*shtml)\\\">(.*)</a>.*");
        String str;

        while((str = buf.readLine()) != null)
        {
            Matcher m = p.matcher(str);
            while(m.find())
            {
                WeatherStation ws = new WeatherStation();
                ws.State = "WA";
                ws.url = new URL("http://www.bom.gov.au" + StationListReader.ConvertToJSONURL(m.group(1)));
                ws.Name = m.group(2);
                weatherStations.add(ws);
            }
        }

        buf.close();
        return weatherStations;
    }

    /*
    This allows us to check that the data returned from the html matches that of the data returned from the
    json file. The data does not actually match. There are a few differences:

    Size dont match: expected: 144 actual: 149

    Json has one HTML doesnt: Brookton (WA)
    Json has one HTML doesnt: Pemberton (WA)

    HTML has one JSON doesnt: Balgo Hills (WA)
    HTML has one JSON doesnt: Eneabba (WA)
    HTML has one JSON doesnt: Goomalling (WA)
    HTML has one JSON doesnt: Kellerberrin (WA)
    HTML has one JSON doesnt: Medina (WA)
    HTML has one JSON doesnt: Melville Water (WA)
    HTML has one JSON doesnt: Mount Barker (WA)

    The HTML pages only show weather stations that have recent readings.
    "Where no observation is available within the last 75 minutes, the latest observation is shown
    in italics and coloured and removed from the table after 24 hours. Station names link to data
    for the previous 72 hours." -> from BOM

    Its likely that stations missing from the HTML were stations that were active when the JSON file
    was generated but not now, and the stations missing from JSON are active now but were not when
    the json file was generated.
     */
    private void AssertEqual(ArrayList<WeatherStation> fromJSON, ArrayList<WeatherStation> fromHTML)
    {
        if(fromJSON.size() < fromHTML.size())
        {
            Log.e(TAG, "Size dont match: expected: " + fromJSON.size() + " actual: " + fromHTML.size());
        }

        for(WeatherStation expected : fromJSON)
        {
            if(!fromHTML.contains(expected))
            {
                Log.e(TAG, "Json has one HTML doesnt: " + expected.toString());
            }
        }

        for(WeatherStation actual : fromHTML)
        {
            if(!fromJSON.contains(actual))
            {
                Log.e(TAG, "HTML has one JSON doesnt: " + actual.toString());
            }
        }
    }
}
