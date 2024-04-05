package com.davidread.booklistings;

import android.os.Parcel;
import android.os.Parcelable;

public class Book implements Parcelable {


    public static final Creator<Book> CREATOR = new Creator<Book>() {
        @Override
        public Book createFromParcel(Parcel in) {
            return new Book(in);
        }

        @Override
        public Book[] newArray(int size) {
            return new Book[size];
        }
    };
    private String title;
    private String[] authors;
    private String url;
    public Book(String title, String[] authors, String url) {
        this.title = title;
        this.authors = authors;
        this.url = url;
    }
    protected Book(Parcel in) {
        title = in.readString();
        authors = in.createStringArray();
        url = in.readString();
    }

    public String getTitle() {
        return title;
    }


    public String[] getAuthors() {
        return authors;
    }

    public String getUrl() {
        return url;
    }

    @Override
    public int describeContents() {
        return 0;
    }
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(title);
        dest.writeStringArray(authors);
        dest.writeString(url);
    }
}
