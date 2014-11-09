package com.example.pva701.rssreader;

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;

import com.example.pva701.rssreader.provider.RSSDatabaseHelper;

import java.util.Date;

/**
* Created by pva701 on 07.11.14.
*/
public class Source implements Parcelable {
    private int id;
    private String url;
    private String name;
    private Date lastUpdate;

    public Source(String name, String url, Date lastUpdate) {
        this.name = name;
        this.url = url;
        this.lastUpdate = lastUpdate;
    }

    public Source(long id, String name, String url, Date lastUpdate) {
        this.id = (int)id;
        this.name = name;
        this.url = url;
        this.lastUpdate = lastUpdate;
    }

    public Source(Parcel parcel) {
        id = parcel.readInt();
        url = parcel.readString();
        name = parcel.readString();
        lastUpdate = new Date(parcel.readInt() * 1000L);
    }

    public String getUrl() {
        return url;
    }

    public String getName() {
        return name;
    }

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setId(long id) {
        this.id = (int)id;
    }

    public int getId() {
        return id;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ContentValues toContentValues() {
        ContentValues cv = new ContentValues();
        cv.put(RSSDatabaseHelper.COLUMN_SOURCES_NAME, getName());
        cv.put(RSSDatabaseHelper.COLUMN_SOURCES_URL, getUrl());
        cv.put(RSSDatabaseHelper.COLUMN_SOURCES_LAST_UPDATE, getLastUpdate().getTime() / 1000);
        return cv;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(url);
        parcel.writeString(name);
        parcel.writeInt((int)(lastUpdate.getTime() / 1000));
    }

    public static final Creator<Source> CREATOR = new Creator<Source>() {

        @Override
        public Source createFromParcel(Parcel parcel) {
            return new Source(parcel);
        }

        @Override
        public Source[] newArray(int i) {
            return new Source[i];
        }
    };
}
