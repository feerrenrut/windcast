package com.feer.windcast;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 *
 */
public class WeatherStationArrayAdapter extends ArrayAdapter<WeatherStation>
{
    public static interface OnFavouriteChangedListener
    {
        public void OnFavouriteChanged(WeatherStation station);
    }

    private final Context mContext;
    private ArrayList<WeatherStation> mStations;
    private final int mLayoutResourceID;
    private final OnFavouriteChangedListener mFavChangedListener;

    public WeatherStationArrayAdapter(Context context, int layoutResourceID, ArrayList<WeatherStation> objects, OnFavouriteChangedListener favChangedListner )
    {
        super(context, layoutResourceID, objects);

        mContext = context;
        mStations = objects;
        mLayoutResourceID = layoutResourceID;
        mFavChangedListener = favChangedListner;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        LayoutInflater inflater = (LayoutInflater) mContext.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = inflater.inflate(mLayoutResourceID, parent, false);
        ImageView imageView = (ImageView) rowView.findViewById(R.id.image);
        TextView textView = (TextView) rowView.findViewById(R.id.text);

        WeatherStation station = mStations.get(position);

        if(station.IsFavourite)
        {
            imageView.setImageResource(R.drawable.star_solid);
        }

        imageView.setOnClickListener(new OnStarClicked(station, mFavChangedListener));

        textView.setText(station.toString());
        return rowView;
    }


    private class OnStarClicked implements View.OnClickListener
    {
        private final WeatherStation mStation;
        private final OnFavouriteChangedListener mFavChangedListener;

        OnStarClicked(WeatherStation station, OnFavouriteChangedListener favChangedListener)
        {
            mStation = station;
            mFavChangedListener = favChangedListener;
        }

        @Override
        public void onClick(View v)
        {
            ImageView imageView = (ImageView) v;
            mStation.IsFavourite = !mStation.IsFavourite;

            if(mStation.IsFavourite)
            {
                imageView.setImageResource(R.drawable.star_solid);
            }
            else
            {
                imageView.setImageResource(R.drawable.star_outline);
            }
            mFavChangedListener.OnFavouriteChanged(mStation);
        }
    }
}
