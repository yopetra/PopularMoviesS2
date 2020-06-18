package com.example.android.popularmoviess1v02.data;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;
import android.arch.persistence.room.PrimaryKey;

@Entity(tableName = "movie")
public class MovieEntry {

    @PrimaryKey(autoGenerate = true)
    private int id;
    private int movieId;
    private String name;
    private String picture;
    private String yearReleased;
    private int runtime;
    private int rating;
    private String overview;

    @Ignore
    public MovieEntry(int movieId, String name, String picture, String yearReleased, int runtime, int rating, String overview){
        this.movieId = movieId;
        this.name = name;
        this.picture = picture;
        this.yearReleased = yearReleased;
        this.runtime = runtime;
        this.rating = rating;
        this.overview = overview;
    }

    public MovieEntry(int id, int movieId, String name, String picture, String yearReleased, int runtime, int rating, String overview){
        this.id = id;
        this.movieId = movieId;
        this.name = name;
        this.picture = picture;
        this.yearReleased = yearReleased;
        this.runtime = runtime;
        this.rating = rating;
        this.overview = overview;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMovieId() {
        return movieId;
    }

    public void setMovieId(int movieId) {
        this.movieId = movieId;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getYearReleased() {
        return yearReleased;
    }

    public void setYearReleased(String yearReleased) {
        this.yearReleased = yearReleased;
    }

    public int getRuntime() {
        return runtime;
    }

    public void setRuntime(int runtime) {
        this.runtime = runtime;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }
}
