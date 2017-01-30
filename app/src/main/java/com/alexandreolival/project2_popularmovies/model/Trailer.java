package com.alexandreolival.project2_popularmovies.model;

public class Trailer {

    private String title;
    private String url;

    public Trailer(String title, String key) {
        this.title = title;
        this.url = "https://www.youtube.com/watch?v=" + key;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
