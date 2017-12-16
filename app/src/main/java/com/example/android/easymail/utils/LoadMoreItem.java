package com.example.android.easymail.utils;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by harshit on 10/12/17.
 */

public class LoadMoreItem extends SenderEmailListItem{

    public LoadMoreItem() {
    }

    @Override
    public int getType() {
        return TYPE_LOAD_MORE;
    }

    public static final Parcelable.Creator<LoadMoreItem> CREATOR
            = new Parcelable.Creator<LoadMoreItem>() {
        public LoadMoreItem createFromParcel(Parcel in) {
            return new LoadMoreItem(in);
        }

        public LoadMoreItem[] newArray(int size) {
            return new LoadMoreItem[size];
        }
    };

    private LoadMoreItem(Parcel in) {
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
    }
}
