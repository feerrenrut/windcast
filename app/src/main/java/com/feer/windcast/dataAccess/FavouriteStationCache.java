package com.feer.windcast.dataAccess;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.feer.windcast.WeatherStation;

import java.net.MalformedURLException;
import java.util.ArrayList;

import static com.feer.windcast.dataAccess.DBContract.FavouriteStation.COLUMN_NAME_STATION_NAME;
import static com.feer.windcast.dataAccess.DBContract.FavouriteStation.COLUMN_NAME_URL;

/**
 * A class to interact with the Favourite Station table
 */
public class FavouriteStationCache
{
    private Context mContext = null;
    private BackgroundTaskManager mTaskManager = null;
    private ArrayList<WeatherStation> mFavs = null; // only populated once GetFavouritesFromDB is called

    /**
     * Initialises the FavouriteStationCache so calls to all other functions
     * can be made synchronously.
     * This must not be called from the UI thread!
     */
    public void Initialise(Context context, BackgroundTaskManager taskManager)
    {
        mContext = context;
        mTaskManager = taskManager;
        mFavs = GetFavouritesFromDB();
    }

    public void AddFavouriteStation(final WeatherStation station)
    {
        mTaskManager.RunInBackground(
                new BackgroundTask<Void>() {
                    @Override
                    public Void DoInBackground() {
                        AddFavouriteStationToDB(station);
                        return null;
                    }
                }
        );

        if(mFavs != null && !mFavs.contains(station))
        {
            mFavs.add(station);
        }
    }

    public void RemoveFavouriteStation(final WeatherStation station)
    {
        mTaskManager.RunInBackground(
                new BackgroundTask<Void>() {
                    @Override
                    public Void DoInBackground() {
                        RemoveFavouriteStationFromDB(station);
                        return null;
                    }
                }
        );

        if(mFavs != null && mFavs.contains(station))
        {
            mFavs.remove(station);
        }
    }

    public ArrayList<WeatherStation> GetFavourites()
    {
        if(mFavs == null)
        {
            throw new IllegalStateException("GetFavourites() called before Initialise() was called or complete!!");
        }
        return mFavs;
    }

    private synchronized void AddFavouriteStationToDB(WeatherStation station)
    {
        station.IsFavourite = true;

        SQLiteDatabase db = DBOpenHelper.Instance(mContext).getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_STATION_NAME, station.Name); //todo i dont think that name is unique, it may need to be combined with state?
        values.put(COLUMN_NAME_URL, station.url.toString());
        db.insert(
                DBContract.FavouriteStation.TABLE_NAME,
                "null", values
                 );
    }

    private synchronized void RemoveFavouriteStationFromDB(WeatherStation station)
    {
        station.IsFavourite = false;

        SQLiteDatabase db = DBOpenHelper.Instance(mContext).getWritableDatabase();
        String selection = COLUMN_NAME_STATION_NAME + " LIKE ?";
        String[] selectionArgs = { station.Name };
        db.delete(DBContract.FavouriteStation.TABLE_NAME, selection, selectionArgs);
    }

    private synchronized ArrayList<WeatherStation> GetFavouritesFromDB()
    {
        SQLiteDatabase db = DBOpenHelper.Instance(mContext).getReadableDatabase();
        Cursor c = db.query(
                DBContract.FavouriteStation.TABLE_NAME,
                new String[]{COLUMN_NAME_STATION_NAME, COLUMN_NAME_URL},
                null, null, null, null, null
                           );
        ArrayList<WeatherStation> favs = new ArrayList<WeatherStation>();

        while(c.moveToNext())
        {
            try
            {
                WeatherStation station = new WeatherStation(
                        c.getString(c.getColumnIndexOrThrow(COLUMN_NAME_STATION_NAME)),
                        c.getString(c.getColumnIndexOrThrow(COLUMN_NAME_URL)));
                station.IsFavourite = true;
                favs.add(station);
            } catch (MalformedURLException e)
            {
                Log.e("WindCast",
                        "MalformedURLException while getting favourites from DB. For station: "
                                + c.getString(c.getColumnIndexOrThrow(COLUMN_NAME_URL)));
            }
        }
        return favs;
    }
}
