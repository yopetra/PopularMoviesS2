package com.example.android.popularmoviess1v02;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.example.android.popularmoviess1v02.utils.ImageConverter;
import com.example.android.popularmoviess1v02.utils.NetworkUtils;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URL;

public class MoviesAdapter extends RecyclerView.Adapter<MoviesAdapter.MovieViewHolder> {

    private static int mSortingType;
    private static final int FAVORITE = 3;
    private JSONArray mMoviesData = new JSONArray();

    private final MovieAdapterOnClickHandler mClickHandler;

    public interface MovieAdapterOnClickHandler{
        void onClick(JSONObject movieItem);
    }

    public MoviesAdapter(MovieAdapterOnClickHandler clickHandler){
        mClickHandler = clickHandler;
    }

    class MovieViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView movieItemImageView;

        public MovieViewHolder(View itemView) {
            super(itemView);
            movieItemImageView = (ImageView) itemView.findViewById(R.id.iv_movie_item);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            int adapterPosition = getAdapterPosition();
            JSONObject dataOfMovieItem = null;
            try {
                dataOfMovieItem = (JSONObject) mMoviesData.get(adapterPosition);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            mClickHandler.onClick(dataOfMovieItem);
        }
    }

    @NonNull
    @Override
    public MovieViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        Context context = viewGroup.getContext();
        int layoutIdForListItem = R.layout.movie_list_item;
        LayoutInflater inflater = LayoutInflater.from(context);
        boolean shouldAttachToParent = false;

        View view = inflater.inflate(layoutIdForListItem, viewGroup, shouldAttachToParent);
        MovieViewHolder viewHolder = new MovieViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MovieViewHolder movieViewHolder, int position) {
        String currentMoviePic = null;
        try {
            JSONObject currentMovieItem = (JSONObject) mMoviesData.get(position);
            currentMoviePic = currentMovieItem.getString("posterPic");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        if(mSortingType != FAVORITE){
            // Here Picasso set Image
            URL picUrl = NetworkUtils.buildPictureUrl(currentMoviePic);
            if( picUrl != null ){

                Picasso.get()
                        .load(String.valueOf(picUrl))
                        .resize(200,245)
                        .into(movieViewHolder.movieItemImageView);
            }
        }else if(mSortingType == FAVORITE){
            Bitmap posterBitmap = ImageConverter.stringToBitmap(currentMoviePic);

            movieViewHolder.movieItemImageView.setImageBitmap(posterBitmap);
        }
    }

    @Override
    public int getItemCount() {
        if(mMoviesData == null){
            return 0;
        }
        return mMoviesData.length();
    }

    public void setMovieData(JSONArray movieData, int sortingType){
        mSortingType = sortingType;

        int arraySize = movieData.length();
        for(int i = 0; i < arraySize; i++){
            try {
                JSONObject currentJsonItem = (JSONObject) movieData.getJSONObject(i);
                mMoviesData.put(currentJsonItem);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        notifyDataSetChanged();
    }

    public void clearData(){
        while( mMoviesData.length() > 0 ){
            mMoviesData.remove(0);
        }
    }
}
