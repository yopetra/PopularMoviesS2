package com.example.android.popularmoviess1v02.utils;


import com.example.android.popularmoviess1v02.data.MovieEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;


public class OpenMovieJsonUtils {
    public static JSONArray getSimpleMovieStringFromJson(String movieJsonString) throws JSONException {

        final String OMJ_MESSAGE_CODE = "cod";
        final String OMJ_LIST = "results";
        final String OMJ_IMAGE_POSTER = "poster_path";
        final String OMJ_MOVIE_ID = "id";

        JSONArray jsonMovieData = null;

        JSONObject movieJson = new JSONObject(movieJsonString);

        JSONArray movieArray = movieJson.getJSONArray(OMJ_LIST);

        jsonMovieData = new JSONArray();

        for(int i = 0; i < movieArray.length(); i++){

            JSONObject currentMovieItem =  movieArray.getJSONObject(i);
            String posterPicture = currentMovieItem.getString(OMJ_IMAGE_POSTER);
            Integer posterId = currentMovieItem.getInt(OMJ_MOVIE_ID);

            JSONObject jsonMovieItem = new JSONObject();
            jsonMovieItem.put("posterPic", posterPicture);
            jsonMovieItem.put("posterId", posterId);

            jsonMovieData.put(jsonMovieItem);
        }

        return jsonMovieData;
    }

    public static JSONArray getSimpleMovieStringFromList(List<MovieEntry> movieEntries) throws JSONException {

        int listSize = movieEntries.size();
        JSONArray jsonMoviesArray = new JSONArray();

        for(int i = 0; i < listSize; i++){
            String posterPicture = movieEntries.get(i).getPicture();
            String posterId = Integer.toString(movieEntries.get(i).getMovieId());
            JSONObject jsonMovieItem = new JSONObject();
            jsonMovieItem.put("posterPic", posterPicture);
            jsonMovieItem.put("posterId", posterId);

            jsonMoviesArray.put(jsonMovieItem);
        }

        return jsonMoviesArray;
    }
}
