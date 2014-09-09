package com.feer.windcast.dataAccess;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.feer.windcast.WeatherStation;

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
    private ArrayList<String> mFavUrls = null; // only populated once GetFavouritesFromDB is called

    /**
     * Initialises the FavouriteStationCache so calls to all other functions
     * can be made synchronously.
     * This must not be called from the UI thread!
     */
    public synchronized void Initialise(Context context, BackgroundTaskManager taskManager)
    {
        mContext = context;
        mTaskManager = taskManager;
        mFavUrls = GetFavouritesFromDB();
    }

    synchronized
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

        if(mFavUrls != null && !mFavUrls.contains(station.GetURL().toString()))
        {
            mFavUrls.add(station.GetURL().toString());
        }
    }

    synchronized
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

        if(mFavUrls != null && mFavUrls.contains(station.GetURL().toString()))
        {
            mFavUrls.remove(station.GetURL().toString());
        }
    }

    synchronized
    public ArrayList<String> GetFavouriteURLs()
    {
        if(mFavUrls == null)
        {
            throw new IllegalStateException("GetFavouriteURLs() called before Initialise() was called or complete!!");
        }
        return mFavUrls;
    }

    private synchronized void AddFavouriteStationToDB(WeatherStation station)
    {
        station.IsFavourite = true;

        SQLiteDatabase db = DBOpenHelper.Instance(mContext).getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_STATION_NAME, station.GetName());
        values.put(COLUMN_NAME_URL, station.GetURL().toString());
        db.insert(
                DBContract.FavouriteStation.TABLE_NAME,
                "null", values
                 );
    }

    private synchronized void RemoveFavouriteStationFromDB(WeatherStation station)
    {
        station.IsFavourite = false;

        SQLiteDatabase db = DBOpenHelper.Instance(mContext).getWritableDatabase();
        String selection = COLUMN_NAME_URL + " LIKE ?";
        String[] selectionArgs = { station.GetURL().toString() };
        db.delete(DBContract.FavouriteStation.TABLE_NAME, selection, selectionArgs);
    }

    private synchronized ArrayList<String> GetFavouritesFromDB()
    {
        SQLiteDatabase db = DBOpenHelper.Instance(mContext).getReadableDatabase();
        Cursor c = db.query(
                DBContract.FavouriteStation.TABLE_NAME,
                new String[]{COLUMN_NAME_URL},
                null, null, null, null, null
                           );
        ArrayList<String> favs = new ArrayList<String>();

        while(c.moveToNext())
        {
            favs.add(c.getString(c.getColumnIndexOrThrow(COLUMN_NAME_URL)));
        }
        return favs;
    }
}
