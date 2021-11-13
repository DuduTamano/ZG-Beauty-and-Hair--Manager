package com.example.zgbeautyandhairstaff.Model;

import com.google.firebase.Timestamp;

public class News {
    private String image, TITLE_KEY, CONTENT_KEY;
    private Timestamp timestamp;

    public News() {
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getTITLE_KEY() {
        return TITLE_KEY;
    }

    public void setTITLE_KEY(String TITLE_KEY) {
        this.TITLE_KEY = TITLE_KEY;
    }

    public String getCONTENT_KEY() {
        return CONTENT_KEY;
    }

    public void setCONTENT_KEY(String CONTENT_KEY) {
        this.CONTENT_KEY = CONTENT_KEY;
    }

    public Timestamp getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Timestamp timestamp) {
        this.timestamp = timestamp;
    }
}
