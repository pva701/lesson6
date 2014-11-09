package com.example.pva701.rssreader;

import android.content.ContentValues;
import android.os.Parcel;
import android.os.Parcelable;

import com.example.pva701.rssreader.provider.RSSDatabaseHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;

/**
* Created by pva701 on 07.11.14.
*/
public class News implements Parcelable {
    private int id;
    private String title;
    private String link;
    private String description;
    private Date pubDate;
    private ArrayList<String> category;
    private boolean read;
    private int sourceId;
    public News() {
    }

    public News(Parcel parcel) {
        id = parcel.readInt();
        title = parcel.readString();
        link = parcel.readString();
        description = parcel.readString();
        pubDate = new Date(parcel.readInt() * 1000L);
        StringTokenizer tokenizer = new StringTokenizer(parcel.readString(), ",");
        while (tokenizer.hasMoreTokens())
            addCategory(tokenizer.nextToken());
        read = parcel.readInt() == 1;
        sourceId = parcel.readInt();
    }

    public void setId(long id) {
        this.id = (int)id;
    }

    public int getId() {
        return id;
    }

    public boolean equals(News other) {
        return pubDate.equals(other.getPubDate()) && title.equals(other.getTitle());
    }

    public void setSourceId(int id) {
        sourceId = id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Date getPubDate() {
        return pubDate;
    }

    public String getLink() {
        return link;
    }

    public ArrayList<String> getCategory() {
        if (category == null)
            category = new ArrayList<String>();
        return category;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPubDate(Date pubDate) {
        this.pubDate = pubDate;
    }

    public void addCategory(String cat) {
        if (category == null)
            category = new ArrayList<String>();
        category.add(cat);
    }

    public void setRead(boolean read) {
        this.read = read;
    }

    public String toStringCategory() {
        if (category == null)
            category = new ArrayList<String>();
        String ret = "";
        for (int i = 0; i < category.size(); ++i)
            if (i != category.size() - 1)
                ret += category.get(i) + ", ";
            else
                ret += category.get(i);
        return ret;
    }

    public boolean isRead() {
        return read;
    }

    public int getSourceId() {
        return sourceId;
    }

    @Override
    public String toString() {
        return "title: " + title + "; pubDate = " + pubDate.toString() + "; link = " + link;
    }

    public ContentValues toContentValues() {
        ContentValues cv = new ContentValues();
        cv.put(RSSDatabaseHelper.COLUMN_NEWS_TITLE, getTitle());
        cv.put(RSSDatabaseHelper.COLUMN_NEWS_LINK, getLink());
        cv.put(RSSDatabaseHelper.COLUMN_NEWS_DESCRIPTION, getDescription());
        cv.put(RSSDatabaseHelper.COLUMN_NEWS_PUB_DATE, getPubDate().getTime() / 1000);
        cv.put(RSSDatabaseHelper.COLUMN_NEWS_CATEGORY, toStringCategory());
        cv.put(RSSDatabaseHelper.COLUMN_NEWS_READ, isRead());
        cv.put(RSSDatabaseHelper.COLUMN_NEWS_SOURCE_ID, getSourceId());
        return cv;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(title);
        parcel.writeString(link);
        parcel.writeString(description);
        parcel.writeInt((int) (pubDate.getTime() / 1000));
        parcel.writeString(toStringCategory());
        parcel.writeInt(read ? 1 : 0);
        parcel.writeInt(sourceId);
    }

    public static final Creator<News> CREATOR = new Creator<News>() {

        @Override
        public News createFromParcel(Parcel parcel) {
            return new News(parcel);
        }

        @Override
        public News[] newArray(int i) {
            return new News[i];
        }
    };
}
