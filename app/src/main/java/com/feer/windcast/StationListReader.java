package com.feer.windcast;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Reads weather stations embedded as links in html
 * */
public class StationListReader
{
    static final String TAG = "StationListReader";
    public static ArrayList<WeatherStation> GetWeatherStationsFromURL(URL fromUrl, String state) throws Exception
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

        /* because we have to escape the slashes and quotes in the regex this is hard to read.

           to break this down:
           we are trying to match a line like one of the following (in html)
           <th id="tKIM-station-broome-port" class="rowleftcolumn"><a href="/products/IDW60801/IDW60801.95202.shtml">Broome Port</a></th>
           <th id="obs-station-braidwood" class="rowleftcolumn"><a href="/products/IDN60903/IDN60903.94927.shtml">Braidwood</a></th>

           we want two groups one for the url (group 1) and one for the full station name (group 2)
           it seems all stations have an id that includes the word 'station'
           Group 1: ([\/\w.]*shtml)
           this is the relative link to the station page. It contains words, forward slashes('/') and
           period characters. It ends with 'shtml'

           Group 2: >(.*)</a>
           is all characters inside the anchor tag
        */
        Pattern p = Pattern.compile("<th id=\\\".*-station-.*\" \\w*=\\\"\\w*\\\"><a \\w*=\\\"([\\/\\w.]*shtml)\\\">(.*)</a>.*");
        String str;

        while((str = buf.readLine()) != null)
        {
            Matcher m = p.matcher(str);
            while(m.find())
            {
                WeatherStation ws = new WeatherStation();
                ws.State = state;
                ws.url = new URL("http://www.bom.gov.au" + StationListReader.ConvertToJSONURL(m.group(1)));
                ws.Name = m.group(2);
                weatherStations.add(ws);
            }
        }

        buf.close();
        return weatherStations;
    }

    public static String ConvertToJSONURL(String urlString)
    {
        urlString = urlString.replaceAll("shtml", "json");
        urlString = urlString.replaceAll("products", "fwo");
        return urlString;
    }
}
