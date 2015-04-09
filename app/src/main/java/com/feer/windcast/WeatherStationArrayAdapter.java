package com.feer.windcast;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
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
    private final int mLayoutResourceID;
    private final OnFavouriteChangedListener mFavChangedListener;

    public WeatherStationArrayAdapter(Context context, int layoutResourceID, ArrayList<WeatherStation> objects, OnFavouriteChangedListener favChangedListner)
    {
        super(context, layoutResourceID, objects);

        mContext = context;
        mLayoutResourceID = layoutResourceID;
        mFavChangedListener = favChangedListner; 
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        LayoutInflater inflater = (LayoutInflater) mContext.
                getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if(convertView == null)
        {
            convertView = inflater.inflate(mLayoutResourceID, parent, false);
        }

        WeatherStation station = super.getItem(position);
                
        CheckBox checkbox = (CheckBox) convertView.findViewById(R.id.is_favourite_checkbox);
        checkbox.setChecked(station.IsFavourite);
        checkbox.setOnCheckedChangeListener(new OnStarClicked(station, mFavChangedListener));

        TextView textView = (TextView) convertView.findViewById(R.id.station_name);
        textView.setText(station.toString());
        
        return convertView;
    }


    private class OnStarClicked implements CheckBox.OnCheckedChangeListener
    {
        private final WeatherStation mStation;
        private final OnFavouriteChangedListener mFavChangedListener;

        OnStarClicked(WeatherStation station, OnFavouriteChangedListener favChangedListener)
        {
            mStation = station;
            mFavChangedListener = favChangedListener;
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            mStation.IsFavourite = isChecked;
            mFavChangedListener.OnFavouriteChanged(mStation);
        }
    }
}
