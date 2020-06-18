package com.example.android.popularmoviess1v02;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.popularmoviess1v02.data.AppDatabase;
import com.example.android.popularmoviess1v02.data.MovieEntry;
import com.example.android.popularmoviess1v02.data.Trailer;
import com.example.android.popularmoviess1v02.utils.ImageConverter;
import com.example.android.popularmoviess1v02.utils.NetworkUtils;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class DetailActivity extends AppCompatActivity {

    private static final String TAG = NetworkUtils.class.getSimpleName();
    private int mMovieId;
    private int mSortingType;
    private boolean mIsCurrentMovieInFavorite = false;
    List<MovieEntry> mMovieEntries;
    private static final int MOST_POPULAR = 1;
    private static final int TOP_RATED = 2;
    private static final int FAVORITE = 3;

    TextView mTitleTextView;
    ImageView mPosterImage;
    TextView mRatingTextView;
    TextView mReleaseDateTextView;
    TextView mRuntime;
    TextView mOverviewTextView;
    Button mReviewsBtn;
    Button mFavoriteBtn;

    private AppDatabase mDb;
    private URL mPosterUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Intent intent = getIntent();
        mMovieId = intent.getExtras().getInt(getString(R.string.movieId));
        mSortingType = intent.getExtras().getInt(getString(R.string.sorting_type));

        mTitleTextView = (TextView) findViewById(R.id.tv_originalTitle);
        mPosterImage = (ImageView) findViewById(R.id.iv_poster_image);
        mRatingTextView = (TextView) findViewById(R.id.tv_user_rating);
        mReleaseDateTextView = (TextView) findViewById(R.id.tv_release_date);
        mRuntime = (TextView) findViewById(R.id.tv_runtime);
        mOverviewTextView = (TextView) findViewById(R.id.tv_overview);
        mReviewsBtn = (Button) findViewById(R.id.bt_read_reviews);
        mFavoriteBtn = (Button) findViewById(R.id.bt_add_favorite);

        mDb = AppDatabase.getInstance(getApplicationContext());

        mReviewsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DetailActivity.this, ReviewsActivity.class);
                intent.putExtra(getString(R.string.movieId), mMovieId);
                startActivity(intent);
            }
        });

        mFavoriteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onFavoriteButtonClicked(mMovieId);
            }
        });

        getMovieDetails(mMovieId);
        getMovieTrailers(mMovieId);
        getFavoriteStatus(mMovieId);
    }

    private void getFavoriteStatus(final int mMovieId) {

        // Here we change status of the Favorite button
        AppExecutors.getInstance().diskIO().execute(new Runnable() {
            @Override
            public void run() {
                final List<MovieEntry> movieEntries = mDb.movieDao().loadAllMovies();
                mMovieEntries = movieEntries;
                boolean isAddedToFavorite = false;

                int sizeMovies = movieEntries.size();
                for (int i = 0; i < sizeMovies; i++){
                    if(mMovieId == movieEntries.get(i).getMovieId()){
                        isAddedToFavorite = true;
                    }
                }
                final boolean finalIsAddedToFavorite = isAddedToFavorite;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if(finalIsAddedToFavorite){

                            // If current opened movie have added to favorite, then button will have color with accent
                            mFavoriteBtn.setBackgroundColor(getResources().getColor(R.color.colorAccent));
                            mIsCurrentMovieInFavorite = true;
                        }else{

                            // Therwise button has default grey color
                            mFavoriteBtn.setBackgroundColor(getResources().getColor(R.color.colorGrey));
                            mIsCurrentMovieInFavorite = false;
                        }
                    }
                });
            }
        });
    }

    private void onFavoriteButtonClicked(int movieId) {
        if( mIsCurrentMovieInFavorite ){

            // Code to remove current movie from favorites
            Toast.makeText(getApplicationContext(), "Removed from favorites", Toast.LENGTH_SHORT).show();
            new DeleteMovieFromDb().execute(movieId);
        }else{

            // Code to add in favotites the current movie
            Toast.makeText(getApplicationContext(), "Added to favorite", Toast.LENGTH_SHORT).show();
            new SaveMovieFromDb().execute(mPosterUrl.toString());
        }
    }

    private void getMovieTrailers(Integer movieId) {
        new FetchMovieTrailersTask().execute(movieId);
    }

    class FetchMovieTrailersTask extends AsyncTask<Integer, Void, String>{
        @Override
        protected String doInBackground(Integer... integers) {
            Integer movieId = integers[0];

            String movieTrailersString = null;
            URL movieTrailersUrl = NetworkUtils.buildMovieTrailerUrl(movieId);
            try{
                movieTrailersString = NetworkUtils.getResponseFromHttpUrl(movieTrailersUrl);
            }catch(IOException e){
                e.printStackTrace();
            }

            return movieTrailersString;
        }

        @Override
        protected void onPostExecute(String s) {
            ArrayList<Trailer> trailers = new ArrayList<>();

            if(s != null){
                try {
                    JSONObject moviesJsonResponse = new JSONObject(s);
                    JSONArray moviesJsonArray = moviesJsonResponse.getJSONArray("results");
                    int arraySize = moviesJsonArray.length();

                    for(int i = 0; i < arraySize; i++){
                        JSONObject movieItem = moviesJsonArray.getJSONObject(i);
                        if(movieItem.getString("type").equals("Trailer")){
                            trailers.add(new Trailer(movieItem.getString("name"), movieItem.getString("key")) );
                        }

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                fillTrailerList(trailers);
            }
        }
    }

    private class SaveMovieFromDb extends AsyncTask<String, Void, Bitmap>{
        @Override
        protected Bitmap doInBackground(String... URL) {
            String posterUrl = URL[0];

            Bitmap bitmap = null;

            try{
                InputStream inputStream = new URL(posterUrl).openStream();
                bitmap = BitmapFactory.decodeStream(inputStream);
            }catch(Exception e){
                e.printStackTrace();
            }

            return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap bitmap) {

            int movieId = mMovieId;
            String name = mTitleTextView.getText().toString();
            String bitmapInString = ImageConverter.bitmapToString(bitmap);
            String yearReleased = mReleaseDateTextView.getText().toString();

            String runtimeInString = mRuntime.getText().toString();
            int cutCharacterRT = runtimeInString.indexOf("m");
            int runtime = Integer.parseInt(runtimeInString.substring(0, cutCharacterRT));

            String ratingInString = mRatingTextView.getText().toString();
            int cutCharacter = ratingInString.indexOf("/");
            int rating = Integer.parseInt(ratingInString.substring(0, cutCharacter));

            String overview = mOverviewTextView.getText().toString();

            final MovieEntry movieEntry = new MovieEntry(movieId, name, bitmapInString, yearReleased, runtime, rating, overview);
            AppExecutors.getInstance().diskIO().execute(new Runnable() {
                @Override
                public void run() {
                    mDb.movieDao().insertMovie(movieEntry);
                }
            });

            mFavoriteBtn.setBackgroundColor(getResources().getColor(R.color.colorAccent));
        }
    }

    private class DeleteMovieFromDb extends AsyncTask<Integer, Void, Void>{

        @Override
        protected Void doInBackground(Integer... integers) {

            int movieId = integers[0];
            int listSize = mMovieEntries.size();
            int indexOfMovieInList = -1;

            for(int i = 0; i < listSize; i++){
                if(movieId == mMovieEntries.get(i).getMovieId()){
                    indexOfMovieInList = i;
                    break;
                }
            }

            if(indexOfMovieInList >= 0 ){
                final MovieEntry movieEntry = mMovieEntries.get(indexOfMovieInList);
                AppExecutors.getInstance().diskIO().execute(new Runnable() {
                    @Override
                    public void run() {
                        mDb.movieDao().deleteMovie(movieEntry);
                    }
                });
            }else{
                Log.d(TAG, "Movie have not been deleted");
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mFavoriteBtn.setBackgroundColor(getResources().getColor(R.color.colorGrey));
            super.onPostExecute(aVoid);
        }
    }

    private void fillTrailerList(final ArrayList<Trailer> trailers) {
        // Fing Layout where will be inflated new trailer
        LinearLayout trailersLayout = (LinearLayout) findViewById(R.id.ll_trailers_list);
        LayoutInflater inflater = getLayoutInflater();
        int trailersSize = trailers.size();

        for(int i = 0;  i < trailersSize; i++){

            // Inflate in selected Layout the trailer view item
            View view  = inflater.inflate(R.layout.trailer_item, trailersLayout, false);
            TextView nameTextView = (TextView) view.findViewById(R.id.tv_trailer_name);
            nameTextView.setText( trailers.get(i).getName() );
            view.getLayoutParams().width = LinearLayout.LayoutParams.MATCH_PARENT;

            // Assign onClickListener for every trailer item
            final int finalI = i;
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    // Create intent to open link of trailer
                    String url = "https://www.youtube.com/watch?v=" + trailers.get(finalI).getKey();
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                }
            });
            trailersLayout.addView(view);
        }
    }

    private void getMovieDetails(Integer movieId) {
        new FetchMovieDetailsTask().execute(movieId);
    }

    class FetchMovieDetailsTask extends AsyncTask<Integer, Void, String>{
        @Override
        protected String doInBackground(Integer... integers) {
            List<MovieEntry> movieEntries = mDb.movieDao().loadAllMovies();
            mMovieEntries = movieEntries;

            Integer movieId = integers[0];
            String movieDetailsString = null;

            if(mSortingType != FAVORITE){

                // Fetch movie data from interne
                URL movieDetailsUrl = NetworkUtils.buildMovieDetailsUrl(movieId);
                try {
                    movieDetailsString = NetworkUtils.getResponseFromHttpUrl(movieDetailsUrl);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }else{

                // Fetch movie data from database and put in in the JSONObject
                // then convert it in to String
                MovieEntry currentMovieEntry = getMovieByMovieId(movieId);
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("original_title", currentMovieEntry.getName());
                    jsonObject.put("poster_path", currentMovieEntry.getPicture()); // Actually this is not path, but image it self
                    jsonObject.put("vote_average", currentMovieEntry.getRating());
                    jsonObject.put("overview", currentMovieEntry.getOverview());
                    jsonObject.put("release_date", currentMovieEntry.getYearReleased());
                    jsonObject.put("runtime", currentMovieEntry.getRuntime());

                    movieDetailsString = jsonObject.toString();
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }


            return movieDetailsString;
        }

        private MovieEntry getMovieByMovieId(Integer movieId) {
            int listSize = mMovieEntries.size();
            for(int i = 0; i < listSize; i++){
                if(mMovieEntries.get(i).getMovieId() == movieId){
                    return mMovieEntries.get(i);
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            String title = null;
            String posterImageLink = null;
            String releaseDate = null;
            Integer runTime = null;
            Integer rating = null;

            String overview = null;

            if( s != null){
                try {
                    JSONObject jsonMovie = new JSONObject(s);
                    title = jsonMovie.getString("original_title");
                    posterImageLink = jsonMovie.getString("poster_path");
                    rating = jsonMovie.getInt("vote_average");
                    overview = jsonMovie.getString("overview");
                    releaseDate = jsonMovie.getString("release_date");
                    runTime = jsonMovie.getInt("runtime");
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }else{
                Log.d(TAG, "Error in postExecute");
            }

            mTitleTextView.setText(title);

            if(mSortingType != FAVORITE){
                URL picUrl = NetworkUtils.buildPictureUrl(posterImageLink);
                mPosterUrl = picUrl;
                if( picUrl != null ){
                    Picasso.get().load(String.valueOf(picUrl)).into(mPosterImage); //.resize(200,245)
                }

                mReviewsBtn.setVisibility(View.VISIBLE);
            }else{
                Bitmap posterBitmap = ImageConverter.stringToBitmap(posterImageLink);
                mPosterImage.setImageBitmap(posterBitmap);

                mReviewsBtn.setVisibility(View.INVISIBLE);
            }

            try{
                mRatingTextView.setText(rating + "/10");
                mReleaseDateTextView.setText(releaseDate.substring(0, 4));
                mRuntime.setText(runTime + "min");
                mOverviewTextView.setText(overview);
            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }
}
