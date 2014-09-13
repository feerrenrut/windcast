package com.feer.windcast;


import java.util.ArrayList;
import java.util.Collections;

public class EmptyDataError {
    public enum EmptyTextState
    {

        NoReason,
        NoResultsAfterFilter,
        NoFavourites,
        LoadingData,
        NoStationsAvailable,
        NoInternetAccess,

    }

    public interface OnEmptyListReasonChanged
    {
        public void onEmptyListReasonChanged(EmptyTextState newReason);
    }

    private ArrayList<EmptyTextState> mTextStateList = new ArrayList<EmptyTextState>();
    final private OnEmptyListReasonChanged mEmptyListReasonChanged;

    public EmptyDataError(OnEmptyListReasonChanged emptyListReasonChanged)
    {
        mEmptyListReasonChanged = emptyListReasonChanged;
        mTextStateList.add(EmptyTextState.NoReason);
    }

    public void AddEmptyListReason(EmptyTextState reason)
    {
        final EmptyTextState oldReason = GetReasonForEmptyText();
        if(!mTextStateList.contains(reason))
        {
            mTextStateList.add(reason);
        }
        Collections.sort(mTextStateList);
        final EmptyTextState newReason = GetReasonForEmptyText();
        if(oldReason != newReason)
        {
            mEmptyListReasonChanged.onEmptyListReasonChanged(newReason);
        }
    }

    public void RemoveEmptyListReason(EmptyTextState reason)
    {
        final EmptyTextState oldReason = GetReasonForEmptyText();
        if(mTextStateList.contains(reason))
        {
            mTextStateList.remove(reason);
        }
        Collections.sort(mTextStateList);
        final EmptyTextState newReason = GetReasonForEmptyText();
        if(oldReason != newReason)
        {
            mEmptyListReasonChanged.onEmptyListReasonChanged(newReason);
        }
    }

    private EmptyTextState GetReasonForEmptyText()
    {
        int lastIndex =  mTextStateList.size() -1;
        return mTextStateList.get(lastIndex);
    }

}
