package com.example.pdfscanner;

import java.util.Date;
import java.util.UUID;

public class File {
    private UUID mId;
    private String mTitle;
    private Date mDate;
    private String mType;
    private String mUrl;


    public File() {
        this.mId = UUID.randomUUID();
        this.mDate = new Date();
    }

    public UUID getId() {
        return mId;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String mTitle) {
        this.mTitle = mTitle;
    }

    public Date getDate() {
        return mDate;
    }

    public void setDate(Date mDate) {
        this.mDate = mDate;
    }

    public String getType() {
        return mType;
    }

    public void setType(String type) {
        this.mType = type;
    }

    public void setmUrl(String url) {
        this.mUrl = url;
    }

    public String getUrl() {
        return mUrl;
    }
}
