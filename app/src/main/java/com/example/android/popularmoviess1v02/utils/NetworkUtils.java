package com.example.android.popularmoviess1v02.utils;

import android.net.Uri;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class NetworkUtils {

    private static final String TAG = NetworkUtils.class.getSimpleName();

    private static final String BASE_URL = "https://api.themoviedb.org";
    private static final String POP_MOVIES = "/3/movie/popular";
    private static final String TOP_RATED = "/3/movie/top_rated";
    private static final String MOVIE_DETAIL = "/3/movie/";
    private static final String API_KEY = "INSERT_YOUR_API_KEY_REHE";
    private static final String LANGUAGE = "en-US";
    private static final String MOVIE_VIDEOS = "/videos";
    private static final String MOVIE_REVIEWS = "/reviews";

    private static final String BASE_PIC_URL = "https://image.tmdb.org/t/p/w185/";

    public static URL buildUrl(int pageNumber, int sortType) {
        String urlSorting = null;
        if(sortType == 1){
            urlSorting = POP_MOVIES;
        }else{
            if(sortType == 2){
                urlSorting = TOP_RATED;
            }
        }
        Uri buildUri = Uri.parse(BASE_URL + urlSorting).buildUpon()
                .appendQueryParameter("api_key", API_KEY)
                .appendQueryParameter("language", LANGUAGE)
                .appendQueryParameter("page", Integer.toString(pageNumber))
                .build();

        URL url = null;
        try {
            url = new URL(buildUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "Build URl=" + url);

        return url;
    }

    public static URL buildPictureUrl(String pictureName){
        Uri buildUri = Uri.parse(BASE_PIC_URL + pictureName).buildUpon().build();

        URL url = null;
        try{
            url = new URL(buildUri.toString());
        }catch(MalformedURLException e){
            e.printStackTrace();
        }

        Log.d(TAG, "URL to pic = " + url );

        return url;
    }

    public static URL buildMovieDetailsUrl(Integer movieId){
        Uri buildUri = Uri.parse(BASE_URL + MOVIE_DETAIL + movieId).buildUpon()
                .appendQueryParameter("api_key", API_KEY)
                .appendQueryParameter("language", LANGUAGE)
                .build();

        URL url = null;
        try {
            url = new URL(buildUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        Log.d(TAG, "URL to movie = " + url);

        return url;
    }

    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if (hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }

    public static URL buildMovieTrailerUrl(Integer movieId) {
        Uri buildUri = Uri.parse(BASE_URL + MOVIE_DETAIL + movieId + MOVIE_VIDEOS).buildUpon()
                .appendQueryParameter("api_key", API_KEY)
                .appendQueryParameter("language", LANGUAGE)
                .build();

        URL url = null;
        try{
            url = new URL(buildUri.toString());
        }catch(MalformedURLException e){
            e.printStackTrace();
        }

        Log.d(TAG, "URL to trailers = " + url);

        return url;
    }

    public static URL buildMovieReviewsUrl(int movieId, int reviewPageNumber) {
        Uri buildUri = Uri.parse(BASE_URL + MOVIE_DETAIL + movieId + MOVIE_REVIEWS).buildUpon()
                .appendQueryParameter("api_key", API_KEY)
                .appendQueryParameter("language", LANGUAGE)
                .appendQueryParameter("page", Integer.toString(reviewPageNumber))
                .build();

        URL url = null;
        try{
            url = new URL(buildUri.toString());
        }catch(MalformedURLException e){
            e.printStackTrace();
        }

        Log.d(TAG, "URL to reviews = " + url);

        return url;
    }

    public static boolean isConnected(){
        final String command = "ping -c 1 google.com";
        try {
            return Runtime.getRuntime().exec(command).waitFor() == 0;
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }
}
